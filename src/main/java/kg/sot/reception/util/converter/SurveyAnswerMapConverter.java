package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.dto.SurveyAnswerValue;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

import java.util.LinkedHashMap;
import java.util.Map;

@Converter
public class SurveyAnswerMapConverter extends AbstractJsonAttributeConverter<Map<String, SurveyAnswerValue>> {
    public SurveyAnswerMapConverter() {
        super(new TypeReference<>() {
        }, new LinkedHashMap<>());
    }
}
