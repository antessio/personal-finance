package antessio.personalfinance.infrastructure.web.customserializer;

import antessio.personalfinance.common.Id;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class IdSerializer extends JsonSerializer<Id> {

    @Override
    public void serialize(Id id, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (id == null) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeString(id.getId().toString());
        }
    }
}
