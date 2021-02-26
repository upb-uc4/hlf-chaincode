package de.upb.cs.uc4.chaincode.helper;

import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 *      Helper for everything
 */
public class GeneralHelper {

    /** gets the possible values of the enum as strings.
     *
     * @param c class of the Enum
     * @param <E> Type of the enum
     * @return returns the possible values of the enum as strings.
     */
    public static <E extends Enum<E>> String[] possibleStringValues(Class<E> c){
        return Arrays.stream((c.getEnumConstants()))
                .map(GeneralHelper::enumValueAsString)
                .toArray(String[]::new);
    }

    public static <E extends Enum<E>> String enumValueAsString(E enumValue) {
        return GeneralHelper.removeEncapsulatingQuotes(GsonWrapper.toJson(enumValue));
    }

    private static String removeEncapsulatingQuotes(String json) {
        return json.replaceAll("\"", "");
    }

    public static boolean valueUnset(String value) {
        return valueUnset((Object) value) || value.equals("");
    }

    public static boolean valueUnset(Object value) {
        return value == null;
    }

    public static <T> boolean valueUnset(List<T> value) {
        return valueUnset((Object) value) || value.isEmpty();
    }

    public static <T> List<T> wrapItemByList(T item) {
        return new ArrayList<T>() {{
            add(item);
        }};
    }

    public static String hashAndEncodeBase64url(String all) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(all.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getUrlEncoder().withoutPadding().encode(bytes));
    }
}
