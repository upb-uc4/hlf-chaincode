package de.upb.cs.uc4.chaincode.exceptions;

public class ParameterError extends Exception {
    private final String jsonError;

    public ParameterError(String jsonError) {
        this.jsonError = jsonError;
    }

    public String getJsonError() {
        return jsonError;
    }
}
