package de.upb.cs.uc4.chaincode.exceptions.serializable.ledgeraccess;

import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;

public class UnprocessableLedgerStateError extends LedgerAccessError {
    public UnprocessableLedgerStateError(String jsonError) {
        super(jsonError);
    }
}
