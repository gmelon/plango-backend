package dev.gmelon.plango.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EmailTokenTest {
    @ParameterizedTest
    @CsvSource({"abc123,true", "abcd123,false", "abc 123,false", "'',false"})
    void tokenValue가_일치하는지_확인한다(String givenTokenValue, boolean expectedEquals) {
        // given
        EmailToken emailToken = EmailToken.builder()
                .email("a@a.com")
                .tokenValue("abc123")
                .build();

        // when, then
        assertThat(emailToken.tokenValueEquals(givenTokenValue)).isEqualTo(expectedEquals);
    }

    @Test
    void 생성_시_authenticate는_false로_생성된다() {
        // given
        EmailToken emailToken = EmailToken.builder()
                .email("a@a.com")
                .tokenValue("abc123")
                .build();

        // when, then
        assertThat(emailToken.authenticated()).isFalse();
    }
}
