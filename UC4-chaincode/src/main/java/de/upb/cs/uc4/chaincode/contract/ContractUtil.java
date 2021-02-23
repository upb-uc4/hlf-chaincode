package de.upb.cs.uc4.chaincode.contract;

import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import de.upb.cs.uc4.chaincode.contract.operation.OperationContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.*;
import de.upb.cs.uc4.chaincode.exceptions.serializable.OperationAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ledgeraccess.LedgerStateNotFoundError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ledgeraccess.UnprocessableLedgerStateError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ValidationError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.parameter.MissingTransactionError;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import de.upb.cs.uc4.chaincode.model.OperationData;
import de.upb.cs.uc4.chaincode.model.OperationDataState;
import de.upb.cs.uc4.chaincode.model.errors.DetailedError;
import de.upb.cs.uc4.chaincode.model.errors.GenericError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.helper.AccessManager;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

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

    public GenericError getParticipationDeniedError(){
        return new GenericError()
                .type("HLParticipationDenied")
                .title("You are not allowed to participate in the given operation");
    }

    public GenericError getOperationNotPendingError(){
        return new GenericError()
                .type("HLParticipationImpossible")
                .title("The operation is not in pending state");
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

    public static GenericError getInternalError() {
        return new GenericError()
                .type("HLInternalError")
                .title("SHA-256 apparently does not exist lol...");
    }

    public GenericError getParamNumberError() {
        return new GenericError()
                .type("HLParameterNumberError")
                .title("The given number of parameters does not match the required number of parameters for the specified transaction");
    }

    public void checkMayParticipate(Context ctx, OperationData operationData) throws MissingTransactionError, OperationAccessError {
        if (!operationData.getState().equals(OperationDataState.PENDING)) {
            throw new OperationAccessError(GsonWrapper.toJson(getOperationNotPendingError()));
        }
        if(!hasRightToParticipate(ctx, operationData)) {
            throw new OperationAccessError(GsonWrapper.toJson(getParticipationDeniedError()));
        }
    }

    private boolean hasRightToParticipate(Context ctx, OperationData operationData) throws MissingTransactionError {
        String clientId = getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        List<String> clientGroups = new GroupContractUtil().getGroupNamesForUser(ctx.getStub(), clientId);
        ApprovalList requiredApprovals = AccessManager.getRequiredApprovals(operationData);
        return requiredApprovals.getUsers().contains(clientId) || requiredApprovals.getGroups().stream().anyMatch(clientGroups::contains);
    }

    public void validateApprovals(
            final Context ctx,
            String contractName,
            String transactionName,
            final String[] args) throws SerializableError {
        String jsonArgs = GsonWrapper.toJson(args);
        ApprovalList requiredApprovals =  AccessManager.getRequiredApprovals(contractName, transactionName, jsonArgs);
        if (requiredApprovals.isEmpty()) {
            return;
        }

        OperationContractUtil oUtil = new OperationContractUtil();
        OperationData operationData = oUtil.getOrInitializeOperationData(ctx, null, contractName, transactionName, jsonArgs);
        checkMayParticipate(ctx, operationData);
        String clientId = getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        List<String> clientGroups = new GroupContractUtil().getGroupNamesForUser(ctx.getStub(), clientId);
        operationData.getExistingApprovals().addUsersItem(clientId).addGroupsItems(clientGroups);
        if (!OperationContractUtil.covers(requiredApprovals, operationData.getExistingApprovals())){
            throw new ValidationError(GsonWrapper.toJson(getInsufficientApprovalsError()));
        }
    }

    public String getEnrollmentIdFromClientId(String clientId) {
        return clientId.substring(9).split(",")[0];
    }

    public String getTimestamp(ChaincodeStub stub) {
        DateTimeFormatter fm = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
        return fm.format(stub.getTxTimestamp().truncatedTo(SECONDS));
    }

    public void finishOperation(
            final ChaincodeStub stub,
            String contractName,
            String transactionName,
            final String[] args) throws SerializableError {
        String jsonArgs = GsonWrapper.toJson(args);

        OperationContractUtil oUtil = new OperationContractUtil();
        String key = OperationContractUtil.getDraftKey(contractName, transactionName, jsonArgs);
        OperationData operation;
        try{
            operation = oUtil.getState(stub, key, OperationData.class);
        } catch (Exception e) {
            return;
        }

        oUtil.putAndGetStringState(stub, key, GsonWrapper.toJson(operation.state(OperationDataState.FINISHED)));
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
