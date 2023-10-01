package dev.gmelon.plango.infrastructure.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageToken;
import dev.gmelon.plango.domain.fcm.FirebaseCloudMessageTokenRepository;
import dev.gmelon.plango.domain.member.Member;
import dev.gmelon.plango.domain.notification.Notification;
import dev.gmelon.plango.exception.fcm.FirebaseTokenRefreshFailureException;
import dev.gmelon.plango.infrastructure.fcm.dto.FcmRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
@Service
public class FirebaseCloudMessageService {

    private static final String CREDENTIAL_JSON_PATH = "firebase/firebase-adminsdk.json";
    private static final String CLOUD_MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String API_URL = "https://fcm.googleapis.com/v1/projects/plango-395216/messages:send";

    private final FirebaseCloudMessageTokenRepository firebaseCloudMessageTokenRepository;
    private final RestTemplate restTemplate;

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
        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, httpEntity, String.class);

        if (response.getStatusCode().isError()) {
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
                    .fromStream(new ClassPathResource(CREDENTIAL_JSON_PATH).getInputStream())
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
