package dev.gmelon.plango.infrastructure.fcm;

import static java.util.stream.Collectors.toList;

import com.google.auth.oauth2.GoogleCredentials;
import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageToken;
import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageTokenRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.notification.Notification;
import dev.gmelon.plango.exception.fcm.FirebaseTokenRefreshFailureException;
import dev.gmelon.plango.infrastructure.fcm.dto.FcmRequestDto;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
public class FirebaseCloudMessageService {

    private static final String CLOUD_MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String API_URL = "https://fcm.googleapis.com/v1/projects/plango-395216/messages:send";
    private static final ResourcePatternResolver resourcePatternResolver;

    private final FirebaseCloudMessageTokenRepository firebaseCloudMessageTokenRepository;
    private final RestTemplate restTemplate;

    @Value("${firebase-token-path}")
    private String firebaseTokenPath;

    static {
        resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader());
    }

    public void sendMessageTo(Notification notification, Member targetMember) {
        List<String> targetTokens = findAllTokensByMember(targetMember);
        for (String targetToken : targetTokens) {
            sendMessageTo(notification, targetToken);
        }
    }

    private List<String> findAllTokensByMember(Member member) {
        return firebaseCloudMessageTokenRepository.findAllByMember(member).stream()
                .map(FirebaseCloudMessageToken::getTokenValue)
                .collect(toList());
    }

    private void sendMessageTo(Notification notification, String targetToken) {
        HttpEntity<FcmRequestDto> httpEntity = createHttpEntity(notification, targetToken);
        ResponseEntity<String> response = null;
        try {
            response = restTemplate.postForEntity(API_URL, httpEntity, String.class);
        } catch (RestClientException exception) {
            log.warn("FirebaseCloudMessage 푸시 발송 실패. code: {}, body: {}", response.getStatusCode(), response.getBody());
        }
    }

    private HttpEntity<FcmRequestDto> createHttpEntity(Notification notification, String targetToken) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(accessToken());

        return new HttpEntity<>(FcmRequestDto.from(notification, targetToken), httpHeaders);
    }

    private String accessToken() {
        GoogleCredentials googleCredentials = null;
        try {
            googleCredentials = GoogleCredentials
                    .fromStream(resourcePatternResolver.getResource(firebaseTokenPath).getInputStream())
                    .createScoped(CLOUD_MESSAGING_SCOPE);
            googleCredentials.refreshIfExpired();
        } catch (IOException e) {
            log.error("Firebase token refresh 실패.", e);
            // TODO 푸시가 발송되지 못해도 상위 Schedule, Notication 관련 트랜잭션은 커밋되어야 함
            throw new FirebaseTokenRefreshFailureException();
        }

        return googleCredentials.getAccessToken().getTokenValue();
    }

}
