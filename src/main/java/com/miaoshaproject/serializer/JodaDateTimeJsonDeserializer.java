package com.miaoshaproject.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

/**
 * Author: XiangL
 * Date: 2019/6/20 13:33
 * Version 1.0
 */
public class JodaDateTimeJsonDeserializer extends JsonDeserializer<DateTime> {

    @Override
    public DateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
       String dataString = jsonParser.readValueAs(String.class);
        DateTimeFormatter dateTimeFormatter= DateTimeFormat.forPattern("yyyy-MM-DD HH:mm:ss");


        return DateTime.parse(dataString, dateTimeFormatter);
    }
}
