package com.example.springboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Register the JavaTimeModule for JSR310 support
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        
        // Configure LocalDateTime serialization
        javaTimeModule.addSerializer(LocalDateTime.class, 
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)));
        
        mapper.registerModule(javaTimeModule);
        
        // Disable writing dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
}
