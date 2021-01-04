package de.upb.cs.uc4.chaincode.contract;

import de.upb.cs.uc4.chaincode.contract.approval.ApprovalContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.*;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ledgeraccess.LedgerStateNotFoundError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ledgeraccess.UnprocessableLedgerStateError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ValidationError;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import de.upb.cs.uc4.chaincode.model.errors.DetailedError;
import de.upb.cs.uc4.chaincode.model.errors.GenericError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.helper.AccessManager;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

abstract public class ContractUtil {

    protected String keyPrefix = "";
    protected String errorPrefix = "";
    protected String thing = "";
    protected String identifier = "";

    public DetailedError getUnprocessableEntityError(InvalidParameter invalidParam) {
        return getUnprocessableEntityError(getArrayList(invalidParam));
    }

    public DetailedError getUnprocessableEntityError(ArrayList<InvalidParameter> invalidParams) {
        return new DetailedError()
                .type("HLUnprocessableEntity")
                .title("The following parameters do not conform to the specified format")
                .invalidParams(invalidParams);
    }

    public GenericError getConflictError() {
        return getConflictError(thing, identifier);
    }

    protected GenericError getConflictError(String thing, String identifier) {
        String article = "aeio".contains(Character.toString(thing.charAt(0)).toLowerCase()) ? "an" : "a";
        return new GenericError()
                .type("HLConflict")
                .title("There is already " + article + " " + thing + " for the given " + identifier);
    }

    public GenericError getNotFoundError() {
        return getNotFoundError(thing, identifier);
    }

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

    public InvalidParameter getUnparsableParam(String parameterName) {
        return new InvalidParameter()
                .name(parameterName)
                .reason("The given parameter cannot be parsed from json");
    }

    public InvalidParameter getEmptyInvalidParameter(String parameterName) {
        return new InvalidParameter()
                .name(parameterName)
                .reason("The given parameter must not be empty");
    }

    public InvalidParameter getEmptyEnrollmentIdParam() {
        return getEmptyEnrollmentIdParam("");
    }

    public InvalidParameter getEmptyEnrollmentIdParam(String prefix) {
        return getEmptyInvalidParameter(prefix + "enrollmentId");
    }

    public GenericError getInternalError() {
        return new GenericError()
                .type("HLInternalError")
                .title("SHA-256 apparently does not exist lol...");
    }


    public void validateApprovals(
            final ChaincodeStub stub,
            String contractName,
            String transactionName,
            final List<String> args) throws SerializableError {
        String jsonArgs = GsonWrapper.toJson(args);
        ApprovalList requiredApprovals =  AccessManager.getRequiredApprovals(contractName, transactionName, jsonArgs);
        if (requiredApprovals.isEmpty()) {
            return;
        }

        ApprovalContractUtil aUtil = new ApprovalContractUtil();
        String key;
        try {
            key = aUtil.getDraftKey(contractName, transactionName, jsonArgs);
        } catch (NoSuchAlgorithmException e) {
            throw new ValidationError(GsonWrapper.toJson(getInternalError()));
        }
        ApprovalList approvals = aUtil.getState(stub, key, ApprovalList.class);
        if (!ApprovalContractUtil.covers(requiredApprovals, approvals)) {
            throw new ValidationError(GsonWrapper.toJson(getInsufficientApprovalsError()));
        }

    }

    public String putAndGetStringState(ChaincodeStub stub, String key, String value) {
        String fullKey = stub.createCompositeKey(keyPrefix, key).toString();
        stub.putStringState(fullKey, value);
        return value;
    }

    public String getStringState(ChaincodeStub stub, String key) {
        String fullKey = stub.createCompositeKey(keyPrefix, key).toString();
        return stub.getStringState(fullKey);
    }

    public void deleteStringState(ChaincodeStub stub, String key) {
        String fullKey = stub.createCompositeKey(keyPrefix, key).toString();
        stub.delState(fullKey);
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
        return valueUnset((Object) value) || value.equals("");
    }

    public boolean valueUnset(Object value) {
        return value == null;
    }

    public <T> boolean valueUnset(List<T> value) {
        return valueUnset((Object) value) || value.isEmpty();
    }


    public <T> T getState(ChaincodeStub stub, String key, Class<T> c) throws LedgerAccessError {
        // read key
        String jsonValue = getJsonState(stub, key);

        // convert type with GSON
        return ledgerJsonToType(jsonValue, c);
    }

    private String getJsonState(ChaincodeStub stub, String key) throws LedgerAccessError {
        // read key
        String jsonValue = getStringState(stub, key);
        if (valueUnset(jsonValue)) {
            throw new LedgerStateNotFoundError(GsonWrapper.toJson(getNotFoundError()));
        }

        return jsonValue;
    }

    private <T> T ledgerJsonToType(String jsonValue, Class<T> c) throws UnprocessableLedgerStateError {
        T dataItem;
        try {
            dataItem = GsonWrapper.fromJson(jsonValue, c);
        } catch (Exception e) {
            throw new UnprocessableLedgerStateError(GsonWrapper.toJson(getUnprocessableLedgerStateError()));
        }
        return dataItem;
    }

    public void delState(ChaincodeStub stub, String key) throws LedgerAccessError {
        String jsonValue = getStringState(stub, key);
        if (valueUnset(jsonValue)) {
            throw new LedgerStateNotFoundError(GsonWrapper.toJson(getNotFoundError()));
        }

        deleteStringState(stub, key);
    }

    public <T> ArrayList<T> getAllStates(ChaincodeStub stub, Class<T> c) {
        QueryResultsIterator<KeyValue> qrIterator;
        qrIterator = getAllRawStates(stub);
        ArrayList<T> resultItems = new ArrayList<>();
        for (KeyValue item : qrIterator) {
            String jsonValue = item.getStringValue();
            try {
                T dataObject = ledgerJsonToType(jsonValue, c);
                resultItems.add(dataObject);
            } catch (UnprocessableLedgerStateError unprocessableLedgerStateError) {
                // ignore errors, just get all valid states
            }
        }
        return resultItems;
    }
}
