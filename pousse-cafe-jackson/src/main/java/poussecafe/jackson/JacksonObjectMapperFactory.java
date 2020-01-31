package poussecafe.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import poussecafe.domain.Service;

public class JacksonObjectMapperFactory implements Service {

    public ObjectMapper buildMapper() {
        return staticBuildMapper();
    }

    public static ObjectMapper staticBuildMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mapper.setConfig(mapper.getSerializationConfig()
                .without(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        mapper.setConfig(mapper.getSerializationConfig()
                .with(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS));
        mapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
        mapper.setConfig(mapper.getDeserializationConfig()
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));

        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), DefaultTyping.NON_FINAL, As.PROPERTY);
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        return mapper;
    }
}
