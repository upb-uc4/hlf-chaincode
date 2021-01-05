package de.upb.cs.uc4.chaincode.contract.approval;

import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import de.upb.cs.uc4.chaincode.model.errors.DetailedError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class ApprovalContractUtil extends ContractUtil {
    private static final String HASH_DELIMITER = new String(Character.toChars(Character.MIN_CODE_POINT));
    private static final GroupContractUtil groupContractUtil = new GroupContractUtil();

    public ApprovalContractUtil() {
        keyPrefix = "draft:";
        thing = "list of approvals";
        identifier = "transaction";
    }

    public static boolean covers(ApprovalList requiredApprovals, ApprovalList existingApprovals) {
        return getMissingApprovalList(requiredApprovals, existingApprovals).isEmpty();
    }

    public static ApprovalList getMissingApprovalList(ApprovalList requiredApprovals, ApprovalList existingApprovals) {
        ApprovalList missingApprovals = new ApprovalList();
        missingApprovals.setUsers(requiredApprovals.getUsers().stream().filter(user -> !existingApprovals.getUsers().contains(user)).collect(Collectors.toList()));
        missingApprovals.setGroups(requiredApprovals.getGroups().stream().filter(group -> !existingApprovals.getGroups().contains(group)).collect(Collectors.toList()));
        return missingApprovals;
    }

    public String getDraftKey(final String contractName, final String transactionName, final String params) throws NoSuchAlgorithmException {
        String all = contractName + HASH_DELIMITER + transactionName + HASH_DELIMITER + params;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = digest.digest(all.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(bytes));
    }

    public ApprovalList addApproval(Context ctx, final String key) {
        ChaincodeStub stub = ctx.getStub();
        String clientId = getEnrollmentIdFromClientId(ctx.getClientIdentity().getId());
        List<String> clientGroups = groupContractUtil.getGroupNamesForUser(stub, clientId);
        ApprovalList approvalList;
        try {
            approvalList = getState(stub, key, ApprovalList.class);
        } catch (LedgerAccessError e) {
            approvalList = new ApprovalList();
        }
        approvalList.addUsersItem(clientId);
        for (String group : clientGroups) {
            approvalList.addGroupsItem(group);
        }
        putAndGetStringState(stub, key, GsonWrapper.toJson(approvalList));
        return approvalList;
    }

    public String getEnrollmentIdFromClientId(String clientId) {
        return clientId.substring(9).split(",")[0];
    }

    public DetailedError getContractUnprocessableError(String parameterName) {
        return getUnprocessableEntityError(new InvalidParameter()
                .name("contractName")
                .reason("The given contract does not exist"));
    }

    public DetailedError getTransactionUnprocessableError(String parameterName) {
        return getUnprocessableEntityError(new InvalidParameter()
                .name("transactionName")
                .reason("The given transaction does not exist"));
    }

    public ArrayList<InvalidParameter> getErrorForInput(String contractName, String transactionName) {
        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        if (valueUnset(contractName)) {
            invalidParams.add(getEmptyInvalidParameter("contractName"));
        }
        if (valueUnset(transactionName)) {
            invalidParams.add(getEmptyInvalidParameter("transactionName"));
        }
        return invalidParams;
    }
}
