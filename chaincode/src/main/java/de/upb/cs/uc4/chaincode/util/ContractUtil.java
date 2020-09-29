package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.model.DetailedError;
import de.upb.cs.uc4.chaincode.model.GenericError;
import de.upb.cs.uc4.chaincode.model.InvalidParameter;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;

abstract public class ContractUtil {

    public DetailedError getUnprocessableEntityError(ArrayList<InvalidParameter> invalidParams) {
        return new DetailedError()
                .type("HLUnprocessableEntity")
                .title("The following parameters do not conform to the specified format")
                .invalidParams(invalidParams);
    }

    public abstract GenericError getConflictError();

    protected GenericError getConflictError(String thing) {
        return new GenericError()
                .type("HLConflict")
                .title("There is already a " + thing + " for the given enrollmentId");
    }

    public abstract GenericError getNotFoundError();

    protected GenericError getNotFoundError(String thing) {
        return new GenericError()
                .type("HLNotFound")
                .title("There is no " + thing + " for the given enrollmentId");
    }

    public GenericError getUnprocessableLedgerStateError() {
        return new GenericError()
                .type("HLUnprocessableLedgerState")
                .title("The state on the ledger does not conform to the specified format");
    }

    public InvalidParameter getEmptyEnrollmentIdParam() {
        return new InvalidParameter()
                .name("enrollmentId")
                .reason("ID must not be empty");
    }

    public String putAndGetStringState(ChaincodeStub stub, String key, String value) {
        stub.putStringState(key,value);
        return value;
    }
}
