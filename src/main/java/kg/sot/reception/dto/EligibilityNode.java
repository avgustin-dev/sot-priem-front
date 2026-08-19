package kg.sot.reception.dto;

import kg.sot.reception.model.AppealCategory;

import java.util.List;

public record EligibilityNode(
        String id,
        String labelRu,
        String labelKy,
        List<EligibilityNode> children,
        Boolean allowed,
        AppealCategory category,
        String topicRu,
        String topicKy,
        RefusalMessage refusal
) {
}
