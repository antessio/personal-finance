package antessio.personalfinance.infrastructure.web.config;

import antessio.personalfinance.common.Id;
import antessio.personalfinance.infrastructure.web.customserializer.IdSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Module idModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Id.class, new IdSerializer());
        return module;
    }
}
