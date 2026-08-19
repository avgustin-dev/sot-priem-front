package kg.sot.reception.util;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;

/** Общая база для JPA-конвертеров "объект/список ↔ JSON-текст в колонке". */
public abstract class AbstractJsonAttributeConverter<T> implements AttributeConverter<T, String> {

    private final TypeReference<T> typeReference;
    private final T emptyValue;

    protected AbstractJsonAttributeConverter(TypeReference<T> typeReference, T emptyValue) {
        this.typeReference = typeReference;
        this.emptyValue = emptyValue;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        try {
            return JsonMapper.INSTANCE.writeValueAsString(attribute == null ? emptyValue : attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось сериализовать значение в JSON", e);
        }
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return emptyValue;
        }
        try {
            return JsonMapper.INSTANCE.readValue(dbData, typeReference);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось разобрать JSON из колонки", e);
        }
    }
}
