package dev.gmelon.plango.config.auth.social.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ApplePublicKeyResponse {
    private List<Key> keys;

    @NoArgsConstructor
    @Getter
    public static class Key {
        private String kty;
        private String kid;
        private String use;
        private String alg;
        private String n;
        private String e;
    }

    public Key matchedKeyOf(String kid, String alg) {
        return this.keys.stream()
                .filter(key -> key.getKid().equals(kid) && key.getAlg().equals(alg))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Apple Public Key 목록에 존재하지 않는 kid와 alg입니다. kid: " + kid + ", alg: " + alg));
    }
}
