package de.upb.cs.uc4.chaincode;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import de.upb.cs.uc4.chaincode.model.Dummy;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeParseException;

import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;

public class GsonWrapper {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(
                    LocalDate.class,
                    new JsonDeserializer<LocalDate>() {
                        @Override
                        public LocalDate deserialize(
                                JsonElement json,
                                Type type,
                                JsonDeserializationContext jsonDeserializationContext
                        ) throws JsonParseException {
                            try {
                                return LocalDate.parse(json.getAsJsonPrimitive().getAsString());
                            } catch (DateTimeParseException e) {
                                return null;
                            }
                        }
                    })
            .registerTypeAdapter(
                    LocalDate.class,
                    new JsonSerializer<LocalDate>() {
                        @Override
                        public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
                            return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE)); // "yyyy-mm-dd"
                        }
                    })
            .registerTypeAdapter(
                    Integer.class,
                    new JsonDeserializer<Integer>() {
                        @Override
                        public Integer deserialize(
                                JsonElement json,
                                Type type,
                                JsonDeserializationContext jsonDeserializationContext
                        ) throws JsonParseException {
                            try {
                                return json.getAsInt();
                            } catch (RuntimeException e) {
                                return null;
                            }
                        }
                    })
            .registerTypeAdapter(
                    Dummy.class,
                    new JsonSerializer<Dummy>() {
                        @Override
                        public JsonElement serialize(Dummy dummy, Type typeOfSrc, JsonSerializationContext context) {
                            return new JsonPrimitive(dummy.getContent()); // "yyyy-mm-dd"
                        }
                    })
            .registerTypeAdapter(
                    Dummy.class,
                    new JsonDeserializer<Dummy>() {
                        @Override
                        public Dummy deserialize(
                                JsonElement json,
                                Type type,
                                JsonDeserializationContext jsonDeserializationContext
                        ) throws JsonParseException {
                            try {
                                String s = json.toString();
                                if (s.charAt(0) == '"') {
                                    s = s.substring(1, s.length()-1);
                                }
                                return new Dummy(s);
                            } catch (RuntimeException e) {
                                return null;
                            }
                        }
                    })
            .registerTypeAdapter(
                    String.class,
                    new JsonDeserializer<String>() {
                        @Override
                        public String deserialize(
                                JsonElement json,
                                Type type,
                                JsonDeserializationContext jsonDeserializationContext
                        ) throws JsonParseException {
                            return Jsoup.clean(json.getAsJsonPrimitive().getAsString(), Whitelist.none());
                        }
                    })
            .create();

    public <T> T fromJson(String json, Class<T> t) throws JsonSyntaxException {
        return gson.fromJson(json, t);
    }

    public <T> String toJson(T object) {
        return gson.toJson(object);
    }

    public <T> T fromJson(Reader reader, Class<T> t) {
        return gson.fromJson(reader, t);
    }

    public <T> T fromJson(Reader reader, Type type) {
        return gson.fromJson(reader, type);
    }
}
