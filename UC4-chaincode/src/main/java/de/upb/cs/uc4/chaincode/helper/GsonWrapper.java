package de.upb.cs.uc4.chaincode.helper;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import de.upb.cs.uc4.chaincode.model.Dummy;
import de.upb.cs.uc4.chaincode.model.admission.AbstractAdmission;
import de.upb.cs.uc4.chaincode.model.admission.AdmissionType;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.io.Reader;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class GsonWrapper {

    private static final Gson cleanGson = new GsonBuilder().disableHtmlEscaping().create();
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping() // need disableHtmlEscaping to handle testCases and data
            //.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .registerTypeAdapter(
                    LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) ->{
                        try {
                            return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString());
                        } catch(Exception e){
                            return null;
                        }
                    })
            .registerTypeAdapter(
                    LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (date, typeOfSrc, context) -> {
                        return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // "YYYY-MM-DDThh:mm:ss"
                    })
            .registerTypeAdapter(Date.class, new DateSerializer())
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
            .registerTypeAdapter(
                    AbstractAdmission.class,
                    (JsonDeserializer<AbstractAdmission>) (json, type, jsonDeserializationContext) -> {
                        JsonObject wrapper = (JsonObject) json;
                        JsonElement jsonType = wrapper.get("type");
                        AdmissionType admissionType = jsonDeserializationContext.deserialize(jsonType, AdmissionType.class);
                        return jsonDeserializationContext.deserialize(json, admissionType.valueToType());
                    })
            .create();

    public static <T> T fromJson(String json, Class<T> t) throws JsonSyntaxException {
        if (t.equals(Date.class)) {
            return (T) DateSerializer.internalDeserialize(json);
        }
        return gson.fromJson(json, t);
    }

    public static <T> String toJson(T object) {
        if (object instanceof Date) {
            return DateSerializer.internalSerialize((Date) object);
        }
        return gson.toJson(object);
    }

    public static <T> T fromJson(Reader reader, Type type) {
        return gson.fromJson(reader, type);
    }

    public static <T> T fromJson(String json, Type type) {
        Type listType = new TypeToken<ArrayList<String>> () {}.getType();
        if (type.equals(listType)) {
            if (json == null || json.equals("")) {
                return cleanGson.fromJson("[]", type);
            }
        }
        if (type.equals(Date.class)) {
            return (T) DateSerializer.internalDeserialize(json);
        }
        return gson.fromJson(json, type);
    }
}
