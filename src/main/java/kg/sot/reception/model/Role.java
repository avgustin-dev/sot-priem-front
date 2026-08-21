package kg.sot.reception.model;

public enum Role {
    reception,
    leadership,
    responsible,
    admin,
    /** Приёмный отдел: только журнал заявок — принять / отклонить / перенести. */
    intake
}
