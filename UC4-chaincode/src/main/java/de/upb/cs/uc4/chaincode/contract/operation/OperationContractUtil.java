package de.upb.cs.uc4.chaincode.contract.operation;

import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParticipationError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ValidationError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.parameter.MissingTransactionError;
import de.upb.cs.uc4.chaincode.helper.AccessManager;
import de.upb.cs.uc4.chaincode.helper.*;
import de.upb.cs.uc4.chaincode.model.operation.ApprovalList;
import de.upb.cs.uc4.chaincode.model.operation.OperationData;
import de.upb.cs.uc4.chaincode.model.operation.OperationDataState;
import de.upb.cs.uc4.chaincode.model.operation.TransactionInfo;
import de.upb.cs.uc4.chaincode.model.errors.DetailedError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

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

    public void approveOperation(Context ctx, OperationData operationData) throws MissingTransactionError, LedgerAccessError, ParticipationError {

        checkMayParticipate(ctx, operationData);

        String clientId = HyperledgerManager.getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        List<String> clientGroups = new GroupContractUtil().getGroupNamesForUser(ctx.getStub(), clientId);
        ApprovalList existingApprovals = operationData.getExistingApprovals().addUsersItem(clientId).addGroupsItems(clientGroups);
        ApprovalList requiredApprovals = AccessManager.getRequiredApprovals(ctx, operationData);
        ApprovalList missingApprovals = ValidationManager.getMissingApprovalList(requiredApprovals, existingApprovals);

        operationData.lastModifiedTimestamp(ctx.getStub().getTxTimestamp())
                .existingApprovals(existingApprovals)
                .missingApprovals(missingApprovals);
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

    public static String getDraftKey(final String contractName, final String transactionName, final String params) throws ValidationError {
        String all = contractName + HASH_DELIMITER + transactionName + HASH_DELIMITER + params.replace(" ", "");
        return GeneralHelper.hashAndEncodeBase64url(all);
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

    public DetailedError getEmptyContractNameError() {
        return getUnprocessableEntityError(getEmptyInvalidParameter("contractName"));
    }

    public DetailedError getEmptyTransactionNameError() {
        return getUnprocessableEntityError(getEmptyInvalidParameter("transactionName"));
    }

    public OperationData getOrInitializeOperationData(Context ctx, String initiator, String contractName, String transactionName, String params) throws ValidationError {
        String clientId = HyperledgerManager.getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        initiator = GeneralHelper.valueUnset(initiator) ? clientId : initiator;

        String key = OperationContractUtil.getDraftKey(contractName, transactionName, params);
        OperationData operationData;
        try {
            operationData = getState(ctx.getStub(), key, OperationData.class);
        } catch (LedgerAccessError ledgerAccessError) {
            operationData = new OperationData()
                    .initiator(initiator)
                    .operationId(key)
                    .initiatedTimestamp(ctx.getStub().getTxTimestamp())
                    .transactionInfo(new TransactionInfo().contractName(contractName).transactionName(transactionName).parameters(params))
                    .state(OperationDataState.PENDING)
                    .reason("");
        }
        return operationData;
    }
}
