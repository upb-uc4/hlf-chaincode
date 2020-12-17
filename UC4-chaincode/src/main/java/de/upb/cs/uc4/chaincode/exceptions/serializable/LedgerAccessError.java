package de.upb.cs.uc4.chaincode.exceptions.serializable;

import de.upb.cs.uc4.chaincode.exceptions.SerializableError;

public abstract class LedgerAccessError extends SerializableError {

    public LedgerAccessError(String jsonError) {
        super(jsonError);
    }
}
