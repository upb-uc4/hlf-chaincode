package de.upb.cs.uc4.chaincode.helper;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.*;

public class DateSerializer implements JsonDeserializer<Date>, JsonSerializer<Date> {

    public static String datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static SimpleDateFormat format = new SimpleDateFormat(datePattern);

    @Override
    public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        String dateString = element.getAsString();

        try {
            return format.parse(dateString);
        } catch (ParseException exp) {
            return null;
        }
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(format.format(src));
    }
}