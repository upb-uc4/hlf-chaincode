package de.upb.cs.uc4.chaincode.helper;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.approval.ApprovalContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.parameter.MissingTransactionError;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import de.upb.cs.uc4.chaincode.model.MatriculationData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AccessManager {
    private static ApprovalContractUtil approvalUtil = new ApprovalContractUtil();

    public static final String ADMIN = "admin";

    public static ApprovalList getRequiredApprovals(String contractName, String transactionName, String params) throws MissingTransactionError {
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> paramList = GsonWrapper.fromJson(params, listType);
        switch (contractName) {
            case "UC4.MatriculationData":
                switch (transactionName) {
                    case "addMatriculationData":
                        return getRequiredApprovalsForAddMatriculationData(paramList);
                    case "updateMatriculationData":
                        return getRequiredApprovalsForUpdateMatriculationData(paramList);
                    case "getMatriculationData":
                        return getRequiredApprovalsForGetMatriculationData(paramList);
                    case "addEntriesToMatriculationData":
                        return getRequiredApprovalsForAddEntriesToMatriculationData(paramList);
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(approvalUtil.getTransactionUnprocessableError(transactionName)));
                }
            case "UC4.Admission":
                switch (transactionName) {
                    case "addAdmission":
                        return getRequiredApprovalsForAddAdmission(paramList);
                    case "dropAdmission":
                        return getRequiredApprovalsForDropAdmission(paramList);
                    case "getAdmissions":
                        return getRequiredApprovalsForGetAdmissions(paramList);
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(approvalUtil.getTransactionUnprocessableError(transactionName)));
                }
            case "UC4.Group":
                switch (transactionName) {
                    case "addUserToGroup":
                        return getRequiredApprovalsForAddUserToGroup(paramList);
                    case "removeUserFromGroup":
                        return getRequiredApprovalsForRemoveUserFromGroup(paramList);
                    case "removeUserFromAllGroups":
                        return getRequiredApprovalsForRemoveUserFromAllGroups(paramList);
                    case "getAllGroups":
                        return getRequiredApprovalsForGetAllGroups(paramList);
                    case "getUsersForGroup":
                        return getRequiredApprovalsForGetUsersForGroup(paramList);
                    case "getGroupsForUser":
                        return getRequiredApprovalsForGetGroupsForUser(paramList);
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(approvalUtil.getTransactionUnprocessableError(transactionName)));
                }
            case "UC4.Certificate":
                switch (transactionName) {
                    case "addCertificate":
                        return getRequiredApprovalsForAddCertificate(paramList);
                    case "updateCertificate":
                        return getRequiredApprovalsForUpdateCertificate(paramList);
                    case "getCertificate":
                        return getRequiredApprovalsForGetCertificate(paramList);
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(approvalUtil.getTransactionUnprocessableError(transactionName)));
                }
            case "UC4.ExaminationRegulation":
                switch (transactionName) {
                    case "addExaminationRegulation":
                        return getRequiredApprovalsForAddExaminationRegulation(paramList);
                    case "getExaminationRegulations":
                        return getRequiredApprovalsForGetExaminationRegulations(paramList);
                    case "closeExaminationRegulation":
                        return getRequiredApprovalsForCloseExaminationRegulation(paramList);
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(approvalUtil.getTransactionUnprocessableError(transactionName)));
                }
            default:
                throw new MissingTransactionError(GsonWrapper.toJson(approvalUtil.getContractUnprocessableError(contractName)));
        }
    }

    private static ApprovalList getRequiredApprovalsForAddMatriculationData(List<String> params) {
        MatriculationData matriculationData = GsonWrapper.fromJson(params.get(0), MatriculationData.class);
        return new ApprovalList()
                .addUsersItem(matriculationData.getEnrollmentId())
                .addGroupsItem(ADMIN);
    }

    private static ApprovalList getRequiredApprovalsForUpdateMatriculationData(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForGetMatriculationData(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForAddEntriesToMatriculationData(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForAddAdmission(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForDropAdmission(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForGetAdmissions(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForAddUserToGroup(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForRemoveUserFromGroup(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForRemoveUserFromAllGroups(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForGetAllGroups(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForGetUsersForGroup(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForGetGroupsForUser(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForAddCertificate(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForUpdateCertificate(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForGetCertificate(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForAddExaminationRegulation(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForGetExaminationRegulations(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForCloseExaminationRegulation(List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }
}
