package de.upb.cs.uc4.chaincode.exceptions.serializable.parameter;

import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;

public class MissingTransactionError extends ParameterError {

    public MissingTransactionError(String jsonError) {
        super(jsonError);
    }
}
