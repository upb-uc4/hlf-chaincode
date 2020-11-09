package de.upb.cs.uc4.chaincode.util;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.error.LedgerAccessError;
import de.upb.cs.uc4.chaincode.error.LedgerStateNotFoundError;
import de.upb.cs.uc4.chaincode.error.UnprocessableLedgerStateError;
import de.upb.cs.uc4.chaincode.model.GenericError;
import de.upb.cs.uc4.chaincode.model.InvalidParameter;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

public class ApprovalContractUtil extends ContractUtil {
    private final String thing = "list of approvals";
    private final String identifier = "transaction";
    private final String HASH_DELIMITER = "::"; //TODO: rework delimiting

    public ApprovalContractUtil() {
        keyPrefix = "draft:";
    }

    @Override
    public GenericError getConflictError() {
        return null;
    }

    @Override
    public GenericError getNotFoundError() {
        return super.getNotFoundError(thing, identifier);
    }

    public GenericError getInternalError() {
        return new GenericError()
                .type("HLInternalError")
                .title("SHA-256 appearently does not exist lol...");
    }

    public InvalidParameter getEmptyContractNameParam() {
        return new InvalidParameter()
                .name("contractName")
                .reason("Contract name must not be empty");
    }

    public InvalidParameter getEmptyTransactionNameParam() {
        return new InvalidParameter()
                .name("transactionName")
                .reason("Transaction name must not be empty");
    }

    public String getDraftKey(final String contractName, final String transactionName, final String... params) throws NoSuchAlgorithmException {
        String all = contractName + HASH_DELIMITER + transactionName + HASH_DELIMITER + String.join(HASH_DELIMITER, params);
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
        return id.getMSPID() + HASH_DELIMITER + id.getId(); //TODO: rework delimiting
    }

    public ArrayList<String> getState(ChaincodeStub stub, String key) throws LedgerAccessError {
        String jsonApprovals;
        jsonApprovals = getStringState(stub, key);
        if (valueUnset(jsonApprovals)) {
            throw new LedgerStateNotFoundError(GsonWrapper.toJson(getNotFoundError()));
        }
        ArrayList<String> approvals;
        try {
            Type setType = new TypeToken<ArrayList<String>>(){}.getType();
            approvals = GsonWrapper.fromJson(jsonApprovals, setType);
        } catch(Exception e) {
            throw new UnprocessableLedgerStateError(GsonWrapper.toJson(getUnprocessableLedgerStateError()));
        }
        return approvals;
    }

    public String addApproval(ChaincodeStub stub, final String key, final String id) {
        ArrayList<String> approvals;
        try{
            approvals = getState(stub, key);
        } catch(LedgerStateNotFoundError e) {
            approvals = new ArrayList<>();
        } catch(LedgerAccessError e) {
            return e.getJsonError();
        }
        if (!approvals.contains(id)) {
            approvals.add(id);
        }
        String jsonApprovals = GsonWrapper.toJson(approvals);
        putAndGetStringState(stub, key, jsonApprovals);
        return jsonApprovals;
    }

    public ArrayList<InvalidParameter> getErrorForInput(String contractName, String transactionName) {
        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        if (valueUnset(contractName)) {
            invalidParams.add(getEmptyContractNameParam());
        }
        if (valueUnset(transactionName)) {
            invalidParams.add(getEmptyTransactionNameParam());
        }
        return invalidParams;
    }
}