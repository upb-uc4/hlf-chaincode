package de.upb.cs.uc4.chaincode.util.helper;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.exceptions.MissingAccessRightsError;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import de.upb.cs.uc4.chaincode.model.MatriculationData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AccessManager {
    static final String ADMIN = "admin";

    public static ApprovalList getRequiredApprovals(String contractName, String transactionName, String params) {
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> paramList = GsonWrapper.fromJson(params, listType);
        switch (contractName) {
            case "UC4.MatriculationData":
                switch (transactionName) {
                    case "addMatriculationData":
                        return getRequiredApprovalsForAddMatriculationData(paramList);
                    default:
                        throw new MissingAccessRightsError(contractName, transactionName);
                }
            case "UC4.Admission":
                switch (transactionName) {
                    case "addAdmission":
                        return getRequiredApprovalsForAddAdmission(paramList);
                    case "dropAdmission":
                        return getRequiredApprovalsForDropAdmission(paramList);
                    default:
                        throw new MissingAccessRightsError(contractName, transactionName);
                }
            case "Uc4.Goup":
                switch (transactionName) {
                    case "addUserToGroup":
                        return getRequiredApprovalsForAddUserToGroup(paramList);
                    case "removeUserFromGroup":
                        return getRequiredApprovalsForRemoveUserFromGroup(paramList);
                    case "removeUserFromAllGroups":
                        return getRequiredApprovalsForRemoveUserFromAllGroups(paramList);
                    default:
                        throw new MissingAccessRightsError(contractName, transactionName);
                }
            default:
                throw new MissingAccessRightsError(contractName, "");
        }
    }

    private static ApprovalList getRequiredApprovalsForAddMatriculationData(List<String> params) {
        MatriculationData matriculationData = GsonWrapper.fromJson(params.get(0), MatriculationData.class);
        return new ApprovalList()
                .addUsersItem(matriculationData.getEnrollmentId())
                .addGroupsItem(ADMIN);
    }

    private static ApprovalList getRequiredApprovalsForAddAdmission(List<String> params) {
        return new ApprovalList();
        // TODO re-enable actual approval requirements
        /*Admission admission = GsonWrapper.fromJson(params.get(0), Admission.class);
        return new ApprovalList()
                .addUsersItem(admission.getEnrollmentId())
                .addGroupsItem(ADMIN);*/
    }

    private static ApprovalList getRequiredApprovalsForDropAdmission(List<String> params) {
        return new ApprovalList();
        // TODO re-enable actual approval requirements
        // TODO only check admin?
        /*return new ApprovalList()
                .addGroupsItem(ADMIN);*/
    }

    private static ApprovalList getRequiredApprovalsForAddUserToGroup(List<String> params) {
        return new ApprovalList();
        // TODO re-enable actual approval requirements
        /*return new ApprovalList()
                .addGroupsItem(ADMIN);*/
    }

    private static ApprovalList getRequiredApprovalsForRemoveUserFromGroup(List<String> params) {
        return new ApprovalList();
        // TODO re-enable actual approval requirements
        // TODO only check admin?
        /*return new ApprovalList()
                .addGroupsItem(ADMIN);*/
    }

    private static ApprovalList getRequiredApprovalsForRemoveUserFromAllGroups(List<String> params) {
        return new ApprovalList();
        // TODO re-enable actual approval requirements
        // TODO only check admin?
        /*return new ApprovalList()
                .addGroupsItem(ADMIN);*/
    }
}
