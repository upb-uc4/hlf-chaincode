package de.upb.cs.uc4.chaincode.contract.admission;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.admission.AbstractAdmission;
import de.upb.cs.uc4.chaincode.model.admission.AdmissionType;
import de.upb.cs.uc4.chaincode.model.admission.CourseAdmission;
import de.upb.cs.uc4.chaincode.model.admission.ExamAdmission;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContractUtil;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

public class AdmissionContractUtil extends ContractUtil {
    public AdmissionContractUtil() {
        keyPrefix = "admission";
        errorPrefix = "admission";
        thing = "Admission";
        identifier = "admissionId";
    }

    public String getErrorPrefix() {
        return errorPrefix;
    }

    public InvalidParameter getInvalidTimestampParam() {
        return new InvalidParameter()
                .name(errorPrefix + ".timestamp")
                .reason("Timestamp must be the following format \"(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\", e.g. \"2020-12-31T23:59:59\"");
    }

    public InvalidParameter getInvalidTypeParam() {
        return new InvalidParameter()
                .name(errorPrefix + ".type")
                .reason("Type must be one of (Course|Exam)");
    }

    public InvalidParameter getInvalidModuleAvailable(String parameterName) {
        return new InvalidParameter()
                .name(errorPrefix + "." + parameterName)
                .reason("The student is not matriculated in any examinationRegulation containing the module he is trying to enroll in");
    }

    public List<CourseAdmission> getCourseAdmissions(ChaincodeStub stub, String enrollmentId, String courseId, String moduleId) {
        return this.getAllStates(stub, AbstractAdmission.class, CourseAdmission.class).stream()
                .filter(item -> enrollmentId.isEmpty() || item.getEnrollmentId().equals(enrollmentId))
                .filter(item -> courseId.isEmpty() || item.getCourseId().equals(courseId))
                .filter(item -> moduleId.isEmpty() || item.getModuleId().equals(moduleId))
                .collect(Collectors.toList());
    }

    public List<ExamAdmission> getExamAdmissions(ChaincodeStub stub, List<String> admissionIds, String enrollmentId, List<String> examIds) {
        return this.getAllStates(stub, AbstractAdmission.class, ExamAdmission.class).stream()
                .filter(item -> enrollmentId.isEmpty() || item.getEnrollmentId().equals(enrollmentId))
                .filter(item -> admissionIds.isEmpty() || admissionIds.contains(item.getAdmissionId()))
                .filter(item -> examIds.isEmpty() || examIds.contains(item.getExamId()))
                .collect(Collectors.toList());
    }

    public boolean checkModuleAvailable(ChaincodeStub stub, CourseAdmission admission) {
        ExaminationRegulationContractUtil erUtil = new ExaminationRegulationContractUtil();
        MatriculationDataContractUtil matUtil = new MatriculationDataContractUtil();

        try {
            MatriculationData matriculationData = matUtil.getState(stub, admission.getEnrollmentId(), MatriculationData.class);
            List<SubjectMatriculation> matriculations = matriculationData.getMatriculationStatus();
            for (SubjectMatriculation matriculation : matriculations) {
                String examinationRegulationIdentifier = matriculation.getFieldOfStudy();
                ExaminationRegulation examinationRegulation = erUtil.getState(stub, examinationRegulationIdentifier, ExaminationRegulation.class);
                List<ExaminationRegulationModule> modules = examinationRegulation.getModules();
                for (ExaminationRegulationModule module : modules) {
                    if (module.getId().equals(admission.getModuleId())) {
                        return true;
                    }
                }
            }
        } catch (LedgerAccessError e) {
            return false;
        }

        return false;
    }

    public void checkParamsAddAdmission(Context ctx, String[] params) throws ParameterError {
        if (params.length != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String admissionJson = params[0];

        ChaincodeStub stub = ctx.getStub();

        AbstractAdmission newAdmission;
        try {
            newAdmission = GsonWrapper.fromJson(admissionJson, AbstractAdmission.class);
            newAdmission.resetAdmissionId();
        } catch (Exception e) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(getUnparsableParam("admission"))));
        }

        if (keyExists(stub, newAdmission.getAdmissionId())) {
            throw new ParameterError(GsonWrapper.toJson(getConflictError()));
        }

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        invalidParams.addAll(newAdmission.getParameterErrors());
        invalidParams.addAll(newAdmission.getSemanticErrors(stub));
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
    }

    public void checkParamsDropAdmission(Context ctx, String[] params) throws LedgerAccessError, ParameterError {
        if (params.length != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String admissionId = params[0];

        ChaincodeStub stub = ctx.getStub();
        AbstractAdmission admission = getState(stub, admissionId, AbstractAdmission.class);
        admission.ensureIsDroppable(stub);
    }

    public void checkParamsGetExamAdmission(Context ctx, String[] params) throws LedgerAccessError, ParameterError {
        if (params.length != 3) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        Type listType = new TypeToken<String[]>() {}.getType();
        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        try {
            GsonWrapper.fromJson(params[0], listType);
        } catch (Exception e) {
            invalidParams.add(getUnparsableParam("admissionIds"));
        }
        try {
            GsonWrapper.fromJson(params[2], listType);
        } catch (Exception e) {
            invalidParams.add(getUnparsableParam("examIds"));        }
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
    }

    public <T1, T2> List<T2> getAllStates(ChaincodeStub stub, Class<T1> superClass, Class<T2> c) {
        ArrayList<T1> states = super.getAllStates(stub, superClass);
        return (List<T2>) states.stream().filter(item -> item.getClass() == c).collect(Collectors.toList());
    }
}
