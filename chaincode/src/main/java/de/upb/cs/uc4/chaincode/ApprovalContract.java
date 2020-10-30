package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.error.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.GenericError;
import de.upb.cs.uc4.chaincode.model.InvalidParameter;
import de.upb.cs.uc4.chaincode.util.ApprovalContractUtil;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;

@Contract(
        name="UC4.Approval"
)
public class ApprovalContract extends ContractBase {

    private final ApprovalContractUtil cUtil = new ApprovalContractUtil();

    /**
     * Submits a draft to the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String approveTransaction(final Context ctx, final String contractName, final String transactionName, final String... params) {
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

        String id = cUtil.getDraftId(ctx.getClientIdentity());
        return cUtil.addApproval(stub, key, id);
    }

    @Transaction()
    public String getApprovals(final Context ctx, final String contractName, final String transactionName, final String... params) {
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

        ArrayList<String> approvals;
        try{
            approvals = cUtil.getState(stub, key);
        } catch(LedgerAccessError e) {
            return e.getJsonError();
        }
        return GsonWrapper.toJson(approvals);
    }
}
