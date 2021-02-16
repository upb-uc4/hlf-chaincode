package de.upb.cs.uc4.chaincode.helper;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;

import com.google.gson.*;

public class InstantAdapter implements JsonDeserializer<Instant>, JsonSerializer<Instant> {

    private static final TimeZone tz = TimeZone.getTimeZone("UTC");

    @Override
    public Instant deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        String dateString = element.getAsString();
        return internalDeserialize(dateString);
    }

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(internalSerialize(src));
    }

    public static String internalSerialize(Instant src){
        return DateTimeFormatter.ISO_INSTANT.format(src);
    }

    public static Instant internalDeserialize(String src) {
        try {
            return Instant.parse(src);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}