package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.model.errors.GenericError;

public class CertificateContractUtil extends ContractUtil {
    private final String thing = "certificate";
    private final String identifier = "enrollmentId";

    public CertificateContractUtil() {
        keyPrefix = "certificate";
    }

    @Override
    public GenericError getConflictError() {
        return super.getConflictError(thing, identifier);
    }

    @Override
    public GenericError getNotFoundError() {
        return super.getNotFoundError(thing, identifier);
    }
}
