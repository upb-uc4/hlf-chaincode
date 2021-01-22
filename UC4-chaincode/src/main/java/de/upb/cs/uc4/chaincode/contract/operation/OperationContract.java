package de.upb.cs.uc4.chaincode.contract.operation;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.parameter.MissingTransactionError;
import de.upb.cs.uc4.chaincode.helper.AccessManager;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.helper.ValidationManager;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;

import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

@Contract(
        name = "UC4.OperationData"
)
public class OperationContract extends ContractBase {

    private final OperationContractUtil cUtil = new OperationContractUtil();

    /**
     * Submits a draft to the ledger.
     *
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String initiateOperation(final Context ctx, String initiator, final String contractName, final String transactionName, final String params) {
        try {
            ValidationManager.validateParams(ctx, contractName, transactionName, params);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        String clientId = cUtil.getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        List<String> clientGroups = new GroupContractUtil().getGroupNamesForUser(ctx.getStub(), clientId);

        initiator = initiator.isEmpty() ? clientId : initiator;

        OperationData operationData;
        try {
            operationData = cUtil.getOrInitializeOperationData(ctx, initiator, contractName, transactionName, params);
        } catch (NoSuchAlgorithmException e) {
            return GsonWrapper.toJson(cUtil.getInternalError());
        }

        ApprovalList existingApprovals = operationData.getExistingApprovals().addUsersItem(clientId).addGroupsItems(clientGroups);
        ApprovalList requiredApprovals;
        try {
            requiredApprovals = AccessManager.getRequiredApprovals(contractName, transactionName, params);
        } catch (MissingTransactionError e) {
            return e.getJsonError();
        }
        ApprovalList missingApprovals = OperationContractUtil.getMissingApprovalList(requiredApprovals, existingApprovals);
        operationData.lastModifiedTimestamp(cUtil.getTimestamp(ctx.getStub()))
                .existingApprovals(existingApprovals)
                .missingApprovals(missingApprovals);
        return cUtil.putAndGetStringState(ctx.getStub(), operationData.getOperationId(), GsonWrapper.toJson(operationData));
    }

    /**
     * Submits a draft to the ledger.
     *
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String approveOperation(final Context ctx, String operationId) {
        OperationData operationData;
        try {
            operationData = cUtil.getState(ctx.getStub(), operationId, OperationData.class);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }
        if (operationData == null) {
            return GsonWrapper.toJson(cUtil.getNotFoundError());
        }

        String clientId = cUtil.getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        List<String> clientGroups = new GroupContractUtil().getGroupNamesForUser(ctx.getStub(), clientId);

        ApprovalList existingApprovals = operationData.getExistingApprovals().addUsersItem(clientId).addGroupsItems(clientGroups);
        ApprovalList requiredApprovals;
        try {
            TransactionInfo info = operationData.getTransactionInfo();
            requiredApprovals = AccessManager.getRequiredApprovals(info.getContractName(), info.getTransactionName(), info.getParameters());
        } catch (MissingTransactionError e) {
            return e.getJsonError();
        }
        ApprovalList missingApprovals = OperationContractUtil.getMissingApprovalList(requiredApprovals, existingApprovals);
        operationData.lastModifiedTimestamp(cUtil.getTimestamp(ctx.getStub()))
                .existingApprovals(existingApprovals)
                .missingApprovals(missingApprovals);
        return cUtil.putAndGetStringState(ctx.getStub(), operationData.getOperationId(), GsonWrapper.toJson(operationData));
    }

    @Transaction
    public String rejectTransaction(final Context ctx, final String operationId, final String rejectMessage) {
        OperationData operationData;
        try {
            operationData = cUtil.getState(ctx.getStub(), operationId, OperationData.class);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }

        operationData.state(OperationDataState.REJECTED).reason(cUtil.getUserRejectionMessage(rejectMessage));
        return cUtil.putAndGetStringState(ctx.getStub(), operationId, GsonWrapper.toJson(operationData));
    }

    @Transaction()
    public String getOperationData(final Context ctx, final String operationId) {

        OperationData operationData;
        try {
            operationData = cUtil.getState(ctx.getStub(), operationId, OperationData.class);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }
        return GsonWrapper.toJson(operationData);
    }

    @Transaction()
    public String getOperations(final Context ctx, final String existingEnrollmentId, final String missingEnrollmentId, final String initiatorEnrollmentId, final String state) {
                List<OperationData> operations = cUtil.getOperations(ctx.getStub(), existingEnrollmentId, missingEnrollmentId, initiatorEnrollmentId, state);
        return GsonWrapper.toJson(operations);
    }
}
