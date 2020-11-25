package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.model.*;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;

abstract public class ContractUtil {

    protected String keyPrefix = "";

    public DetailedError getUnprocessableEntityError(ArrayList<InvalidParameter> invalidParams) {
        return new DetailedError()
                .type("HLUnprocessableEntity")
                .title("The following parameters do not conform to the specified format")
                .invalidParams(invalidParams);
    }

    public SemanticError getInvalidActionError(ArrayList<ValidationRule> ruleViolations) {
        return new SemanticError()
                .type("HLSemanticError")
                .title("The transaction could not be performed. The following rules were violated:")
                .validationRules(ruleViolations);
    }

    public DetailedError getUnprocessableEntityError(InvalidParameter invalidParam) {
        return getUnprocessableEntityError(getArrayList(invalidParam));
    }

    public GenericError getConflictError() {
        return null;
    }

    protected GenericError getConflictError(String thing, String identifier) {
        String article = "aeio".contains(Character.toString(thing.charAt(0)).toLowerCase()) ? "an" : "a";
        return new GenericError()
                .type("HLConflict")
                .title("There is already " + article + " " + thing + " for the given " + identifier);
    }

    public abstract GenericError getNotFoundError();

    protected GenericError getNotFoundError(String thing, String identifier) {
        return new GenericError()
                .type("HLNotFound")
                .title("There is no " + thing + " for the given " + identifier);
    }

    public GenericError getUnprocessableLedgerStateError() {
        return new GenericError()
                .type("HLUnprocessableLedgerState")
                .title("The state on the ledger does not conform to the specified format");
    }

    public GenericError getInsufficientApprovalsError() {
        return new GenericError()
                .type("HLInsufficientApprovals")
                .title("The approvals present on the ledger do not suffice to execute this transaction");
    }

    public InvalidParameter getEmptyParameterError(String parameterName) {
        return new InvalidParameter()
                .name(parameterName)
                .reason("The given parameter must not be empty.");
    }

    public InvalidParameter getEmptyEnrollmentIdParam() {
        return getEmptyEnrollmentIdParam("");
    }

    public InvalidParameter getEmptyEnrollmentIdParam(String prefix) {
        return getEmptyParameterError(prefix + "enrollmentId");
    }

    public InvalidParameter getEmptyAdmissionIdParam() {
        return getEmptyParameterError("admissionId");
    }

    public boolean validateApprovals(
            final Context ctx,
            final List<String> requiredIds,
            final List<String> requiredTypes,
            String contractName,
            String transactionName,
            final List<String> args) {
        // replace approval checking until we get third Party signing fixed.
        return true;
        /*
        ChaincodeStub stub = ctx.getStub();
        ApprovalContractUtil aUtil = new ApprovalContractUtil();
        ArrayList<Approval> approvals;
        String key;
        try {
            key = aUtil.getDraftKey(contractName, transactionName, args.toArray(new String[0]));
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
        try{
            approvals = aUtil.getState(stub, key);
        } catch(LedgerAccessError e) {
            return false;
        }
        return ApprovalContractUtil.covers(approvals, requiredIds, requiredTypes);
        */
    }

    public String putAndGetStringState(ChaincodeStub stub, String key, String value) {
        String fullKey = stub.createCompositeKey(keyPrefix, key).toString();
        stub.putStringState(fullKey,value);
        return value;
    }

    public String getStringState(ChaincodeStub stub, String key) {
        String fullKey = stub.createCompositeKey(keyPrefix, key).toString();
        return stub.getStringState(fullKey);
    }

    public QueryResultsIterator<KeyValue> getAllRawStates(ChaincodeStub stub) {
        CompositeKey key = stub.createCompositeKey(keyPrefix);
        return stub.getStateByPartialCompositeKey(key);
    }

    public ArrayList<InvalidParameter> getArrayList(InvalidParameter invalidParam) {
        return new ArrayList<InvalidParameter>() {{
            add(invalidParam);
        }};
    }

    public boolean keyExists(ChaincodeStub stub, String key) {
        String result = getStringState(stub, key);
        return result != null && !result.equals("");
    }

    public boolean valueUnset(String value) {
        return value == null || value.equals("");
    }

    public <T> boolean valueUnset(List<T> value) {
        return value == null || value.isEmpty();
    }
}
