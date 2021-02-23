package de.upb.cs.uc4.chaincode.contract.admission;

import de.upb.cs.uc4.chaincode.contract.exam.ExamContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.admission.AbstractAdmission;
import de.upb.cs.uc4.chaincode.model.admission.CourseAdmission;
import de.upb.cs.uc4.chaincode.model.admission.ExamAdmission;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContractUtil;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.exam.Exam;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.time.Instant;
import java.util.ArrayList;
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

    public InvalidParameter getStudentNotMatriculatedParam(String parameterName) {
        return new InvalidParameter()
                .name(errorPrefix + "." + parameterName)
                .reason("The student is not matriculated in any examinationRegulation");
    }

    public InvalidParameter getAdmissionAlreadyExistsParam(String parameterName) {
        return new InvalidParameter()
                .name(errorPrefix + "." + parameterName)
                .reason("The student is already admitted for the exam.");
    }

    public InvalidParameter getCourseAdmissionNotExistsParam(String parameterName) {
        return new InvalidParameter()
                .name(errorPrefix + "." + parameterName)
                .reason("The student is not admitted in the course of the exam.");
    }

    public InvalidParameter getAdmissionExamNotExistsParam() {
        return new InvalidParameter()
                .name(errorPrefix + ".examId")
                .reason("The exam you are trying to admit for does not exist.");
    }

    public InvalidParameter getAdmissionNotPossibleParam() {
        return new InvalidParameter()
                .name(errorPrefix + ".timestamp")
                .reason("The exam you are trying to admit for is no longer admittable.");
    }

    public InvalidParameter getAdmissionExamNotAvailableParam(String parameterName) {
        return new InvalidParameter()
                .name(errorPrefix + "." + parameterName)
                .reason("The student is not matriculated in any examinationRegulation containing the module the exam is referencing.");
    }

    public List<CourseAdmission> getCourseAdmissions(ChaincodeStub stub, String enrollmentId, String courseId, String moduleId) {
        return this.getAllStates(stub, CourseAdmission.class).stream()
                .filter(item -> enrollmentId.isEmpty() || item.getEnrollmentId().equals(enrollmentId))
                .filter(item -> courseId.isEmpty() || item.getCourseId().equals(courseId))
                .filter(item -> moduleId.isEmpty() || item.getModuleId().equals(moduleId))
                .collect(Collectors.toList());
    }

    public List<ExamAdmission> getExamAdmissions(ChaincodeStub stub, List<String> admissionIds, String enrollmentId, List<String> examIds) {
        return this.getAllStates(stub, ExamAdmission.class).stream()
                .filter(item -> enrollmentId.isEmpty() || item.getEnrollmentId().equals(enrollmentId))
                .filter(item -> admissionIds.isEmpty() || admissionIds.contains(item.getAdmissionId()))
                .filter(item -> examIds.isEmpty() || examIds.contains(item.getExamId()))
                .collect(Collectors.toList());
    }

    public boolean checkStudentMatriculated(ChaincodeStub stub, AbstractAdmission admission) {
        MatriculationDataContractUtil matUtil = new MatriculationDataContractUtil();

        try {
            matUtil.getState(stub, admission.getEnrollmentId(), MatriculationData.class);
        } catch (LedgerAccessError e) {
            return false;
        }
        return true;
    }

    public boolean checkModuleAvailable(ChaincodeStub stub, CourseAdmission admission) {
        return studentListensToModule(stub, admission.getEnrollmentId(), admission.getModuleId());
    }

    public boolean checkExamAvailableForStudent(ChaincodeStub stub, ExamAdmission admission) {
        if(!checkExamExists(stub, admission)){
            return true; // the simpler error is already reported
        }

        // check
        try {
            Exam exam = new ExamContractUtil().getState(stub, admission.getExamId(), Exam.class);
            return studentListensToModule(stub, admission.getEnrollmentId(), exam.getModuleId());
        } catch (LedgerAccessError e) {
            return false;
        }
    }

    private boolean studentListensToModule(ChaincodeStub stub, String enrollmentId, String moduleId){
        ExaminationRegulationContractUtil erUtil = new ExaminationRegulationContractUtil();
        MatriculationDataContractUtil matUtil = new MatriculationDataContractUtil();
        try {
            MatriculationData matriculationData = matUtil.getState(stub, enrollmentId, MatriculationData.class);
            List<SubjectMatriculation> matriculations = matriculationData.getMatriculationStatus();
            for (SubjectMatriculation matriculation : matriculations) {
                String examinationRegulationIdentifier = matriculation.getFieldOfStudy();
                ExaminationRegulation examinationRegulation = erUtil.getState(stub, examinationRegulationIdentifier, ExaminationRegulation.class);
                List<ExaminationRegulationModule> modules = examinationRegulation.getModules();
                for (ExaminationRegulationModule module : modules) {
                    if (module.getId().equals(moduleId)) {
                        return true;
                    }
                }
            }
        } catch (LedgerAccessError e) {
            return false;
        }
        return false;
    }

    public boolean checkExamAdmittable(ChaincodeStub stub, ExamAdmission admission) {
        if(!checkExamExists(stub, admission)){
            return true; // the simpler error is already reported
        }

        // check
        try {
            Exam exam = new ExamContractUtil().getState(stub, admission.getExamId(), Exam.class);
            return exam.getAdmittableUntil().isAfter(Instant.now());
        } catch (LedgerAccessError e) {
            return false;
        }
    }

    public boolean checkExamExists(ChaincodeStub stub, ExamAdmission admission) {
        try {
            Exam exam = new ExamContractUtil().getState(stub, admission.getExamId(), Exam.class);
            if (exam != null){
                return true;
            }
        } catch (LedgerAccessError e) {
            return false;
        }
        return false;
    }

    public boolean checkExamAdmissionNotAlreadyExists(ChaincodeStub stub, ExamAdmission admission) {
        if(!checkExamExists(stub, admission)){
            return true; // the simpler error is already reported
        }

        // check
        List<ExamAdmission> examAdmissions = this.getAllStates(stub, ExamAdmission.class);
        return examAdmissions.stream().noneMatch(item ->
                item.getExamId().equals(admission.getExamId())
                && item.getEnrollmentId().equals(admission.getEnrollmentId()));
    }
    public boolean checkCourseAdmissionExists(ChaincodeStub stub, ExamAdmission admission) {
        if(!checkExamExists(stub, admission)){
            return true; // the simpler error is already reported
        }

        // check
        try{
            Exam exam = new ExamContractUtil().getState(stub, admission.getExamId(), Exam.class);
            List<CourseAdmission> courseAdmissions = this.getAllStates(stub, CourseAdmission.class);
            return courseAdmissions.stream().anyMatch(item ->
                    item.getCourseId().equals(exam.getCourseId())
                    && item.getEnrollmentId().equals(admission.getEnrollmentId()));
        } catch (LedgerAccessError e){
            return false;
        }
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

        List<InvalidParameter> invalidParams = new ArrayList<>();
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
        List<InvalidParameter> invalidParams = new ArrayList<>();
        try {
            GsonWrapper.fromJson(params[0], String[].class);
        } catch (Exception e) {
            invalidParams.add(getUnparsableParam("admissionIds"));
        }
        try {
            GsonWrapper.fromJson(params[2], String[].class);
        } catch (Exception e) {
            invalidParams.add(getUnparsableParam("examIds"));
        }

        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
    }

    @Override
    public <T> List<T> getAllStates(ChaincodeStub stub, Class<T> c) {
        List<AbstractAdmission> states = super.getAllStates(stub, AbstractAdmission.class);
        return (List<T>) states.stream().filter(item -> item.getClass() == c).collect(Collectors.toList());
    }
}
