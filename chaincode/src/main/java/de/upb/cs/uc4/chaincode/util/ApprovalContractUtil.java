package de.upb.cs.uc4.chaincode.util;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.error.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.GenericError;
import de.upb.cs.uc4.chaincode.model.MatriculationData;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ApprovalContractUtil extends ContractUtil {
    private final String thing = "list of approvals";

    public ApprovalContractUtil() {
        keyPrefix = "draft:";
    }

    @Override
    public GenericError getConflictError() {
        return null;
    }

    @Override
    public GenericError getNotFoundError() {
        return super.getNotFoundError(thing);
    }

    public GenericError getInternalError() {
        return new GenericError()
                .type("HLInternalError")
                .title("SHA-256 appearently does not exist lol...");
    }

    public String getDraftKey(final String contractName, final String transactionName, final String... params) throws NoSuchAlgorithmException {
        String all = contractName + transactionName + Arrays.stream(params).collect(Collectors.joining());
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(all.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(bytes));
    }

    /**
     * Returns the draftId, which is obtained by concatenating the submitter's mspId and enrollmentId using "::"
     * @param id identity of the submitter
     * @return draftId
     */
    public String getDraftId(final ClientIdentity id) {
        return id.getMSPID() + "::" + id.getId();
    }

    public HashSet<String> getState(ChaincodeStub stub, String key) throws LedgerAccessError {
        String jsonApprovals;
        jsonApprovals = getStringState(stub, key);
        if (valueUnset(jsonApprovals)) {
            throw new LedgerAccessError(GsonWrapper.toJson(getNotFoundError()));
        }
        HashSet<String> approvals;
        try {
            Type setType = new TypeToken<HashSet<String>>(){}.getType();
            approvals = GsonWrapper.fromJson(jsonApprovals, setType);
        } catch(Exception e) {
            throw new LedgerAccessError(GsonWrapper.toJson(getUnprocessableLedgerStateError()));
        }
        return approvals;
    }

    public String addApproval(ChaincodeStub stub, final String key, final String id) {
        HashSet<String> approvals;
        try{
            approvals = getState(stub, key);
        } catch(LedgerAccessError e) {
            return e.getJsonError();
        }
        approvals.add(id);
        String jsonApprovals = GsonWrapper.toJson(approvals);
        putAndGetStringState(stub, key, jsonApprovals);
        return jsonApprovals;
    }
}
