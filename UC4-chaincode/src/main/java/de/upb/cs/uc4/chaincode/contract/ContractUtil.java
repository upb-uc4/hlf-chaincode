package de.upb.cs.uc4.chaincode.contract;

import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContractUtil;
import de.upb.cs.uc4.chaincode.contract.operation.OperationContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.*;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ledgeraccess.LedgerStateNotFoundError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ledgeraccess.UnprocessableLedgerStateError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ValidationError;
import de.upb.cs.uc4.chaincode.model.*;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

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

    public InvalidParameter getInvalidEnumValue(String parameterName, String[] possibleValues) {
        return new InvalidParameter()
                .name(parameterName)
                .reason("The " + parameterName + " has/have to be one of {" + String.join(", ", possibleValues) + "}");
    }

    public GenericError getInternalError() {
        return new GenericError()
                .type("HLInternalError")
                .title("SHA-256 apparently does not exist lol...");
    }

    public GenericError getParamNumberError() {
        return new GenericError()
                .type("HLParameterNumberError")
                .title("The given number of parameters does not match the required number of parameters for the specified transaction");
    }

    public void validateApprovals(
            final Context ctx,
            String contractName,
            String transactionName,
            final String[] args) throws SerializableError {
        ChaincodeStub stub = ctx.getStub();
        String jsonArgs = GsonWrapper.toJson(args);
        ApprovalList requiredApprovals =  AccessManager.getRequiredApprovals(ctx, contractName, transactionName, jsonArgs);
        if (requiredApprovals.isEmpty()) {
            return;
        }

        OperationContractUtil oUtil = new OperationContractUtil();
        String key;
        try {
            key = OperationContractUtil.getDraftKey(contractName, transactionName, jsonArgs);
        } catch (NoSuchAlgorithmException e) {
            throw new ValidationError(GsonWrapper.toJson(getInternalError()));
        }
        ApprovalList approvals;
        OperationDataState operationState;
        try{
            OperationData operation = oUtil.getState(stub, key, OperationData.class);
            approvals = operation.getExistingApprovals();
            operationState = operation.getState();
        } catch (Exception e) {
            approvals = new ApprovalList();
            operationState = OperationDataState.PENDING;
        }
        String clientId = getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        List<String> clientGroups = new GroupContractUtil().getGroupNamesForUser(ctx.getStub(), clientId);
        approvals.addUsersItem(clientId);
        approvals.addGroupsItems(clientGroups);

        if(operationState != OperationDataState.PENDING || !OperationContractUtil.covers(requiredApprovals, approvals)){
            throw new ValidationError(GsonWrapper.toJson(getInsufficientApprovalsError()));
        }
    }

    public void validateAttributes(Context ctx, List<String> attributes) throws SerializableError {
        for (String attribute : attributes){
            boolean userIsSysAdmin = ctx.getClientIdentity().assertAttributeValue(attribute, "true");
            if(!userIsSysAdmin){
                // TODO: better Error?
                throw new ValidationError(GsonWrapper.toJson(getInsufficientApprovalsError()));
            }
        }
    }

    public String getEnrollmentIdFromClientId(String clientId) {
        return clientId.substring(9).split(",")[0];
    }


    public void finishOperation(
            final ChaincodeStub stub,
            String contractName,
            String transactionName,
            final String[] args) throws SerializableError {
        String jsonArgs = GsonWrapper.toJson(args);

        OperationContractUtil oUtil = new OperationContractUtil();
        String key;
        try {
            key = OperationContractUtil.getDraftKey(contractName, transactionName, jsonArgs);
        } catch (NoSuchAlgorithmException e) {
            throw new ValidationError(GsonWrapper.toJson(getInternalError()));
        }
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

    public static String hashAndEncodeBase64url(String all) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(all.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getUrlEncoder().withoutPadding().encode(bytes));
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

    public <T> List<T> getAllStates(ChaincodeStub stub, Class<T> c) {
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

    public boolean checkModuleAvailable(ChaincodeStub stub, String enrollmentId, String moduleId) {
        ExaminationRegulationContractUtil erUtil = new ExaminationRegulationContractUtil();
        MatriculationDataContractUtil matUtil = new MatriculationDataContractUtil();

        try {
            MatriculationData matriculationData = matUtil.getState(stub, enrollmentId, MatriculationData.class);
            List<SubjectMatriculation> matriculations = matriculationData.getMatriculationStatus();
            for (SubjectMatriculation matriculation : matriculations) {
                String examinationRegulationIdentifier = matriculation.getFieldOfStudy();
                ExaminationRegulation examinationRegulation = erUtil.getState(stub, examinationRegulationIdentifier, ExaminationRegulation.class);
                List<ExaminationRegulationModule> modules = examinationRegulation.getModules();
                for (ExaminationRegulationModule module : modules) {
                    if (module.getId().equals(moduleId)) {
                        return true;
                    }
                }
            }
        } catch (LedgerAccessError e) {
            return false;
        }
        return false;
    }
}
