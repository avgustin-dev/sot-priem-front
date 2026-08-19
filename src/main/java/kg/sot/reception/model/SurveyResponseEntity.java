package kg.sot.reception.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kg.sot.reception.dto.SurveyAnswerValue;
import kg.sot.reception.util.converter.SurveyAnswerMapConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Table(name = "survey_response")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyResponseEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;

    @Column(name = "court_name")
    private String courtName;

    @Convert(converter = SurveyAnswerMapConverter.class)
    @Column(columnDefinition = "text", nullable = false)
    @Builder.Default
    private Map<String, SurveyAnswerValue> answers = new LinkedHashMap<>();
}
