package de.upb.cs.uc4.chaincode.helper;

import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContract;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContract;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContractUtil;
import de.upb.cs.uc4.chaincode.contract.exam.ExamContract;
import de.upb.cs.uc4.chaincode.contract.exam.ExamContractUtil;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContract;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.contract.examresult.ExamResultContract;
import de.upb.cs.uc4.chaincode.contract.examresult.ExamResultContractUtil;
import de.upb.cs.uc4.chaincode.contract.group.GroupContract;
import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContract;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContractUtil;
import de.upb.cs.uc4.chaincode.contract.operation.OperationContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.parameter.MissingTransactionError;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.model.operation.ApprovalList;
import org.hyperledger.fabric.contract.Context;

import java.util.stream.Collectors;

public class ValidationManager {
    private static final AdmissionContractUtil admissionUtil = new AdmissionContractUtil();
    private static final OperationContractUtil operationUtil = new OperationContractUtil();
    private static final CertificateContractUtil certificateUtil = new CertificateContractUtil();
    private static final ExaminationRegulationContractUtil examinationRegulationUtil = new ExaminationRegulationContractUtil();
    private static final GroupContractUtil groupUtil = new GroupContractUtil();
    private static final MatriculationDataContractUtil matriculationDataUtil = new MatriculationDataContractUtil();
    private static final ExamContractUtil examUtil = new ExamContractUtil();
    private static final ExamResultContractUtil examResultContractUtil = new ExamResultContractUtil();

    public static void validateParams(Context ctx, String contractName, String transactionName, String params) throws SerializableError {
        String[] paramList = GsonWrapper.fromJson(params, String[].class);
        switch (contractName) {
            case MatriculationDataContract.contractName:
                switch (transactionName) {
                    case MatriculationDataContract.transactionNameAddMatriculationData:
                        matriculationDataUtil.checkParamsAddMatriculationData(ctx, paramList);
                        break;
                    case MatriculationDataContract.transactionNameUpdateMatriculationData:
                        matriculationDataUtil.checkParamsUpdateMatriculationData(ctx, paramList);
                        break;
                    case MatriculationDataContract.transactionNameGetMatriculationData:
                        matriculationDataUtil.checkParamsGetMatriculationData(ctx, paramList);
                        break;
                    case MatriculationDataContract.transactionNameAddEntriesToMatriculationData:
                        matriculationDataUtil.checkParamsAddEntriesToMatriculationData(ctx, paramList);
                        break;
                    case MatriculationDataContract.transactionNameGetVersion:
                        break;
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case AdmissionContract.contractName:
                switch (transactionName) {
                    case AdmissionContract.transactionNameAddAdmission:
                        admissionUtil.checkParamsAddAdmission(ctx, paramList);
                        break;
                    case AdmissionContract.transactionNameDropAdmission:
                        admissionUtil.checkParamsDropAdmission(ctx, paramList);
                        break;
                    case AdmissionContract.transactionNameGetCourseAdmissions:
                        // pass
                        break;
                    case AdmissionContract.transactionNameGetExamAdmissions:
                        admissionUtil.checkParamsGetExamAdmission(ctx, paramList);
                        break;
                    case AdmissionContract.transactionNameGetVersion:
                        break;
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case GroupContract.contractName:
                switch (transactionName) {
                    case GroupContract.transactionNameAddUserToGroup:
                        groupUtil.checkParamsAddUserToGroup(ctx, paramList);
                        break;
                    case GroupContract.transactionNameRemoveUserFromGroup:
                        groupUtil.checkParamsRemoveUserFromGroup(ctx, paramList);
                        break;
                    case GroupContract.transactionNameRemoveUserFromAllGroups:
                        groupUtil.checkParamsRemoveUserFromAllGroups(paramList);
                        break;
                    case GroupContract.transactionNameGetAllGroups:
                        // pass, for there are no parameters
                        break;
                    case GroupContract.transactionNameGetUsersForGroup:
                        groupUtil.checkParamsGetUsersForGroup(ctx, paramList);
                        break;
                    case GroupContract.transactionNameGetGroupsForUser:
                        groupUtil.checkParamsGetGroupsForUser(paramList);
                        break;
                    case GroupContract.transactionNameGetVersion:
                        break;
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                   default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case ExaminationRegulationContract.contractName:
                switch (transactionName) {
                    case ExaminationRegulationContract.transactionNameAddExaminationRegulation:
                        examinationRegulationUtil.checkParamsAddExaminationRegulation(ctx, paramList);
                        break;
                    case ExaminationRegulationContract.transactionNameGetExaminationRegulations:
                        examinationRegulationUtil.checkParamsGetExaminationRegulations(paramList);
                        break;
                    case ExaminationRegulationContract.transactionNameCloseExaminationRegulation:
                        examinationRegulationUtil.checkParamsCloseExaminationRegulation(ctx, paramList);
                        break;
                    case ExaminationRegulationContract.transactionNameGetVersion:
                        break;
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case CertificateContract.contractName:
                switch (transactionName) {
                    case CertificateContract.transactionNameAddCertificate:
                        certificateUtil.checkParamsAddCertificate(ctx, paramList);
                        break;
                    case CertificateContract.transactionNameUpdateCertificate:
                        certificateUtil.checkParamsUpdateCertificate(ctx, paramList);
                        break;
                    case CertificateContract.transactionNameGetCertificate:
                        certificateUtil.checkParamsGetCertificate(ctx, paramList);
                        break;
                    case CertificateContract.transactionNameGetVersion:
                        break;
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case ExamContract.contractName:
                switch (transactionName) {
                    case ExamContract.transactionNameAddExam:
                        examUtil.checkParamsAddExam(ctx, paramList);
                        break;
                    case ExamContract.transactionNameGetExams:
                        examUtil.checkParamsGetExams(ctx, paramList);
                        break;
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case ExamResultContract.contractName:
                switch (transactionName) {
                    case ExamResultContract.transactionNameAddExamResult:
                        examResultContractUtil.checkParamsAddExamResult(ctx, paramList);
                        break;
                    case ExamResultContract.transactionNameGetExamResultEntries:
                        examResultContractUtil.checkParamsGetExamResultEntries(ctx, paramList);
                        break;
                    case CertificateContract.transactionNameGetVersion:
                        break;
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case "":
                throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyContractNameError()));
            default:
                throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getContractUnprocessableError(contractName)));
        }
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
}
