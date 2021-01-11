package de.upb.cs.uc4.chaincode.helper;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContractUtil;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContractUtil;
import de.upb.cs.uc4.chaincode.contract.operation.OperationContractUtil;
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
            case "UC4.MatriculationData":
                switch (transactionName) {
                    case "addMatriculationData":
                        matriculationDataUtil.checkParamsAddMatriculationData(ctx, paramList.get(0));
                        break;
                    case "updateMatriculationData":
                        matriculationDataUtil.checkParamsUpdateMatriculationData(ctx, paramList.get(0));
                        break;
                    case "getMatriculationData":
                        matriculationDataUtil.checkParamsGetMatriculationData(ctx, paramList.get(0));
                        break;
                    case "addEntriesToMatriculationData":
                        matriculationDataUtil.checkParamsAddEntriesToMatriculationData(ctx, paramList.get(0), paramList.get(1));
                        break;
                    case "getVersion":
                        break;
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case "UC4.Admission":
                switch (transactionName) {
                    case "addAdmission":
                        admissionUtil.checkParamsAddAdmission(ctx, paramList.get(0));
                        break;
                    case "dropAdmission":
                        admissionUtil.checkParamsDropAdmission(ctx, paramList.get(0));
                        break;
                    case "getAdmissions":
                        // pass
                        break;
                    case "getVersion":
                        break;
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case "UC4.Group":
                switch (transactionName) {
                    case "addUserToGroup":
                        groupUtil.checkParamsAddUserToGroup(ctx, paramList.get(0), paramList.get(1));
                        break;
                    case "removeUserFromGroup":
                        groupUtil.checkParamsRemoveUserFromGroup(ctx, paramList.get(0), paramList.get(1));
                        break;
                    case "removeUserFromAllGroups":
                        groupUtil.checkParamsRemoveUserFromAllGroups(paramList.get(0));
                        break;
                    case "getAllGroups":
                        // pass, for there are no parameters
                        break;
                    case "getUsersForGroup":
                        groupUtil.checkParamsGetUsersForGroup(ctx, paramList.get(0));
                        break;
                    case "getGroupsForUser":
                        groupUtil.checkParamsGetGroupsForUser(paramList.get(0));
                        break;
                    case "getVersion":
                        break;
                   default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case "UC4.ExaminationRegulation":
                switch (transactionName) {
                    case "addExaminationRegulation":
                        examinationRegulationUtil.checkParamsAddExaminationRegulation(ctx, paramList.get(0));
                        break;
                    case "getExaminationRegulations":
                        examinationRegulationUtil.checkParamsGetExaminationRegulations(paramList.get(0));
                        break;
                    case "closeExaminationRegulation":
                        examinationRegulationUtil.checkParamsCloseExaminationRegulation(ctx, paramList.get(0));
                        break;
                    case "getVersion":
                        break;
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            case "UC4.Certificate":
                switch (transactionName) {
                    case "addCertificate":
                        certificateUtil.checkParamsAddCertificate(ctx, paramList.get(0), paramList.get(1));
                        break;
                    case "updateCertificate":
                        certificateUtil.checkParamsUpdateCertificate(ctx, paramList.get(0), paramList.get(1));
                        break;
                    case "getCertificate":
                        certificateUtil.checkParamsGetCertificate(ctx, paramList.get(0));
                        break;
                    case "getVersion":
                        break;
                    default:
                        throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getTransactionUnprocessableError(transactionName)));
                }
                break;
            default:
                throw new MissingTransactionError(GsonWrapper.toJson(operationUtil.getContractUnprocessableError(contractName)));
        }
    }
}
