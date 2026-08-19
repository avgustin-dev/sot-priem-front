package kg.sot.reception.dto;

import java.util.List;

public record RefusalMessage(
        String greetingRu,
        String greetingKy,
        List<String> bodyRu,
        List<String> bodyKy,
        String closingRu,
        String closingKy
) {
}
