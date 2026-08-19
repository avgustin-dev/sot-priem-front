package kg.sot.reception.dto;

import jakarta.validation.constraints.NotBlank;

public record RecoverCodesRequest(@NotBlank String phone) {
}
