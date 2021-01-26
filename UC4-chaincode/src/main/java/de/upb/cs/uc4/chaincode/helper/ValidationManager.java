package de.upb.cs.uc4.chaincode.helper;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContract;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContract;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContractUtil;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContract;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.contract.group.GroupContract;
import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContract;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContractUtil;
import de.upb.cs.uc4.chaincode.contract.operation.OperationContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.parameter.MissingTransactionError;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import org.hyperledger.fabric.contract.Context;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ValidationManager {
    private static AdmissionContractUtil admissionUtil = new AdmissionContractUtil();
    private static OperationContractUtil operationUtil = new OperationContractUtil();
    private static CertificateContractUtil certificateUtil = new CertificateContractUtil();
    private static ExaminationRegulationContractUtil examinationRegulationUtil = new ExaminationRegulationContractUtil();
    private static GroupContractUtil groupUtil = new GroupContractUtil();
    private static MatriculationDataContractUtil matriculationDataUtil = new MatriculationDataContractUtil();

    public static void validateParams(Context ctx, String contractName, String transactionName, String params) throws SerializableError {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        List<String> paramList = GsonWrapper.fromJson(params, listType);
        switch (contractName) {
            case MatriculationDataContract.contractName:
                switch (transactionName) {
                    case "addMatriculationData":
                        matriculationDataUtil.checkParamsAddMatriculationData(ctx, paramList);
                        break;
                    case "updateMatriculationData":
                        matriculationDataUtil.checkParamsUpdateMatriculationData(ctx, paramList);
                        break;
                    case "getMatriculationData":
                        matriculationDataUtil.checkParamsGetMatriculationData(ctx, paramList);
                        break;
                    case "addEntriesToMatriculationData":
                        matriculationDataUtil.checkParamsAddEntriesToMatriculationData(ctx, paramList);
                        break;
                    case "getVersion":
                        break;
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case AdmissionContract.contractName:
                switch (transactionName) {
                    case "addAdmission":
                        admissionUtil.checkParamsAddAdmission(ctx, paramList);
                        break;
                    case "dropAdmission":
                        admissionUtil.checkParamsDropAdmission(ctx, paramList);
                        break;
                    case "getAdmissions":
                        // pass
                        break;
                    case "getVersion":
                        break;
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case GroupContract.contractName:
                switch (transactionName) {
                    case "addUserToGroup":
                        groupUtil.checkParamsAddUserToGroup(ctx, paramList);
                        break;
                    case "removeUserFromGroup":
                        groupUtil.checkParamsRemoveUserFromGroup(ctx, paramList);
                        break;
                    case "removeUserFromAllGroups":
                        groupUtil.checkParamsRemoveUserFromAllGroups(paramList);
                        break;
                    case "getAllGroups":
                        // pass, for there are no parameters
                        break;
                    case "getUsersForGroup":
                        groupUtil.checkParamsGetUsersForGroup(ctx, paramList);
                        break;
                    case "getGroupsForUser":
                        groupUtil.checkParamsGetGroupsForUser(paramList);
                        break;
                    case "getVersion":
                        break;
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                   default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case ExaminationRegulationContract.contractName:
                switch (transactionName) {
                    case "addExaminationRegulation":
                        examinationRegulationUtil.checkParamsAddExaminationRegulation(ctx, paramList);
                        break;
                    case "getExaminationRegulations":
                        examinationRegulationUtil.checkParamsGetExaminationRegulations(paramList);
                        break;
                    case "closeExaminationRegulation":
                        examinationRegulationUtil.checkParamsCloseExaminationRegulation(ctx, paramList);
                        break;
                    case "getVersion":
                        break;
                    case "":
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getEmptyTransactionNameError()));
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case CertificateContract.contractName:
                switch (transactionName) {
                    case "addCertificate":
                        certificateUtil.checkParamsAddCertificate(ctx, paramList);
                        break;
                    case "updateCertificate":
                        certificateUtil.checkParamsUpdateCertificate(ctx, paramList);
                        break;
                    case "getCertificate":
                        certificateUtil.checkParamsGetCertificate(ctx, paramList);
                        break;
                    case "getVersion":
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
}
