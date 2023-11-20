package dev.gmelon.plango.domain.refreshtoken;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash("refreshToken")
public class RefreshToken {
    @Id
    private String email;

    private String tokenValue;

    @Builder
    public RefreshToken(String email, String tokenValue) {
        this.email = email;
        this.tokenValue = tokenValue;
    }

    public void updateTokenValue(String newTokenValue) {
        this.tokenValue = newTokenValue;
    }
}
