package kg.sot.reception.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kg.sot.reception.dto.Companion;
import kg.sot.reception.dto.HistoryItem;
import kg.sot.reception.util.converter.CompanionListConverter;
import kg.sot.reception.util.converter.HistoryListConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, length = 32)
    private String phone;

    private String email;

    @Column(name = "pin_hash", nullable = false)
    private String pinHash;

    @Column(nullable = false, length = 500)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppealCategory category;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "appt_date", nullable = false)
    private LocalDate date;

    @Column(name = "slot_start", nullable = false, length = 5)
    private String slotStart;

    @Column(name = "slot_end", nullable = false, length = 5)
    private String slotEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppointmentStatus status;

    @Column(name = "target_id", nullable = false, length = 64)
    private String targetId;

    @Convert(converter = CompanionListConverter.class)
    @Column(columnDefinition = "text")
    @Builder.Default
    private List<Companion> companions = new ArrayList<>();

    @Column(name = "review_note", columnDefinition = "text")
    private String reviewNote;

    @Convert(converter = HistoryListConverter.class)
    @Column(columnDefinition = "text", nullable = false)
    @Builder.Default
    private List<HistoryItem> history = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public void addHistory(HistoryItem item) {
        List<HistoryItem> next = new ArrayList<>(history);
        next.add(item);
        this.history = next;
    }
}
