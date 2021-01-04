package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.exceptions.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.util.OperationContractUtil;
import de.upb.cs.uc4.chaincode.util.helper.AccessManager;
import de.upb.cs.uc4.chaincode.util.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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
    public String approveTransaction(final Context ctx, final String contractName, final String transactionName, final String params) {
        ArrayList<InvalidParameter> invalidParams = cUtil.getErrorForInput(contractName, transactionName);
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        String key;
        try {
            key = OperationContractUtil.getDraftKey(contractName, transactionName, params);
        } catch (NoSuchAlgorithmException e) {
            return GsonWrapper.toJson(cUtil.getInternalError());
        }
        ApprovalList existingApprovals = cUtil.addApproval(ctx, key);
        ApprovalList requiredApprovals = AccessManager.getRequiredApprovals(contractName, transactionName, params);
        ApprovalList missingApprovals = OperationContractUtil.getMissingApprovalList(requiredApprovals, existingApprovals);
        OperationData result = new OperationData()
                .operationId(key)
                .transactionInfo(new TransactionInfo().contractName(contractName).transactionName(transactionName).parameters(params))
                .state(OperationDataState.PENDING)
                .reason("")
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
    public String getOperations(final Context ctx, final String enrollmentId, String state) {

        List<OperationData> operations = cUtil.getOperations(ctx.getStub(), enrollmentId, state);
        return GsonWrapper.toJson(operations);
    }
}
