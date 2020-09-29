package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.model.GenericError;

public class MatriculationDataContractUtil extends ContractUtil {

    private final String thing = "MatriculationData";

    @Override
    public GenericError getConflictError() {
        return super.getConflictError(thing);
    }

    @Override
    public GenericError getNotFoundError() {
        return super.getNotFoundError(thing);
    }
}
