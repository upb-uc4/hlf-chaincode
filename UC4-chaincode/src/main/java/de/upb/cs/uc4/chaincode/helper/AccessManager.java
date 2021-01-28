package de.upb.cs.uc4.chaincode.helper;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContract;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContract;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContract;
import de.upb.cs.uc4.chaincode.contract.group.GroupContract;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContract;
import de.upb.cs.uc4.chaincode.contract.operation.OperationContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.parameter.MissingTransactionError;
import de.upb.cs.uc4.chaincode.model.Admission;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import de.upb.cs.uc4.chaincode.model.MatriculationData;
import org.hyperledger.fabric.contract.Context;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AccessManager {
    private static OperationContractUtil operationUtil = new OperationContractUtil();

    public static final String ADMIN = "Admin";
    public static final String SYSTEM = "System";

    public static ApprovalList getRequiredApprovals(Context ctx, String contractName, String transactionName, String params) throws MissingTransactionError, LedgerAccessError {
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> paramList = GsonWrapper.fromJson(params, listType);
        switch (contractName) {
            case MatriculationDataContract.contractName:
                switch (transactionName) {
                    case "addMatriculationData":
                        return getRequiredApprovalsForAddMatriculationData(ctx, paramList);
                    case "updateMatriculationData":
                        return getRequiredApprovalsForUpdateMatriculationData(ctx, paramList);
                    case "getMatriculationData":
                        return getRequiredApprovalsForGetMatriculationData(ctx, paramList);
                    case "addEntriesToMatriculationData":
                        return getRequiredApprovalsForAddEntriesToMatriculationData(ctx, paramList);
                    case "getVersion":
                        return new ApprovalList();
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
            case AdmissionContract.contractName:
                switch (transactionName) {
                    case "addAdmission":
                        return getRequiredApprovalsForAddAdmission(ctx, paramList);
                    case "dropAdmission":
                        return getRequiredApprovalsForDropAdmission(ctx, paramList);
                    case "getAdmissions":
                        return getRequiredApprovalsForGetAdmissions(ctx, paramList);
                    case "getVersion":
                        return new ApprovalList();
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
            case GroupContract.contractName:
                switch (transactionName) {
                    case "addUserToGroup":
                        return getRequiredApprovalsForAddUserToGroup(ctx, paramList);
                    case "removeUserFromGroup":
                        return getRequiredApprovalsForRemoveUserFromGroup(ctx, paramList);
                    case "removeUserFromAllGroups":
                        return getRequiredApprovalsForRemoveUserFromAllGroups(ctx, paramList);
                    case "getAllGroups":
                        return getRequiredApprovalsForGetAllGroups(ctx, paramList);
                    case "getUsersForGroup":
                        return getRequiredApprovalsForGetUsersForGroup(ctx, paramList);
                    case "getGroupsForUser":
                        return getRequiredApprovalsForGetGroupsForUser(ctx, paramList);
                    case "getVersion":
                        return new ApprovalList();
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
            case CertificateContract.contractName:
                switch (transactionName) {
                    case "addCertificate":
                        return getRequiredApprovalsForAddCertificate(ctx, paramList);
                    case "updateCertificate":
                        return getRequiredApprovalsForUpdateCertificate(ctx, paramList);
                    case "getCertificate":
                        return getRequiredApprovalsForGetCertificate(ctx, paramList);
                    case "getVersion":
                        return new ApprovalList();
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
            case ExaminationRegulationContract.contractName:
                switch (transactionName) {
                    case "addExaminationRegulation":
                        return getRequiredApprovalsForAddExaminationRegulation(ctx, paramList);
                    case "getExaminationRegulations":
                        return getRequiredApprovalsForGetExaminationRegulations(ctx, paramList);
                    case "closeExaminationRegulation":
                        return getRequiredApprovalsForCloseExaminationRegulation(ctx, paramList);
                    case "getVersion":
                        return new ApprovalList();
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
            case "":
                throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyContractNameError()));
            default:
                throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getContractUnprocessableError(contractName)));
        }
    }

    private static ApprovalList getRequiredApprovalsForAddMatriculationData(Context ctx, List<String> params) {
        MatriculationData matriculationData = GsonWrapper.fromJson(params.get(0), MatriculationData.class);
        return new ApprovalList()
                .addUsersItem(matriculationData.getEnrollmentId())
                .addGroupsItem(ADMIN)
                .addGroupsItem(SYSTEM);
    }

    private static ApprovalList getRequiredApprovalsForUpdateMatriculationData(Context ctx, List<String> params) {
        return new ApprovalList()
                .addGroupsItem(SYSTEM);
    }

    private static ApprovalList getRequiredApprovalsForGetMatriculationData(Context ctx, List<String> params) {
        return new ApprovalList()
                .addGroupsItem(SYSTEM);
    }

    private static ApprovalList getRequiredApprovalsForAddEntriesToMatriculationData(Context ctx, List<String> params) {
        String enrollmentId = params.get(0);
        return new ApprovalList()
                .addUsersItem(enrollmentId)
                .addGroupsItem(ADMIN)
                .addGroupsItem(SYSTEM);
    }

    private static ApprovalList getRequiredApprovalsForAddAdmission(Context ctx, List<String> params) {
        Admission admission = GsonWrapper.fromJson(params.get(0), Admission.class);
        return new ApprovalList()
                .addUsersItem(admission.getEnrollmentId())
                .addGroupsItem(SYSTEM);
    }

    private static ApprovalList getRequiredApprovalsForDropAdmission(Context ctx, List<String> params) throws LedgerAccessError {
        AdmissionContractUtil cUtil = new AdmissionContractUtil();
        Admission admission = cUtil.getState(ctx.getStub(), params.get(0), Admission.class);
        return new ApprovalList()
                .addUsersItem(admission.getEnrollmentId())
                .addGroupsItem(SYSTEM);
    }

    private static ApprovalList getRequiredApprovalsForGetAdmissions(Context ctx, List<String> params) {
        return new ApprovalList()
                .addGroupsItem(SYSTEM);
    }

    private static ApprovalList getRequiredApprovalsForAddUserToGroup(Context ctx, List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForRemoveUserFromGroup(Context ctx, List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForRemoveUserFromAllGroups(Context ctx, List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForGetAllGroups(Context ctx, List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForGetUsersForGroup(Context ctx, List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForGetGroupsForUser(Context ctx, List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForAddCertificate(Context ctx, List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForUpdateCertificate(Context ctx, List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForGetCertificate(Context ctx, List<String> params) {
        // TODO fill with required approvals
        return new ApprovalList();
    }

    private static ApprovalList getRequiredApprovalsForAddExaminationRegulation(Context ctx, List<String> params) {
        return new ApprovalList()
                .addGroupsItem(SYSTEM);
    }

    private static ApprovalList getRequiredApprovalsForGetExaminationRegulations(Context ctx, List<String> params) {
        return new ApprovalList()
                .addGroupsItem(SYSTEM);
    }

    private static ApprovalList getRequiredApprovalsForCloseExaminationRegulation(Context ctx, List<String> params) {
        return new ApprovalList()
                .addGroupsItem(SYSTEM);
    }
}
