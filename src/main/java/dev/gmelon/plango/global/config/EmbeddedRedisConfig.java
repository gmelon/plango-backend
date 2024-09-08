package dev.gmelon.plango.global.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.StringUtils;
import redis.embedded.RedisServer;

@Profile({"local", "test"})
@Configuration(proxyBeanMethods = false)
public class EmbeddedRedisConfig {
    private static final int PORT_CANDIDATE_START = 10000;
    private static final int PORT_CADIDATE_END = 65535;
    private static final String PORT_STATUS_COMMAND_FORMAT = "netstat -nat | grep LISTEN | grep %d";
    private static final String OSARCH_PROPERTY_KEY = "os.arch";
    private static final String OSARCH_PROPERTY_ARM_VALUE = "aarch64";
    private static final String OSNAME_PROPERTY_KEY = "os.name";
    private static final String OSNAME_PROPERTY_MAC_VALUE = "Mac OS X";
    private static final String ARM_MAC_REDIS_BINARY_PATH = "classpath:binary/redis/redis-server-stable-arm64";

    @Value("${spring.redis.port}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() throws IOException {
        redisServer = resolveRedisServer(resolvePort());
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    private int resolvePort() throws IOException {
        if (isRedisRunning()) {
            return findAvailablePort();
        }
        return redisPort;
    }

    private boolean isRedisRunning() throws IOException {
        return isRunning(executeGrepProcessCommand(redisPort));
    }

    private int findAvailablePort() throws IOException {
        for (int port = PORT_CANDIDATE_START; port <= PORT_CADIDATE_END; port++) {
            Process process = executeGrepProcessCommand(port);
            if (!isRunning(process)) {
                return port;
            }
        }

        throw new IllegalArgumentException("Embedded Redis 실행을 위한 적절한 포트를 찾지 못했습니다.");
    }

    private Process executeGrepProcessCommand(int port) throws IOException {
        String command = String.format(PORT_STATUS_COMMAND_FORMAT, port);
        String[] shell = {"/bin/sh", "-c", command};
        return Runtime.getRuntime().exec(shell);
    }

    private boolean isRunning(Process process) {
        String line;
        StringBuilder pidInfo = new StringBuilder();

        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            while ((line = input.readLine()) != null) {
                pidInfo.append(line);
            }
        } catch (Exception ignored) {
        }

        return StringUtils.hasLength(pidInfo.toString());
    }

    private RedisServer resolveRedisServer(int port) {
        if (isArmMac()) {
            return new RedisServer(getRedisExecutableFileForArmMac(), port);
        }
        return new RedisServer(port);
    }

    private boolean isArmMac() {
        return Objects.equals(System.getProperty(OSARCH_PROPERTY_KEY), OSARCH_PROPERTY_ARM_VALUE)
                && Objects.equals(System.getProperty(OSNAME_PROPERTY_KEY), OSNAME_PROPERTY_MAC_VALUE);
    }

    private File getRedisExecutableFileForArmMac() {
        try {
            return ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader())
                    .getResource(ARM_MAC_REDIS_BINARY_PATH).getFile();
        } catch (IOException e) {
            throw new IllegalArgumentException("Arm Mac OS 용 Embedded Redis 실행 파일을 찾지 못했습니다.");
        }
    }
}
