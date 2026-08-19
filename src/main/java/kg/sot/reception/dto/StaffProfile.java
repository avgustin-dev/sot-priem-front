package kg.sot.reception.dto;

import kg.sot.reception.model.Role;

public record StaffProfile(
        String id,
        String login,
        String fullName,
        Role role,
        String position,
        String department
) {
}
