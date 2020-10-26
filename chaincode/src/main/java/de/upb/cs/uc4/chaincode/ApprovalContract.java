package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.model.GenericError;
import de.upb.cs.uc4.chaincode.util.ApprovalContractUtil;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.security.NoSuchAlgorithmException;

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
        String key;
        try {
            key = cUtil.getDraftKey(contractName, transactionName, params);
        } catch (NoSuchAlgorithmException e) {
            return GsonWrapper.toJson(new GenericError()
                    .type("HLInternalException")
                    .title("SHA-256 appearently does not exist lol..."));
        }
        String id = cUtil.getDraftId(ctx.getClientIdentity());
        return cUtil.addApproval(stub, key, id);
    }
}
