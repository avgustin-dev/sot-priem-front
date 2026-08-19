package kg.sot.reception.util;

public final class PhoneUtil {

    private PhoneUtil() {
    }

    public static String normalize(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[\\s()\\-]", "");
    }

    public static String last9(String phone) {
        String n = normalize(phone);
        return n.length() <= 9 ? n : n.substring(n.length() - 9);
    }

    public static boolean matchCitizen(String fullNameA, String phoneA, String fullNameB, String phoneB) {
        String p1 = normalize(phoneA);
        String p2 = normalize(phoneB);
        if (!p1.isEmpty() && !p2.isEmpty() && p1.equals(p2)) return true;
        boolean sameName = fullNameA != null && fullNameB != null
                && fullNameA.trim().equalsIgnoreCase(fullNameB.trim());
        return sameName && last9(phoneA).equals(last9(phoneB));
    }
}
