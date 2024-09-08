package dev.gmelon.plango.global.config.auth.social;

import dev.gmelon.plango.domain.member.entity.MemberType;
import dev.gmelon.plango.global.config.auth.social.dto.SocialAccountResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class SocialClients {
    private final List<SocialClient> clients;

    public SocialAccountResponse requestAccountResponse(MemberType type, String token) {
        return clients.stream()
                .filter(client -> client.supports(type))
                .findAny()
                .map(client -> client.requestAccountResponse(token))
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Member Type입니다. - " + type));
    }

    public void revokeToken(MemberType type, Long targetId, String token) {
        clients.stream()
                .filter(client -> client.supports(type))
                .findAny()
                .ifPresentOrElse(client -> client.revokeToken(targetId, token),
                        () -> {
                            throw new IllegalArgumentException("유효하지 않은 Member Type입니다. - " + type);
                        });
    }
}
