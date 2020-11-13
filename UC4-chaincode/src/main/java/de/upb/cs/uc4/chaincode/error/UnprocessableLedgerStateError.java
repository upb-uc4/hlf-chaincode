package de.upb.cs.uc4.chaincode.error;

public class UnprocessableLedgerStateError extends LedgerAccessError {
    public UnprocessableLedgerStateError(String jsonError) {
        super(jsonError);
    }
}
