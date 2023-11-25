package dev.gmelon.plango.config.auth.social;

import dev.gmelon.plango.config.auth.social.dto.SocialAccountResponse;
import dev.gmelon.plango.domain.member.MemberType;

public interface SocialClient {
    boolean supports(MemberType type);

    SocialAccountResponse requestAccountResponse(String token);

    void revokeToken(Long targetId, String token);
}
