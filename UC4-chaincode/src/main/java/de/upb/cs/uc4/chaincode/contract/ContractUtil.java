package de.upb.cs.uc4.chaincode.contract;

import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import de.upb.cs.uc4.chaincode.contract.operation.OperationContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParticipationError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ValidationError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ledgeraccess.LedgerStateNotFoundError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ledgeraccess.UnprocessableLedgerStateError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.parameter.MissingTransactionError;
import de.upb.cs.uc4.chaincode.helper.AccessManager;
import de.upb.cs.uc4.chaincode.helper.GeneralHelper;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.helper.ValidationManager;
import de.upb.cs.uc4.chaincode.model.errors.DetailedError;
import de.upb.cs.uc4.chaincode.model.errors.GenericError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.model.operation.ApprovalList;
import de.upb.cs.uc4.chaincode.model.operation.OperationData;
import de.upb.cs.uc4.chaincode.model.operation.OperationDataState;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static de.upb.cs.uc4.chaincode.helper.HyperledgerManager.getEnrollmentIdFromClientId;

abstract public class ContractUtil {

    protected String keyPrefix = "";
    protected String errorPrefix = "";
    protected String thing = "";
    protected String identifier = "";

    public DetailedError getUnprocessableEntityError(InvalidParameter invalidParam) {
        return getUnprocessableEntityError(GeneralHelper.wrapItemByList(invalidParam));
    }

    public DetailedError getUnprocessableEntityError(List<InvalidParameter> invalidParams) {
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

    public GenericError getAccessDeniedError(){
        return new GenericError()
                .type("HLAccessDenied")
                .title("You are not allowed to execute in the given transaction");
    }

    public GenericError getTransactionTimestampInvalidError(){
        return new GenericError()
                .type("HLTransactionTimestampInvalid")
                .title("The transaction you submitted contains a timestamp differing more than two minutes from the current system time");
    }

    public GenericError getOperationNotPendingError(){
        return new GenericError()
                .type("HLExecutionImpossible")
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

    public InvalidParameter getInvalidTimestampParam(String param) {
        return new InvalidParameter()
                .name(param)
                .reason("Any date must conform to the following format \"(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z\", e.g. \"2020-12-31T23:59:59.999Z\"");
    }

    public <E extends Enum<E>> InvalidParameter getInvalidEnumValue(String parameterName, Class<E> enumClass) {
        String[] possibleValues = GeneralHelper.possibleStringValues(enumClass);
        return new InvalidParameter()
                .name(parameterName)
                .reason("The " + parameterName + " has/have to be one of {" + String.join(", ", possibleValues)+ "}");
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

    public void checkMayParticipate(Context ctx, OperationData operationData) throws MissingTransactionError, LedgerAccessError, ParticipationError {
        if(!hasRightToParticipate(ctx, operationData)) {
            throw new ParticipationError(GsonWrapper.toJson(getAccessDeniedError()));
        }
        if (!operationData.getState().equals(OperationDataState.PENDING)) {
            throw new ParticipationError(GsonWrapper.toJson(getOperationNotPendingError()));
        }
    }

    private boolean hasRightToParticipate(Context ctx, OperationData operationData) throws MissingTransactionError, LedgerAccessError {
        String clientId = getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        List<String> clientGroups = new GroupContractUtil().getGroupNamesForUser(ctx.getStub(), clientId);
        ApprovalList requiredApprovals = AccessManager.getRequiredApprovals(ctx, operationData);
        return requiredApprovals.getUsers().contains(clientId) || requiredApprovals.getGroups().stream().anyMatch(clientGroups::contains);
    }

    public void validateApprovals(
            final Context ctx,
            String contractName,
            String transactionName,
            final String[] args) throws SerializableError {
        String jsonArgs = GsonWrapper.toJson(args);
        ApprovalList requiredApprovals =  AccessManager.getRequiredApprovals(ctx, contractName, transactionName, jsonArgs);
        if (requiredApprovals.isEmpty()) {
            return;
        }

        OperationContractUtil oUtil = new OperationContractUtil();
        OperationData operationData = oUtil.getOrInitializeOperationData(ctx, null, contractName, transactionName, jsonArgs);
        checkMayParticipate(ctx, operationData);
        String clientId = getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        List<String> clientGroups = new GroupContractUtil().getGroupNamesForUser(ctx.getStub(), clientId);
        operationData.getExistingApprovals().addUsersItem(clientId).addGroupsItems(clientGroups);
        if (!ValidationManager.covers(requiredApprovals, operationData.getExistingApprovals())){
            throw new ValidationError(GsonWrapper.toJson(getInsufficientApprovalsError()));
        }
    }

    public void validateCurrentUserHasAttributes(Context ctx, List<String> attributes) throws SerializableError {
        for (String attribute : attributes){
            boolean userHasAttribute = ctx.getClientIdentity().assertAttributeValue(attribute, "true");
            if(!userHasAttribute){
                // TODO: better Error?
                throw new ValidationError(GsonWrapper.toJson(getInsufficientApprovalsError()));
            }
        }
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

    public boolean keyExists(ChaincodeStub stub, String key) {
        String result = getStringState(stub, key);
        return result != null && !result.equals("");
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
        if (GeneralHelper.valueUnset(jsonValue)) {
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
        if (GeneralHelper.valueUnset(jsonValue)) {
            throw new LedgerStateNotFoundError(GsonWrapper.toJson(getNotFoundError()));
        }

        deleteStringState(stub, key);
    }

    public <T> List<T> getAllStates(ChaincodeStub stub, Class<T> c) {
        QueryResultsIterator<KeyValue> qrIterator;
        qrIterator = getAllRawStates(stub);
        List<T> resultItems = new ArrayList<>();
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

    public void checkTimestamp(Context ctx) throws ValidationError {
        Instant timestamp = ctx.getStub().getTxTimestamp();
        Instant current = Instant.now();

        long diff = timestamp.getEpochSecond() - current.getEpochSecond();
        diff = diff < 0 ? (-diff) : diff;

        if(diff > 120){
            throw new ValidationError(GsonWrapper.toJson(getTransactionTimestampInvalidError()));
        }
    }

    public void validateTransaction(Context ctx,
                                    String contractName,
                                    String transactionName,
                                    String[] args) throws SerializableError {
        checkTimestamp(ctx);
        ValidationManager.validateParams(ctx, contractName, transactionName, GsonWrapper.toJson(args));
        validateApprovals(ctx, contractName, transactionName, args);
        finishOperation(ctx.getStub(), contractName, transactionName, args);
    }
}
