package kg.sot.reception.util.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import kg.sot.reception.dto.ReceptionProtocol;
import kg.sot.reception.util.AbstractJsonAttributeConverter;

@Converter
public class ReceptionProtocolConverter extends AbstractJsonAttributeConverter<ReceptionProtocol> {
    public ReceptionProtocolConverter() {
        super(new TypeReference<>() {
        }, null);
    }
}
