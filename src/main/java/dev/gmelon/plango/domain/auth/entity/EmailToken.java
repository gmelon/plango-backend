package dev.gmelon.plango.domain.auth.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "emailToken", timeToLive = 30 * 60)
public class EmailToken {
    @Id
    private String email;

    private String tokenValue;

    private boolean authenticated;

    @Builder
    public EmailToken(String email, String tokenValue) {
        this.email = email;
        this.tokenValue = tokenValue;
        this.authenticated = false;
    }

    public boolean tokenValueEquals(String tokenValue) {
        return this.tokenValue.equals(tokenValue);
    }

    public void authenticate() {
        this.authenticated = true;
    }

    public boolean authenticated() {
        return this.authenticated;
    }
}
