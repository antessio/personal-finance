package antessio.personalfinance.infrastructure.web.controller.common;

import antessio.personalfinance.common.Id;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class IdSerializer extends JsonSerializer<Id<?>> {
    @Override
    public void serialize(Id<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObject(value.getId());
    }
}