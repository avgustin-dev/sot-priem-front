package kg.sot.reception.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kg.sot.reception.dto.Assignment;
import kg.sot.reception.dto.ControlLogEntry;
import kg.sot.reception.dto.Feedback;
import kg.sot.reception.dto.NotificationItem;
import kg.sot.reception.dto.ReceptionProtocol;
import kg.sot.reception.util.converter.AssignmentConverter;
import kg.sot.reception.util.converter.ControlLogListConverter;
import kg.sot.reception.util.converter.FeedbackConverter;
import kg.sot.reception.util.converter.NotificationListConverter;
import kg.sot.reception.util.converter.ReceptionProtocolConverter;
import kg.sot.reception.util.converter.StringListConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "appeal_card")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppealCard {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "appointment_id", nullable = false, unique = true, length = 64)
    private String appointmentId;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, length = 32)
    private String phone;

    private String email;

    @Column(nullable = false, length = 500)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppealCategory category;

    @Column(columnDefinition = "text")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AppealStage stage;

    @Convert(converter = StringListConverter.class)
    @Column(name = "previous_appeal_ids", columnDefinition = "text")
    @Builder.Default
    private List<String> previousAppealIds = new ArrayList<>();

    @Column(name = "previous_notes", columnDefinition = "text")
    private String previousNotes;

    @Column(name = "prep_notes", columnDefinition = "text")
    private String prepNotes;

    @Column(name = "prep_completed_by")
    private String prepCompletedBy;

    @Column(name = "prep_completed_at")
    private OffsetDateTime prepCompletedAt;

    @Convert(converter = ReceptionProtocolConverter.class)
    @Column(name = "reception_protocol", columnDefinition = "text")
    private ReceptionProtocol receptionProtocol;

    @Convert(converter = AssignmentConverter.class)
    @Column(columnDefinition = "text")
    private Assignment assignment;

    @Convert(converter = ControlLogListConverter.class)
    @Column(name = "control_log", columnDefinition = "text", nullable = false)
    @Builder.Default
    private List<ControlLogEntry> controlLog = new ArrayList<>();

    @Column(name = "final_answer", columnDefinition = "text")
    private String finalAnswer;

    @Column(name = "final_answer_at")
    private OffsetDateTime finalAnswerAt;

    @Convert(converter = FeedbackConverter.class)
    @Column(columnDefinition = "text")
    private Feedback feedback;

    @Convert(converter = NotificationListConverter.class)
    @Column(columnDefinition = "text", nullable = false)
    @Builder.Default
    private List<NotificationItem> notifications = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public void addNotification(NotificationItem item) {
        List<NotificationItem> next = new ArrayList<>();
        next.add(item);
        next.addAll(notifications);
        this.notifications = next;
    }

    public void addControlLog(ControlLogEntry entry) {
        List<ControlLogEntry> next = new ArrayList<>();
        next.add(entry);
        next.addAll(controlLog);
        this.controlLog = next;
    }
}
