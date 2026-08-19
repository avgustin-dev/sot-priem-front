package kg.sot.reception.util;

import java.security.SecureRandom;
import java.util.UUID;

public final class IdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private IdGenerator() {
    }

    public static String next(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "");
    }

    public static String appointmentCode(int year) {
        int n = 1000 + RANDOM.nextInt(9000);
        return "VS-" + year + "-" + n;
    }

    public static String pin() {
        int n = 1000 + RANDOM.nextInt(9000);
        return String.valueOf(n);
    }
}
