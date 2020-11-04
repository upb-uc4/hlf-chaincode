package de.upb.cs.uc4.chaincode.error;

public class LedgerStateNotFoundError extends LedgerAccessError {
    public LedgerStateNotFoundError(String jsonError) {
        super(jsonError);
    }
}
