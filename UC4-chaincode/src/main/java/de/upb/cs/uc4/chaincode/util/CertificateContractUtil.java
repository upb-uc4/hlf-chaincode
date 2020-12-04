package de.upb.cs.uc4.chaincode.util;

public class CertificateContractUtil extends ContractUtil {

    public CertificateContractUtil() {
        keyPrefix = "certificate";
        thing = "certificate";
        identifier = "enrollmentId";
    }
}
