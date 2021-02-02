package de.upb.cs.uc4.chaincode.contract.exam;

import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContractUtil;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExamContractUtil extends ContractUtil {

    public ExamContractUtil() {
        keyPrefix = "exam";
        thing = "Exam";
        errorPrefix = thing.toLowerCase();
        identifier = "examId";
    }

    public InvalidParameter getInvalidUserNotRegistered() {
        return new InvalidParameter()
                .name(errorPrefix + ".lecturerEnrollmentId")
                .reason("The user trying to add an exam is not registered in the system.");
    }

    public InvalidParameter getInvalidTimestampParam() {
        return new InvalidParameter()
                .name(errorPrefix + ".timestamp")
                .reason("Timestamp must be the following format \"(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\", e.g. \"2020-12-31T23:59:59\"");
    }

    public InvalidParameter getInvalidModuleAvailable(String parameterName) {
        return new InvalidParameter()
                .name(errorPrefix + "." + parameterName)
                .reason("The student is not matriculated in any examinationRegulation containing the module he is trying to enroll in");
    }

    /**
     * Returns a list of errors describing everything wrong with the given exam parameters
     *
     * @param exam exam to return errors for
     * @return a list of all errors found for the given exam
     */
    public ArrayList<InvalidParameter> getSemanticErrorsForExam(
            ChaincodeStub stub,
            Exam exam) {

        CertificateContractUtil certificateUtil = new CertificateContractUtil();

        ArrayList<InvalidParameter> invalidParameters = new ArrayList<>();

        if (!(certificateUtil.keyExists(stub, exam.getLecturerEnrollmentId()))) {
            invalidParameters.add(getInvalidUserNotRegistered());
        }

        if (!this.checkModuleAvailable(stub, exam)) {
            invalidParameters.add(getInvalidModuleAvailable("moduleId"));
        }

        if(!(exam.getDate().isAfter(LocalDateTime.now()))){
            invalidParameters.add(getInvalidModuleAvailable("date"));
        }

        if(!(exam.getAdmittableUntil().isAfter(LocalDateTime.now()))){
            invalidParameters.add(getInvalidModuleAvailable("admittableAt"));
        }

        if(!(exam.getDroppableUntil().isAfter(LocalDateTime.now()))){
            invalidParameters.add(getInvalidModuleAvailable("droppableAt"));
        }

        if(!(exam.getAdmittableUntil().isBefore(exam.getDate()))){
            invalidParameters.add(getInvalidModuleAvailable("admittableAt"));
        }

        if(!(exam.getDroppableUntil().isBefore(exam.getDate()))){
            invalidParameters.add(getInvalidModuleAvailable("droppableAt"));
        }

        if(!(exam.getAdmittableUntil().isBefore(exam.getDroppableUntil()))){
            invalidParameters.add(getInvalidModuleAvailable("admittableAt"));
        }

        return invalidParameters;
    }

    private boolean checkModuleAvailable(ChaincodeStub stub, Exam exam) {
        ExaminationRegulationContractUtil erUtil = new ExaminationRegulationContractUtil();
        MatriculationDataContractUtil matUtil = new MatriculationDataContractUtil();

        try {
            MatriculationData matriculationData = matUtil.getState(stub, exam.getLecturerEnrollmentId(), MatriculationData.class);
            List<SubjectMatriculation> matriculations = matriculationData.getMatriculationStatus();
            for (SubjectMatriculation matriculation : matriculations) {
                String examinationRegulationIdentifier = matriculation.getFieldOfStudy();
                ExaminationRegulation examinationRegulation = erUtil.getState(stub, examinationRegulationIdentifier, ExaminationRegulation.class);
                List<ExaminationRegulationModule> modules = examinationRegulation.getModules();
                for (ExaminationRegulationModule module : modules) {
                    if (module.getId().equals(exam.getModuleId())) {
                        return true;
                    }
                }
            }
        } catch (LedgerAccessError e) {
            return false;
        }

        return false;
    }

    /**
     * Returns a list of errors describing everything wrong with the given exam parameters
     *
     * @param exam exam to return errors for
     * @return a list of all errors found for the given exam
     */

    public ArrayList<InvalidParameter> getParameterErrorsForExam(
            Exam exam) {

        ArrayList<InvalidParameter> invalidparams = new ArrayList<>();

        if (valueUnset(exam.getExamId())) {
            invalidparams.add(getEmptyEnrollmentIdParam(errorPrefix + ".examId"));
        }
        if (valueUnset(exam.getCourseId())) {
            invalidparams.add(getEmptyInvalidParameter(errorPrefix + ".courseId"));
        }
        if (valueUnset(exam.getLecturerEnrollmentId())) {
            invalidparams.add(getEmptyInvalidParameter(errorPrefix + ".lecturerId"));
        }
        if (valueUnset(exam.getModuleId())) {
            invalidparams.add(getEmptyInvalidParameter(errorPrefix + ".moduleId"));
        }
        if (valueUnset(exam.getType())) {
            invalidparams.add(getEmptyInvalidParameter(errorPrefix + ".type"));
        }
        if (valueUnset(exam.getAdmittableUntil())) {
            invalidparams.add(getInvalidTimestampParam());
        }
        if (valueUnset(exam.getDroppableUntil())) {
            invalidparams.add(getInvalidTimestampParam());
        }

        return invalidparams;
    }

    public List<Exam> getExams(
        ChaincodeStub stub,
        final List<String> examIds,
        final List<String> courseIds,
        final List<String> lecturerIds,
        final List<String> moduleIds,
        final List<String> types,
        final String admittableAt,
        final String droppableAt) {

            return this.getAllStates(stub, Exam.class).stream()
                    .filter(item -> examIds.isEmpty() ||
                            examIds.contains(item.getExamId()))
                    .filter(item -> courseIds.isEmpty() ||
                            courseIds.contains(item.getCourseId()))
                    .filter(item -> lecturerIds.isEmpty() ||
                            lecturerIds.contains(item.getLecturerEnrollmentId()))
                    .filter(item -> moduleIds.isEmpty() ||
                            moduleIds.contains(item.getModuleId()))
                    .filter(item -> types.isEmpty() ||
                            types.contains(item.getType()))
                    .filter(item -> admittableAt.isEmpty() ||
                            item.getAdmittableUntil().equals(admittableAt))
                    .filter(item -> droppableAt.isEmpty() ||
                            item.getDroppableUntil().equals(droppableAt))
                    .collect(Collectors.toList());
    }

    public void checkParamsAddExam(Context ctx, List<String> params) throws ParameterError {
        if (params.size() != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }

        String examJson = params.get(0);

        ChaincodeStub stub = ctx.getStub();

        Exam exam;
        try {
            exam = GsonWrapper.fromJson(examJson, Exam.class);
        } catch (Exception e) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(getUnparsableParam("exam"))));
        }

        if (keyExists(stub, exam.getExamId())) {
            throw new ParameterError(GsonWrapper.toJson(getConflictError()));
        }

        ArrayList<InvalidParameter> invalidParams = getParameterErrorsForExam(exam);
        invalidParams.addAll(getSemanticErrorsForExam(stub, exam));
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
    }

    public void checkParamsGetExams(Context ctx, List<String> params) throws SerializableError {
        if (params.size() != 7) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String examJson = params.get(0);

        Exam exam;
        try {
            exam = GsonWrapper.fromJson(examJson, Exam.class);
        } catch (Exception e) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(getUnparsableParam("exam"))));
        }

        ArrayList<InvalidParameter> invalidParams = getParameterErrorsForExam(exam);
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }

    }
}