package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.dto.SurveyMeta;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

@Converter
public class SurveyMetaConverter extends AbstractJsonAttributeConverter<SurveyMeta> {
    public SurveyMetaConverter() {
        super(new TypeReference<>() {
        }, null);
    }
}
