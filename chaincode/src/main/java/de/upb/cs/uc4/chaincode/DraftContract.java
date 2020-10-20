package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.util.DraftContractUtil;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.security.NoSuchAlgorithmException;

@Contract(
        name="UC4.Draft"
)
public class DraftContract extends ContractBase {

    private final DraftContractUtil cUtil = new DraftContractUtil();

    /**
     * Submits a draft to the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return certificate on success, serialized error on failure
     */
    @Transaction()
    public String submitDraft(final Context ctx, final String transactionName, final String... params) {

        ChaincodeStub stub = ctx.getStub();
        String key;
        try {
            key = cUtil.getDraftKey(transactionName, params);
        } catch (NoSuchAlgorithmException e) {
            return "NOT A PROPER ERROR YET"; // TODO SHA-256 does not exist lol
        }
        String id = cUtil.getDraftId(ctx.getClientIdentity());
        return cUtil.addApproval(stub, key, id);
    }
}
