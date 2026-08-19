package kg.sot.reception.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kg.sot.reception.util.converter.IntegerListConverter;
import kg.sot.reception.util.converter.LocalDateListConverter;
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
@Table(name = "calendar_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarSettings {

    public static final int SINGLETON_ID = 1;

    @Id
    private Integer id;

    @Convert(converter = IntegerListConverter.class)
    @Column(name = "reception_weekdays", columnDefinition = "text", nullable = false)
    @Builder.Default
    private List<Integer> receptionWeekdays = new ArrayList<>();

    @Column(name = "day_start_minutes", nullable = false)
    private int dayStartMinutes;

    @Column(name = "day_end_minutes", nullable = false)
    private int dayEndMinutes;

    @Column(name = "slot_duration_minutes", nullable = false)
    private int slotDurationMinutes;

    @Column(name = "break_minutes", nullable = false)
    private int breakMinutes;

    @Column(name = "booking_horizon_days", nullable = false)
    private int bookingHorizonDays;

    @Convert(converter = LocalDateListConverter.class)
    @Column(name = "closed_dates", columnDefinition = "text", nullable = false)
    @Builder.Default
    private List<LocalDate> closedDates = new ArrayList<>();

    @Convert(converter = LocalDateListConverter.class)
    @Column(name = "extra_open_dates", columnDefinition = "text", nullable = false)
    @Builder.Default
    private List<LocalDate> extraOpenDates = new ArrayList<>();

    @Column(name = "rules_text", columnDefinition = "text")
    private String rulesText;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
