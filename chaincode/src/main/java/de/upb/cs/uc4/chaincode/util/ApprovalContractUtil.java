package de.upb.cs.uc4.chaincode.util;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.model.GenericError;
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

    public ApprovalContractUtil() {
        keyPrefix = "draft:";
    }

    @Override
    public GenericError getConflictError() {
        return null;
    }

    @Override
    public GenericError getNotFoundError() {
        return null;
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

    public String addApproval(ChaincodeStub stub, final String key, final String id) {
        String jsonApprovals = this.getStringState(stub, key);
        HashSet<String> approvals;
        if (jsonApprovals == null || jsonApprovals.equals("")) { // TODO: replace by valueUnset
            approvals = new HashSet<>();
        } else {
            Type setType = new TypeToken<HashSet<String>>(){}.getType();
            approvals = GsonWrapper.fromJson(jsonApprovals, setType);
        }
        approvals.add(id);
        jsonApprovals = GsonWrapper.toJson(approvals);
        this.putAndGetStringState(stub, key, jsonApprovals);
        return jsonApprovals;
    }
}
