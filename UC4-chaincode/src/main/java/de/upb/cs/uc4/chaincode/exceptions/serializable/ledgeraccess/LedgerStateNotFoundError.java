package de.upb.cs.uc4.chaincode.exceptions.serializable.ledgeraccess;

import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;

public class LedgerStateNotFoundError extends LedgerAccessError {
    public LedgerStateNotFoundError(String jsonError) {
        super(jsonError);
    }
}
