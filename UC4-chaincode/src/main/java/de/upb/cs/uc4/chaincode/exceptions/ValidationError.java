package de.upb.cs.uc4.chaincode.exceptions;

public class ValidationError extends SerializableError {

    public ValidationError(String jsonError) {
        super(jsonError);
    }
}
