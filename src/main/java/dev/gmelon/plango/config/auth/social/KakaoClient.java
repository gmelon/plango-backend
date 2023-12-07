package dev.gmelon.plango.config.auth.social;

import dev.gmelon.plango.config.auth.social.dto.SocialAccountResponse;
import dev.gmelon.plango.domain.member.MemberType;
import dev.gmelon.plango.exception.auth.InvalidSocialTokenException;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class KakaoClient implements SocialClient {
    private static final String BASE_URL = "https://kapi.kakao.com";

    private final RestTemplate restTemplate;

    @Override
    public boolean supports(MemberType type) {
        return type == MemberType.KAKAO;
    }

    @Override
    public SocialAccountResponse requestAccountResponse(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.setBearerAuth(token);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL + "/v2/user/me", httpEntity, String.class);

        if (response.getStatusCode().isError()) {
            throw new InvalidSocialTokenException();
        }

        try {
            JSONObject jsonObject = new JSONObject(response.getBody());
            JSONObject kakaoAccount = jsonObject.getJSONObject("kakao_account");
            String email = kakaoAccount.getString("email");
            return SocialAccountResponse.builder()
                    .email(email)
                    .nickname(email)
                    .build();
        } catch (JSONException exception) {
            throw new InvalidSocialTokenException(exception);
        }
    }

    @Override
    public void revokeToken(Long targetId, String token) {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", targetId.toString());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        httpHeaders.setBearerAuth(token);
        HttpEntity<Object> httpEntity = new HttpEntity<>(body, httpHeaders);

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL + "/v1/user/unlink", httpEntity,
                String.class);

        if (response.getStatusCode().isError()) {
            throw new InvalidSocialTokenException();
        }
    }
}
