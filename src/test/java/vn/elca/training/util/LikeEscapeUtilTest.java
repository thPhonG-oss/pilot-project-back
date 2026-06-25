package vn.elca.training.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LikeEscapeUtil Unit Tests")
class LikeEscapeUtilTest {

    @Test
    @DisplayName("Should escape LIKE wildcard characters")
    void escape_shouldEscapeWildcards() {
        assertThat(LikeEscapeUtil.escape("a%b_c\\d")).isEqualTo("a\\%b\\_c\\\\d");
    }

    @Test
    @DisplayName("Should return null when input is null")
    void escape_shouldReturnNull_whenInputIsNull() {
        assertThat(LikeEscapeUtil.escape(null)).isNull();
    }
}
