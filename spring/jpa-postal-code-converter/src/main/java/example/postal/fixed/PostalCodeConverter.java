package example.postal.fixed;

import example.postal.PostalCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PostalCodeConverter implements AttributeConverter<PostalCode, String> {
    @Override
    public String convertToDatabaseColumn(PostalCode attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public PostalCode convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new PostalCode(dbData);
    }
}
