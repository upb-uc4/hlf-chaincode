package de.upb.cs.uc4.chaincode.exceptions.serializable;

import de.upb.cs.uc4.chaincode.exceptions.SerializableError;

public class OperationAccessError extends SerializableError {
    public OperationAccessError(String jsonError) {
        super(jsonError);
    }
}
