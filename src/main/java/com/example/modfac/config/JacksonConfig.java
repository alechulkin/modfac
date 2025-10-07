package com.example.modfac.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.text.SimpleDateFormat;

@Configuration
public class JacksonConfig {
    /**
     * Configuration class for customizing the Jackson ObjectMapper.
     * <p>
     * This class defines a Spring bean for the ObjectMapper, which is configured
     * with custom serializers, date formats, and modules to handle specific data types
     * such as Java 8 date/time types and MongoDB ObjectId.
     */
    /**
         * Creates and configures an {@link ObjectMapper} bean for JSON serialization and deserialization.
         * <p>
         * The configured {@link ObjectMapper} includes custom serializers, date formats, and modules
         * to handle specific data types such as Java 8 date/time types and MongoDB ObjectId.
         *
         * @return a configured {@link ObjectMapper} instance
         */
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(ObjectId.class, new ObjectIdSerializer());
            mapper.registerModule(module);
            mapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy"));
            mapper.registerModule(new JavaTimeModule());
            return mapper;
        }
}
