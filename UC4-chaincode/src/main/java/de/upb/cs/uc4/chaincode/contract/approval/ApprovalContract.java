package de.upb.cs.uc4.chaincode.contract.approval;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.MissingTransactionError;
import de.upb.cs.uc4.chaincode.exceptions.ParameterError;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.helper.ValidationManager;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import de.upb.cs.uc4.chaincode.model.SubmissionResult;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.helper.AccessManager;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@Contract(
        name = "UC4.Approval"
)
public class ApprovalContract extends ContractBase {

    private final ApprovalContractUtil cUtil = new ApprovalContractUtil();

    /**
     * Submits a draft to the ledger.
     *
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String approveTransaction(final Context ctx, final String contractName, final String transactionName, final String params) {
        try {
            ValidationManager.validateParams(ctx, contractName, transactionName, params);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        String key;
        try {
            key = cUtil.getDraftKey(contractName, transactionName, params);
        } catch (NoSuchAlgorithmException e) {
            return GsonWrapper.toJson(cUtil.getInternalError());
        }
        ApprovalList existingApprovals = cUtil.addApproval(ctx, key);
        ApprovalList requiredApprovals = null;
        try {
            requiredApprovals = AccessManager.getRequiredApprovals(contractName, transactionName, params);
        } catch (MissingTransactionError e) {
            return e.getJsonError();
        }

        ApprovalList missingApprovals = ApprovalContractUtil.getMissingApprovalList(requiredApprovals, existingApprovals);
        SubmissionResult result = new SubmissionResult()
                .operationId(key)
                .existingApprovals(existingApprovals)
                .missingApprovals(missingApprovals);
        return GsonWrapper.toJson(result);
    }

    @Transaction()
    public String getApprovals(final Context ctx, final String contractName, final String transactionName, String params) {
        ChaincodeStub stub = ctx.getStub();

        ArrayList<InvalidParameter> invalidParams = cUtil.getErrorForInput(contractName, transactionName);
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        String key;
        try {
            key = cUtil.getDraftKey(contractName, transactionName, params);
        } catch (NoSuchAlgorithmException e) {
            return GsonWrapper.toJson(cUtil.getInternalError());
        }

        ApprovalList existingApprovals;
        try {
            existingApprovals = cUtil.getState(stub, key, ApprovalList.class);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }
        ApprovalList requiredApprovals = null;
        try {
            requiredApprovals = AccessManager.getRequiredApprovals(contractName, transactionName, params);
        } catch (MissingTransactionError e) {
            return e.getJsonError();
        }
        ApprovalList missingApprovals = ApprovalContractUtil.getMissingApprovalList(requiredApprovals, existingApprovals);
        SubmissionResult result = new SubmissionResult()
                .operationId(key)
                .existingApprovals(existingApprovals)
                .missingApprovals(missingApprovals);
        return GsonWrapper.toJson(result);
    }
}
