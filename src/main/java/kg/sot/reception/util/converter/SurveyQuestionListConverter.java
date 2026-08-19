package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.dto.SurveyQuestion;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

import java.util.List;

@Converter
public class SurveyQuestionListConverter extends AbstractJsonAttributeConverter<List<SurveyQuestion>> {
    public SurveyQuestionListConverter() {
        super(new TypeReference<>() {
        }, List.of());
    }
}
