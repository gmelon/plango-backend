package dev.gmelon.plango.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration(proxyBeanMethods = false)
public class AsyncConfig implements AsyncConfigurer {
    private static final int ASYNC_THREAD_SIZE = 5;
    private static final String ASYNC_THREAD_PREFIX = "async";

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(ASYNC_THREAD_SIZE);
        executor.setMaxPoolSize(ASYNC_THREAD_SIZE);
        executor.setThreadNamePrefix(ASYNC_THREAD_PREFIX);

        executor.initialize();
        return executor;
    }
}
