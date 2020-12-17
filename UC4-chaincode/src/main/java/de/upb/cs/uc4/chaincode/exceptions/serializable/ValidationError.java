package de.upb.cs.uc4.chaincode.exceptions.serializable;

import de.upb.cs.uc4.chaincode.exceptions.SerializableError;

public class ValidationError extends SerializableError {

    public ValidationError(String jsonError) {
        super(jsonError);
    }
}
