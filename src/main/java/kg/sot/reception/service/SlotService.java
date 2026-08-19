package kg.sot.reception.service;

import kg.sot.reception.dto.AvailableDatesResponse;
import kg.sot.reception.dto.CalendarSettingsDto;
import kg.sot.reception.dto.SlotDayResponse;
import kg.sot.reception.dto.TimeSlot;
import kg.sot.reception.model.Appointment;
import kg.sot.reception.repository.AppointmentRepository;
import kg.sot.reception.util.Mappers;
import kg.sot.reception.util.SlotCalculator;
import kg.sot.reception.util.TargetWindow;
import kg.sot.reception.util.TargetWindowResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SlotService {

    public static final ZoneId BISHKEK = ZoneId.of("Asia/Bishkek");

    private final CmsService cmsService;
    private final AppointmentRepository appointmentRepository;

    public SlotService(CmsService cmsService, AppointmentRepository appointmentRepository) {
        this.cmsService = cmsService;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public AvailableDatesResponse availableDates(String targetId) {
        CalendarSettingsDto calendar = cmsService.getCalendar();
        Map<String, Object> site = cmsService.getSiteContent();
        TargetWindow window = TargetWindowResolver.resolve(targetId, calendar, site);
        LocalDate today = LocalDate.now(BISHKEK);
        List<LocalDate> dates = SlotCalculator.listAvailableDates(calendar, today, window);
        return new AvailableDatesResponse(targetId, dates);
    }

    @Transactional(readOnly = true)
    public SlotDayResponse slotsForDate(LocalDate date, String targetId, String excludeAppointmentId) {
        CalendarSettingsDto calendar = cmsService.getCalendar();
        Map<String, Object> site = cmsService.getSiteContent();
        TargetWindow window = TargetWindowResolver.resolve(targetId, calendar, site);

        Set<String> booked = appointmentRepository.findActiveByDateAndTarget(date, targetId).stream()
                .filter(a -> excludeAppointmentId == null || !excludeAppointmentId.equals(a.getId()))
                .map(Appointment::getSlotStart)
                .collect(Collectors.toSet());

        ZonedDateTime now = ZonedDateTime.now(BISHKEK);
        boolean isToday = date.equals(now.toLocalDate());
        int nowMinutes = now.getHour() * 60 + now.getMinute();

        List<TimeSlot> slots = SlotCalculator.availableSlots(date, calendar, window, booked, isToday, nowMinutes);
        return new SlotDayResponse(date, targetId, slots);
    }

    @Transactional(readOnly = true)
    public boolean isSlotFree(LocalDate date, String targetId, String slotStart, String excludeAppointmentId) {
        return slotsForDate(date, targetId, excludeAppointmentId).slots().stream()
                .anyMatch(s -> s.start().equals(slotStart));
    }
}
