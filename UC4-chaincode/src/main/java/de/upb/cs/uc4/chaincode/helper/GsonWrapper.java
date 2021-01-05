package de.upb.cs.uc4.chaincode.helper;

import com.google.gson.*;
import de.upb.cs.uc4.chaincode.model.Dummy;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.Reader;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class GsonWrapper {

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping() // need disableHtmlEscaping to handle testCases and data
            .registerTypeAdapter(
                    LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) -> {
                        try {
                            return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString());
                        } catch (DateTimeParseException e) {
                            return null;
                        }
                    })
            .registerTypeAdapter(
                    LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (date, typeOfSrc, context) -> {
                        return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // "YYYY-MM-DDThh:mm:ss"
                    })
            .registerTypeAdapter(
                    Integer.class,
                    (JsonDeserializer<Integer>) (json, type, jsonDeserializationContext) -> {
                        try {
                            return json.getAsInt();
                        } catch (RuntimeException e) {
                            return null;
                        }
                    })
            .registerTypeAdapter(
                    Dummy.class,
                    (JsonSerializer<Dummy>) (dummy, typeOfSrc, context) -> {
                        return new JsonPrimitive(dummy.getContent()); // "yyyy-mm-dd"
                    })
            .registerTypeAdapter(
                    Dummy.class,
                    (JsonDeserializer<Dummy>) (json, type, jsonDeserializationContext) -> {
                        try {
                            String s = json.toString();
                            if (s.charAt(0) == '"') {
                                s = s.substring(1, s.length() - 1);
                            }
                            return new Dummy(s);
                        } catch (RuntimeException e) {
                            return null;
                        }
                    })
            .registerTypeAdapter(
                    String.class,
                    (JsonDeserializer<String>) (json, type, jsonDeserializationContext) ->
                            Jsoup.clean(json.getAsJsonPrimitive().getAsString(), Whitelist.none()))
            .create();

    public static <T> T fromJson(String json, Class<T> t) throws JsonSyntaxException {
        return gson.fromJson(json, t);
    }

    public static <T> String toJson(T object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(Reader reader, Type type) {
        return gson.fromJson(reader, type);
    }

    public static <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }
}
