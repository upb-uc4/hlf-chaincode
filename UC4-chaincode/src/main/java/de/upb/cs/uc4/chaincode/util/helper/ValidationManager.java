package de.upb.cs.uc4.chaincode.util.helper;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.exceptions.MissingAccessRightsError;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ValidationManager {
    public static ApprovalList getRequiredApprovals(Context ctx, String contractName, String transactionName, String params) {
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> paramList = GsonWrapper.fromJson(params, listType);
        switch (contractName) {
            case "UC4.MatriculationData":
                switch (transactionName) {
                    case "addMatriculationData":
                        //TODO return getRequiredApprovalsForAddMatriculationData(paramList);
                    default:
                        throw new MissingAccessRightsError(contractName, transactionName);
                }
            case "UC4.Admission":
                switch (transactionName) {
                    case "addAdmission":
                        //TODO return getRequiredApprovalsForAddAdmission(paramList);
                    case "dropAdmission":
                        //TODO return getRequiredApprovalsForDropAdmission(paramList);
                    default:
                        throw new MissingAccessRightsError(contractName, transactionName);
                }
            case "Uc4.Goup":
                switch (transactionName) {
                    case "addUserToGroup":
                        //TODO return getRequiredApprovalsForAddUserToGroup(paramList);
                    case "removeUserFromGroup":
                        //TODO return getRequiredApprovalsForRemoveUserFromGroup(paramList);
                    case "removeUserFromAllGroups":
                        //TODO return getRequiredApprovalsForRemoveUserFromAllGroups(paramList);
                    default:
                        throw new MissingAccessRightsError(contractName, transactionName);
                }
            default:
                throw new MissingAccessRightsError(contractName, "");
        }
    }
}
