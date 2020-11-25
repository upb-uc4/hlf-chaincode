package de.upb.cs.uc4.chaincode.exceptions;

public class UnprocessableLedgerStateError extends LedgerAccessError {
    public UnprocessableLedgerStateError(String jsonError) {
        super(jsonError);
    }
}
