package dev.gmelon.plango.domain.fcm.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import dev.gmelon.plango.domain.member.entity.Member;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FirebaseCloudMessageTokenTest {
    @Test
    void 토큰을_업데이트하면_lastUpdatedDate이_주어진_시간으로_갱신된다() {
        // given
        LocalDateTime oldDateTime = LocalDateTime.of(2023, 10, 1, 0, 0, 0);
        LocalDateTime newDateTime = LocalDateTime.of(2023, 10, 10, 0, 0, 0);
        FirebaseCloudMessageToken token = FirebaseCloudMessageToken.builder()
                .lastUpdatedDate(oldDateTime)
                .build();

        // when
        token.update(newDateTime);

        // then
        assertThat(token.getLastUpdatedDate()).isEqualTo(newDateTime);
    }

    @Test
    void 토큰을_소유한_회원과_주어진_회원이_동일한지_비교한다() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("hsh1769@naver.com")
                .nickname("gmelon")
                .build();
        FirebaseCloudMessageToken token = FirebaseCloudMessageToken.builder()
                .member(member)
                .build();

        // when, then
        assertThat(token.isMemberEquals(member.getId())).isTrue();
    }
}
