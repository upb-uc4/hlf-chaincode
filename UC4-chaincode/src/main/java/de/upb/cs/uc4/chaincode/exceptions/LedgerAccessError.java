package de.upb.cs.uc4.chaincode.exceptions;

public abstract class LedgerAccessError extends SerializableError {

    public LedgerAccessError(String jsonError) {
        super(jsonError);
    }
}
