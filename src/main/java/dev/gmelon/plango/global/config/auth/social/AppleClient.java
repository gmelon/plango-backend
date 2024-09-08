package dev.gmelon.plango.global.config.auth.social;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.gmelon.plango.domain.auth.exception.InvalidSocialTokenException;
import dev.gmelon.plango.domain.member.entity.MemberType;
import dev.gmelon.plango.global.config.auth.social.dto.ApplePublicKeyResponse;
import dev.gmelon.plango.global.config.auth.social.dto.ApplePublicKeyResponse.Key;
import dev.gmelon.plango.global.config.auth.social.dto.SocialAccountResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class AppleClient implements SocialClient {
    private static final String BASE_URL = "https://appleid.apple.com/auth";
    private static final ResourcePatternResolver resourcePatternResolver;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    static {
        resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader());
    }

    @Value("${apple-key-path}")
    private String appleKeyPath;

    @Value("${auth.social.apple.client_id}")
    private String clientId;

    @Value("${auth.social.apple.team_id}")
    private String teamId;

    @Value("${auth.social.apple.key_id}")
    private String keyId;

    @Override
    public boolean supports(MemberType type) {
        return type == MemberType.APPLE;
    }

    @Override
    public SocialAccountResponse requestAccountResponse(String token) {
        String idToken = request(token, "id_token");
        try {
            String idHeaderToken = idToken.split("\\.")[0];
            String decodedIdHeaderToken = new String(Base64.getDecoder().decode(idHeaderToken));
            Map<String, String> idHeader = objectMapper.readValue(decodedIdHeaderToken, Map.class);

            Claims payload = Jwts.parser()
                    .verifyWith(publicKeyOf(idHeader.get("kid"), idHeader.get("alg")))
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();
            String email = payload.get("email", String.class);
            return SocialAccountResponse.builder()
                    .email(email)
                    .nickname(email)
                    .build();
        } catch (JwtException | JsonProcessingException exception) {
            throw new InvalidSocialTokenException(exception);
        }
    }

    @Override
    public void revokeToken(Long targetId, String token) {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret());
        body.add("token", request(token, "access_token"));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Object> httpEntity = new HttpEntity<>(body, httpHeaders);

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL + "/revoke", httpEntity, String.class);

        if (response.getStatusCode().isError()) {
            throw new InvalidSocialTokenException();
        }
    }

    private String request(String token, String field) {
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret());
        body.add("code", token);
        body.add("grant_type", "authorization_code");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Object> httpEntity = new HttpEntity<>(body, httpHeaders);

        ResponseEntity<String> response = restTemplate.postForEntity(BASE_URL + "/token", httpEntity, String.class);

        if (response.getStatusCode().isError()) {
            throw new InvalidSocialTokenException();
        }

        try {
            JSONObject jsonObject = new JSONObject(response.getBody());
            return jsonObject.getString(field);
        } catch (JSONException exception) {
            throw new InvalidSocialTokenException(exception);
        }
    }

    private PublicKey publicKeyOf(String kid, String alg) {
        ResponseEntity<ApplePublicKeyResponse> response = restTemplate.getForEntity(BASE_URL + "/keys",
                ApplePublicKeyResponse.class);

        if (response.getStatusCode().isError()) {
            throw new IllegalArgumentException("Apple Public Key를 불러오는데 실패했습니다.");
        }

        Key key = response.getBody().matchedKeyOf(kid, alg);

        Decoder decoder = Base64.getUrlDecoder();
        byte[] nBytes = decoder.decode(key.getN());
        byte[] eBytes = decoder.decode(key.getE());

        BigInteger n = new BigInteger(1, nBytes);
        BigInteger e = new BigInteger(1, eBytes);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(key.getKty());
            return keyFactory.generatePublic(new RSAPublicKeySpec(n, e));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalArgumentException("Apple Public Key 생성에 실패했습니다.", exception);
        }
    }

    private String clientSecret() {
        return Jwts.builder()
                .header()
                .keyId(keyId)
                .and()
                .issuer(teamId)
                .issuedAt(Timestamp.valueOf(LocalDateTime.now()))
                .expiration(Timestamp.valueOf(LocalDateTime.now().plusMinutes(30)))
                .audience().add("https://appleid.apple.com")
                .and()
                .subject(clientId)
                .signWith(privateKey(), SIG.ES256)
                .compact();
    }

    private PrivateKey privateKey() {
        byte[] keyBytes = new byte[0];
        try {
            keyBytes = resourcePatternResolver.getResource(appleKeyPath).getInputStream().readAllBytes();
        } catch (IOException exception) {
            throw new IllegalArgumentException("Apple Private Key를 불러오는데 실패했습니다.");
        }

        String key = new String(keyBytes).replaceAll("\\s+", "");
        try {
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(key)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new IllegalArgumentException("Apple Private Key 생성에 실패했습니다.", exception);
        }
    }
}
