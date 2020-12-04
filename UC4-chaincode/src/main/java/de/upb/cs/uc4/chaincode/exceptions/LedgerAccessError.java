package de.upb.cs.uc4.chaincode.exceptions;

public class LedgerAccessError extends Exception {
    private final String jsonError;

    public LedgerAccessError(String jsonError) {
        this.jsonError = jsonError;
    }

    public String getJsonError() {
        return jsonError;
    }
}
