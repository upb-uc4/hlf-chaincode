package de.upb.cs.uc4.chaincode.exceptions;

public class SerializableError extends Exception {
    private final String jsonError;

    public SerializableError(String jsonError) {
        this.jsonError = jsonError;
    }

    public String getJsonError() {
        return jsonError;
    }
}
