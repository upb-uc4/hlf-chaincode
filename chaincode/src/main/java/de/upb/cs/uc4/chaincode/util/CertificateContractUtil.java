package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.model.GenericError;
import de.upb.cs.uc4.chaincode.model.InvalidParameter;

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

    public InvalidParameter getEmptyCertificateParam() {
        return new InvalidParameter()
                .name("certificate")
                .reason("Certificate must not be empty");
    }
}
