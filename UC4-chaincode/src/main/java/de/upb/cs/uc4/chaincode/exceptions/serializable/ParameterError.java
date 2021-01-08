package de.upb.cs.uc4.chaincode.exceptions.serializable;

import de.upb.cs.uc4.chaincode.exceptions.SerializableError;

public class ParameterError extends SerializableError {
    public ParameterError(String jsonError) {
        super(jsonError);
    }
}
