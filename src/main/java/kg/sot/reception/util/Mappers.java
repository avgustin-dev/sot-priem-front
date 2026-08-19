package kg.sot.reception.util;

import kg.sot.reception.dto.AppealCardDto;
import kg.sot.reception.dto.CalendarSettingsDto;
import kg.sot.reception.dto.PublicAppointment;
import kg.sot.reception.dto.StaffProfile;
import kg.sot.reception.dto.SurveyBundle;
import kg.sot.reception.dto.SurveyResponseDto;
import kg.sot.reception.model.AppealCard;
import kg.sot.reception.model.Appointment;
import kg.sot.reception.model.CalendarSettings;
import kg.sot.reception.model.StaffUser;
import kg.sot.reception.model.SurveyConfig;
import kg.sot.reception.model.SurveyResponseEntity;

import java.util.ArrayList;

public final class Mappers {

    private Mappers() {
    }

    public static StaffProfile toStaffProfile(StaffUser u) {
        return new StaffProfile(u.getId(), u.getLogin(), u.getFullName(), u.getRole(), u.getPosition(), u.getDepartment());
    }

    public static PublicAppointment toPublicAppointment(Appointment a) {
        return new PublicAppointment(
                a.getId(), a.getCode(), a.getFullName(), a.getPhone(), a.getEmail(),
                a.getTopic(), a.getCategory(), a.getDescription(), a.getDate(),
                a.getSlotStart(), a.getSlotEnd(), a.getStatus(), a.getTargetId(),
                new ArrayList<>(a.getCompanions()), a.getReviewNote(),
                a.getCreatedAt(), a.getUpdatedAt(), new ArrayList<>(a.getHistory())
        );
    }

    public static AppealCardDto toAppealCardDto(AppealCard c) {
        return new AppealCardDto(
                c.getId(), c.getAppointmentId(), c.getCode(), c.getFullName(), c.getPhone(), c.getEmail(),
                c.getTopic(), c.getCategory(), c.getSummary(), c.getStage(),
                new ArrayList<>(c.getPreviousAppealIds()), c.getPreviousNotes(), c.getPrepNotes(),
                c.getPrepCompletedBy(), c.getPrepCompletedAt(), c.getReceptionProtocol(), c.getAssignment(),
                new ArrayList<>(c.getControlLog()), c.getFinalAnswer(), c.getFinalAnswerAt(), c.getFeedback(),
                new ArrayList<>(c.getNotifications()), c.getCreatedAt(), c.getUpdatedAt()
        );
    }

    public static CalendarSettingsDto toCalendarSettingsDto(CalendarSettings c) {
        return new CalendarSettingsDto(
                new ArrayList<>(c.getReceptionWeekdays()), c.getDayStartMinutes(), c.getDayEndMinutes(),
                c.getSlotDurationMinutes(), c.getBreakMinutes(), c.getBookingHorizonDays(),
                new ArrayList<>(c.getClosedDates()), new ArrayList<>(c.getExtraOpenDates()), c.getRulesText()
        );
    }

    public static void applyCalendarDto(CalendarSettings entity, CalendarSettingsDto dto) {
        entity.setReceptionWeekdays(new ArrayList<>(dto.receptionWeekdays()));
        entity.setDayStartMinutes(dto.dayStartMinutes());
        entity.setDayEndMinutes(dto.dayEndMinutes());
        entity.setSlotDurationMinutes(dto.slotDurationMinutes());
        entity.setBreakMinutes(dto.breakMinutes());
        entity.setBookingHorizonDays(dto.bookingHorizonDays());
        entity.setClosedDates(new ArrayList<>(dto.closedDates()));
        entity.setExtraOpenDates(new ArrayList<>(dto.extraOpenDates()));
        entity.setRulesText(dto.rulesText());
    }

    public static SurveyBundle toSurveyBundle(SurveyConfig c) {
        return new SurveyBundle(c.getMeta(), new ArrayList<>(c.getQuestions()));
    }

    public static SurveyResponseDto toSurveyResponseDto(SurveyResponseEntity e) {
        return new SurveyResponseDto(e.getId(), e.getSubmittedAt(), e.getCourtName(), e.getAnswers());
    }
}
