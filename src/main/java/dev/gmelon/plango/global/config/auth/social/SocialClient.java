package dev.gmelon.plango.global.config.auth.social;

import dev.gmelon.plango.domain.member.entity.MemberType;
import dev.gmelon.plango.global.config.auth.social.dto.SocialAccountResponse;

public interface SocialClient {
    boolean supports(MemberType type);

    SocialAccountResponse requestAccountResponse(String token);

    void revokeToken(Long targetId, String token);
}
