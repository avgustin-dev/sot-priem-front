package kg.sot.reception.dto;

import jakarta.validation.constraints.NotNull;
import kg.sot.reception.model.AppealStage;

public record SetAppealStageRequest(@NotNull AppealStage stage, String note) {
}
