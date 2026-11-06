package anilink.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    public static boolean verify(String plain, String hashed) {
        if (plain == null || hashed == null || hashed.isEmpty()) {
            return false;
        }
        String normalized = hashed;
        if (normalized.startsWith("$2y$")) {
            normalized = "$2a$" + normalized.substring(4);
        }
        try {
            return BCrypt.checkpw(plain, normalized);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
