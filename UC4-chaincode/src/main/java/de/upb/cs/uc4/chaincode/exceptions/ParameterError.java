package de.upb.cs.uc4.chaincode.exceptions;

public class ParameterError extends SerializableError {
    public ParameterError(String jsonError) {
        super(jsonError);
    }
}
