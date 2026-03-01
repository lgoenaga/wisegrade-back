package com.wisegrade.exam.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RespuestaCorrectaConverter implements AttributeConverter<RespuestaCorrecta, String> {

    @Override
    public String convertToDatabaseColumn(RespuestaCorrecta attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public RespuestaCorrecta convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return RespuestaCorrecta.valueOf(dbData.trim().toUpperCase());
    }
}
