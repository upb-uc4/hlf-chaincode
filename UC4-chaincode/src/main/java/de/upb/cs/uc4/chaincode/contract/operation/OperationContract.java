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

import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

@Contract(
        name = "UC4.Operation"
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
    public String approveTransaction(final Context ctx, final String initiator, final String contractName, final String transactionName, final String params) {
        ArrayList<InvalidParameter> invalidParameters = cUtil.getErrorForInput(contractName, transactionName);
        if(!invalidParameters.isEmpty()){
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParameters));
        }
        try {
            ValidationManager.validateParams(ctx, contractName, transactionName, params);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        String key;
        try {
            key = OperationContractUtil.getDraftKey(contractName, transactionName, params);
        } catch (NoSuchAlgorithmException e) {
            return GsonWrapper.toJson(cUtil.getInternalError());
        }
        OperationData operationData;
        DateTimeFormatter fm = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
        String timeStamp = fm.format(ctx.getStub().getTxTimestamp().truncatedTo(SECONDS));
        try{
            operationData = cUtil.getState(ctx.getStub(), key, OperationData.class);
        } catch (LedgerAccessError ledgerAccessError) {
            operationData = new OperationData()
                    .initiator(initiator)
                    .operationId(key)
                    .initiatedTimestamp(timeStamp)
                    .transactionInfo(new TransactionInfo().contractName(contractName).transactionName(transactionName).parameters(params))
                    .state(OperationDataState.PENDING)
                    .reason("");

        }
        String clientId = cUtil.getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        List<String> clientGroups = new GroupContractUtil().getGroupNamesForUser(ctx.getStub(), clientId);

        ApprovalList existingApprovals = operationData.getExistingApprovals().addUsersItem(clientId).addGroupsItems(clientGroups);
        ApprovalList requiredApprovals = null;
        try {
            requiredApprovals = AccessManager.getRequiredApprovals(contractName, transactionName, params);
        } catch (MissingTransactionError e) {
            return e.getJsonError();
        }
        ApprovalList missingApprovals = OperationContractUtil.getMissingApprovalList(requiredApprovals, existingApprovals);
        OperationData result = operationData
                .lastModifiedTimestamp(timeStamp)
                .existingApprovals(existingApprovals)
                .missingApprovals(missingApprovals);
        return cUtil.putAndGetStringState(ctx.getStub(), key, GsonWrapper.toJson(result));
    }

    @Transaction
    public String rejectTransaction(final Context ctx, final String operationId, final String rejectMessage) {
        OperationData operationData;
        try {
            operationData = cUtil.<OperationData>getState(ctx.getStub(), operationId, OperationData.class);
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
            operationData = cUtil.<OperationData>getState(ctx.getStub(), operationId, OperationData.class);
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
