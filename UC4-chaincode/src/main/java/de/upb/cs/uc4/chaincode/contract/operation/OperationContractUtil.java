package de.upb.cs.uc4.chaincode.contract.operation;

import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.OperationAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.parameter.MissingTransactionError;
import de.upb.cs.uc4.chaincode.helper.AccessManager;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import de.upb.cs.uc4.chaincode.model.OperationData;
import de.upb.cs.uc4.chaincode.model.OperationDataState;
import de.upb.cs.uc4.chaincode.model.TransactionInfo;
import de.upb.cs.uc4.chaincode.model.errors.DetailedError;
import de.upb.cs.uc4.chaincode.model.errors.GenericError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class OperationContractUtil extends ContractUtil {
    private static final String HASH_DELIMITER = ":";
    private static final GroupContractUtil gUtil = new GroupContractUtil();

    public OperationContractUtil() {
        keyPrefix = "operation:";
        thing = "operationData";
        identifier = "operationId";
    }

    public String getUserRejectionMessage(String message) {
        return message;
    }

    public boolean hasRightToParticipate(Context ctx, OperationData operationData) throws MissingTransactionError {
        String clientId = getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        List<String> clientGroups = new GroupContractUtil().getGroupNamesForUser(ctx.getStub(), clientId);
        ApprovalList requiredApprovals = AccessManager.getRequiredApprovals(operationData);
        return requiredApprovals.getUsers().contains(clientId) || requiredApprovals.getGroups().stream().anyMatch(clientGroups::contains);
    }

    public void approveOperation(Context ctx, OperationData operationData) throws MissingTransactionError, OperationAccessError {

        if (!operationData.getState().equals(OperationDataState.PENDING)) {
            throw new OperationAccessError(GsonWrapper.toJson(getApprovalImpossibleError()));
        }
        if(!hasRightToParticipate(ctx, operationData)) {
            throw new OperationAccessError(GsonWrapper.toJson(getApprovalDeniedError()));
        }

        String clientId = this.getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        List<String> clientGroups = new GroupContractUtil().getGroupNamesForUser(ctx.getStub(), clientId);
        ApprovalList existingApprovals = operationData.getExistingApprovals().addUsersItem(clientId).addGroupsItems(clientGroups);
        ApprovalList requiredApprovals = AccessManager.getRequiredApprovals(operationData);
        ApprovalList missingApprovals = OperationContractUtil.getMissingApprovalList(requiredApprovals, existingApprovals);

        operationData.lastModifiedTimestamp(this.getTimestamp(ctx.getStub()))
                .existingApprovals(existingApprovals)
                .missingApprovals(missingApprovals);
    }

    public void rejectOperation(Context ctx, OperationData operationData, String rejectMessage) throws OperationAccessError, MissingTransactionError {
        if(!operationData.getState().equals(OperationDataState.PENDING)){
            throw new OperationAccessError(GsonWrapper.toJson(getRejectionImpossibleError()));
        }
        if (!hasRightToParticipate(ctx, operationData)) {
            throw new OperationAccessError(GsonWrapper.toJson(getRejectionDeniedError()));
        }

        operationData.state(OperationDataState.REJECTED).reason(getUserRejectionMessage(rejectMessage));
    }

    public List<OperationData> getOperations(
            ChaincodeStub stub,
            final List<String> operationIds,
            final String existingEnrollmentId,
            final String missingEnrollmentId,
            final String initiatorEnrollmentId,
            final String involvedEnrollmentId,
            final List<String> states) {
        List<String> groupsForUserMissingApproval = gUtil.getGroupNamesForUser(stub, missingEnrollmentId);
        List<String> groupsForUserInvolved = gUtil.getGroupNamesForUser(stub, involvedEnrollmentId);

        return this.getAllStates(stub, OperationData.class).stream()
                .filter(item -> operationIds.isEmpty() ||
                        operationIds.contains(item.getOperationId()))
                .filter(item -> existingEnrollmentId.isEmpty() ||
                        item.getExistingApprovals().getUsers().contains(existingEnrollmentId))
                .filter(item -> missingEnrollmentId.isEmpty() ||
                        item.getMissingApprovals().getUsers().contains(missingEnrollmentId) ||
                        item.getMissingApprovals().getGroups().stream().anyMatch(groupsForUserMissingApproval::contains))
                .filter(item -> initiatorEnrollmentId.isEmpty() ||
                        item.getInitiator().equals(initiatorEnrollmentId))
                .filter(item -> involvedEnrollmentId.isEmpty() ||
                        item.getExistingApprovals().getUsers().contains(involvedEnrollmentId) ||
                        item.getMissingApprovals().getUsers().contains(involvedEnrollmentId) ||
                        item.getMissingApprovals().getGroups().stream().anyMatch(groupsForUserInvolved::contains) ||
                        item.getInitiator().equals(initiatorEnrollmentId))
                .filter(item -> states.isEmpty() ||
                        states.contains(item.getState().toString()))
                .collect(Collectors.toList());
    }

    public static boolean covers(ApprovalList requiredApprovals, ApprovalList existingApprovals) {
        return getMissingApprovalList(requiredApprovals, existingApprovals).isEmpty();
    }

    public static ApprovalList getMissingApprovalList(ApprovalList requiredApprovals, ApprovalList existingApprovals) {
        ApprovalList missingApprovals = new ApprovalList();
        missingApprovals.setUsers(requiredApprovals.getUsers().stream().filter(user -> !existingApprovals.getUsers().contains(user)).collect(Collectors.toList()));
        missingApprovals.setGroups(requiredApprovals.getGroups().stream().filter(group -> !existingApprovals.getGroups().contains(group)).collect(Collectors.toList()));
        return missingApprovals;
    }

    public static String getDraftKey(final String contractName, final String transactionName, final String params) throws NoSuchAlgorithmException {
        String all = contractName + HASH_DELIMITER + transactionName + HASH_DELIMITER + params.replace(" ", "");
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(all.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getUrlEncoder().encode(bytes));
    }

    public DetailedError getContractUnprocessableError(String contractName) {
        return getUnprocessableEntityError(new InvalidParameter()
                .name("contractName")
                .reason("The given contract \"" + contractName + "\" does not exist"));
    }

    public DetailedError getTransactionUnprocessableError(String transactionName) {
        return getUnprocessableEntityError(new InvalidParameter()
                .name("transactionName")
                .reason("The given transaction \"" + transactionName + "\" does not exist"));
    }

    public GenericError getApprovalDeniedError(){
        return new GenericError()
                .type("HLApprovalDenied")
                .title("You are not allowed to approve the given operation");
    }

    public GenericError getApprovalImpossibleError(){
        return new GenericError()
                .type("HLApprovalImpossible")
                .title("The operation is not in pending state");
    }

    public GenericError getRejectionDeniedError(){
        return new GenericError()
                .type("HLRejectionDenied")
                .title("You are not allowed to reject the given operation");
    }

    public GenericError getRejectionImpossibleError(){
        return new GenericError()
                .type("HLRejectionImpossible")
                .title("The operation is not in pending state");
    }

    public DetailedError getEmptyContractNameError() {
        return getUnprocessableEntityError(getEmptyInvalidParameter("contractName"));
    }

    public DetailedError getEmptyTransactionNameError() {
        return getUnprocessableEntityError(getEmptyInvalidParameter("transactionName"));
    }

    public OperationData getOrInitializeOperationData(Context ctx, String initiator, String contractName, String transactionName, String params) throws NoSuchAlgorithmException {
        String key = OperationContractUtil.getDraftKey(contractName, transactionName, params);
        OperationData operationData;
        String timeStamp = getTimestamp(ctx.getStub());
        try {
            operationData = getState(ctx.getStub(), key, OperationData.class);
        } catch (LedgerAccessError ledgerAccessError) {
            operationData = new OperationData()
                    .initiator(initiator)
                    .operationId(key)
                    .initiatedTimestamp(timeStamp)
                    .transactionInfo(new TransactionInfo().contractName(contractName).transactionName(transactionName).parameters(params))
                    .state(OperationDataState.PENDING)
                    .reason("");

        }
        return operationData;
    }
}
