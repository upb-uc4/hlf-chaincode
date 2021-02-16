package de.upb.cs.uc4.chaincode.helper;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.*;

public class DateSerializer implements JsonDeserializer<Date>, JsonSerializer<Date> {

    private static final String datePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    private static final SimpleDateFormat format = new SimpleDateFormat(datePattern);
    private static final TimeZone tz = TimeZone.getTimeZone("UTC");

    @Override
    public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        String dateString = element.getAsString();

        format.setTimeZone(tz);
        try {
            return internalDeserialize(dateString);
        } catch (ParseException exp) {
            return null;
        }
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(internalSerialize(src));
    }

    public static String internalSerialize(Date src){
        format.setTimeZone(tz);
        return src == null ? "" : format.format(src);
    }
    public static Date internalDeserialize(String src) throws ParseException {
        format.setTimeZone(tz);
        return src.isEmpty() ? null : format.parse(src);
    }
}