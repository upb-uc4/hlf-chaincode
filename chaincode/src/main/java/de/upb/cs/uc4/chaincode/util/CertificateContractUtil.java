package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.model.GenericError;
import de.upb.cs.uc4.chaincode.model.InvalidParameter;

public class CertificateContractUtil extends ContractUtil {

    private final String thing = "certificate";

    @Override
    public GenericError getConflictError() {
        return super.getConflictError(thing);
    }

    @Override
    public GenericError getNotFoundError() {
        return super.getNotFoundError(thing);
    }

    public InvalidParameter getEmptyCertificateParam() {
        return new InvalidParameter()
                .name("certificate")
                .reason("Certificate must not be empty");
    }
}
