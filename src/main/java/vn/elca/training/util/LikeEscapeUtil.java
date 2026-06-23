package vn.elca.training.util;

public final class LikeEscapeUtil {

    public static final char ESCAPE_CHAR = '\\';

    private LikeEscapeUtil() {
    }

    public static String escape(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
