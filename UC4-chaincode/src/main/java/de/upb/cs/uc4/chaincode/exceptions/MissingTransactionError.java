package de.upb.cs.uc4.chaincode.exceptions;

public class MissingTransactionError extends ParameterError {

    public MissingTransactionError(String jsonError) {
        super(jsonError);
    }
}
