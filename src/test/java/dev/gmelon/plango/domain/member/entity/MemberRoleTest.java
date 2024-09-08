package dev.gmelon.plango.domain.member.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberRoleTest {
    @ParameterizedTest
    @MethodSource("parseArgumentsProvider")
    void 주어진_role문자열과_일치하는_enum을_반환한다(String roleString, MemberRole expectedRole) {
        assertThat(MemberRole.parse(roleString)).isEqualTo(expectedRole);
    }

    static Stream<Arguments> parseArgumentsProvider() {
        return Stream.of(
                Arguments.of("role_user", MemberRole.ROLE_USER),
                Arguments.of("ROLE_USER", MemberRole.ROLE_USER),
                Arguments.of("role_admin", MemberRole.ROLE_ADMIN),
                Arguments.of("ROLE_ADMIN", MemberRole.ROLE_ADMIN)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"role_use", "user_role", "a", "("})
    void 유효하지_않은_문자열이_입력되면_예외가_발생한다(String roleString) {
        assertThatThrownBy(() -> MemberRole.parse(roleString))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
