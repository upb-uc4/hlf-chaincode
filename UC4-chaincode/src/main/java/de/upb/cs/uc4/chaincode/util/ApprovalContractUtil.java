package de.upb.cs.uc4.chaincode.util;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.exceptions.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.LedgerStateNotFoundError;
import de.upb.cs.uc4.chaincode.exceptions.UnprocessableLedgerStateError;
import de.upb.cs.uc4.chaincode.model.Approval;
import de.upb.cs.uc4.chaincode.model.errors.GenericError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ApprovalContractUtil extends ContractUtil {
    private final String thing = "list of approvals";
    private final String identifier = "transaction";
    private static final String HASH_DELIMITER = new String(Character.toChars(Character.MIN_CODE_POINT));

    public ApprovalContractUtil() {
        keyPrefix = "draft:";
    }

    @Override
    public GenericError getNotFoundError() {
        return super.getNotFoundError(thing, identifier);
    }

    public GenericError getInternalError() {
        return new GenericError()
                .type("HLInternalError")
                .title("SHA-256 apparently does not exist lol...");
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

    public ArrayList<Approval> getState(ChaincodeStub stub, String key) throws LedgerAccessError {
        String jsonApprovals;
        jsonApprovals = getStringState(stub, key);
        if (valueUnset(jsonApprovals)) {
            throw new LedgerStateNotFoundError(GsonWrapper.toJson(getNotFoundError()));
        }
        ArrayList<Approval> approvals;
        try {
            Type setType = new TypeToken<ArrayList<Approval>>(){}.getType();
            approvals = GsonWrapper.fromJson(jsonApprovals, setType);
        } catch(Exception e) {
            throw new UnprocessableLedgerStateError(GsonWrapper.toJson(getUnprocessableLedgerStateError()));
        }
        return approvals;
    }

    public String addApproval(ChaincodeStub stub, final String key, final Approval approval) {
        ArrayList<Approval> approvals;
        try{
            approvals = getState(stub, key);
        } catch(LedgerAccessError e) {
            approvals = new ArrayList<>();
        }
        if (!approvals.contains(approval)) {
            approvals.add(approval);
        }
        return putAndGetStringState(stub, key, GsonWrapper.toJson(approvals));
    }

    public static boolean covers(Function<Approval, String> func, List<Approval> approvals, List<String> required) {
        return approvals.stream().map(func).collect(Collectors.toList()).containsAll(required);
    }

    public static boolean covers(List<Approval> approvals, List<String> requiredIds, List<String> requiredTypes) {
        return covers(Approval::getId, approvals, requiredIds) && covers(Approval::getType, approvals, requiredTypes);
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
