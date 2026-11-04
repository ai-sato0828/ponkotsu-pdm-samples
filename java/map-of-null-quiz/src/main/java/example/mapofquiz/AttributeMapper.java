package example.mapofquiz;

import java.util.HashMap;
import java.util.Map;

final class AttributeMapper {
    Map<String, String> toAttributes(UserForm form) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("department", form.getDepartment());

        String extension = form.getExtension();
        if (extension != null) {
            attributes.put("extension", extension);
        }
        return Map.copyOf(attributes);
    }
}
