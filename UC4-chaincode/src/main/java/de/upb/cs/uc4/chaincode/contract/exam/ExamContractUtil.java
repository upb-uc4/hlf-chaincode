package de.upb.cs.uc4.chaincode.contract.exam;

import com.google.common.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContractUtil;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.Exam;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
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

    public InvalidParameter getInvalidModuleAvailable(String parameterName) {
        return new InvalidParameter()
                .name(errorPrefix + "." + parameterName)
                .reason("The exam cannot be specified for the given module.");
    }

    public InvalidParameter getInvalidDate(String parameterName) {
        return new InvalidParameter()
                .name(errorPrefix + "." + parameterName)
                .reason("The exam date has to be in the future and after admittbale and droppable date.");
    }

    public InvalidParameter getInvalidAdmittableDate(String parameterName) {
        return new InvalidParameter()
                .name(errorPrefix + "." + parameterName)
                .reason("The admittable date has to be in the future and before droppable date and date of the exam.");
    }

    public InvalidParameter getInvalidDroppableDate(String parameterName) {
        return new InvalidParameter()
                .name(errorPrefix + "." + parameterName)
                .reason("The droppable date has to be in the future and after admittable date and before date of the exam.");
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

        if (!new ExaminationRegulationContractUtil().moduleExists(stub, exam.getModuleId())) {
            invalidParameters.add(getInvalidModuleAvailable("moduleId"));
        }

        Instant now = ZonedDateTime.now().toInstant();

        if (initializedAndBefore(exam.getDate(), now)) {
            invalidParameters.add(getInvalidDate("date"));
        }

        if (initializedAndBefore(exam.getAdmittableUntil(), now)
                || initializedAndBefore(exam.getDate(), exam.getAdmittableUntil())
                || initializedAndBefore(exam.getDroppableUntil(), exam.getAdmittableUntil())
        ) {
            invalidParameters.add(getInvalidAdmittableDate("admittableUntil"));
        }

        if (initializedAndBefore(exam.getDroppableUntil(), now)
                || initializedAndBefore(exam.getDate(), exam.getDroppableUntil())
                || initializedAndBefore(exam.getDroppableUntil(), exam.getAdmittableUntil())
        ) {
            invalidParameters.add(getInvalidDroppableDate("droppableUntil"));
        }

        return invalidParameters;
    }

    private boolean initializedAndBefore (Instant before, Instant after) {
        return before != null && after != null && before.isBefore(after);
    }

    /**
     * Returns a list of errors describing everything wrong with the given exam parameters
     *
     * @param exam exam to return errors for
     * @return a list of all errors found for the given exam
     */

    public ArrayList<InvalidParameter> getParameterErrorsForExam(
            Exam exam) {

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();

        if (valueUnset(exam.getExamId())) {
            invalidParams.add(getEmptyEnrollmentIdParam(errorPrefix + ".examId"));
        }
        if (valueUnset(exam.getCourseId())) {
            invalidParams.add(getEmptyInvalidParameter(errorPrefix + ".courseId"));
        }
        if (valueUnset(exam.getLecturerEnrollmentId())) {
            invalidParams.add(getEmptyInvalidParameter(errorPrefix + ".lecturerEnrollmentId"));
        }
        if (valueUnset(exam.getModuleId())) {
            invalidParams.add(getEmptyInvalidParameter(errorPrefix + ".moduleId"));
        }
        if (valueUnset(exam.getDate())) {
            invalidParams.add(getInvalidTimestampParam("date"));
        }
        if (valueUnset(exam.getType())) {
            invalidParams.add(getEmptyInvalidParameter(errorPrefix + ".type"));
        }
        if (valueUnset(exam.getAdmittableUntil())) {
            invalidParams.add(getInvalidTimestampParam("admittableUntil"));
        }
        if (valueUnset(exam.getDroppableUntil())) {
            invalidParams.add(getInvalidTimestampParam("droppableUntil"));
        }

        return invalidParams;
    }

    public List<Exam> getExams(
            ChaincodeStub stub,
            final List<String> examIds,
            final List<String> courseIds,
            final List<String> lecturerIds,
            final List<String> moduleIds,
            final List<String> types,
            final Instant admittableAt,
            final Instant droppableAt) {

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
                .filter(item -> valueUnset(admittableAt) ||
                        item.getAdmittableUntil().isAfter(admittableAt))
                .filter(item -> valueUnset(droppableAt) ||
                        item.getDroppableUntil().isAfter(droppableAt))
                .collect(Collectors.toList());
    }

    public void checkParamsAddExam(Context ctx, String[] params) throws ParameterError {
        if (params.length != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }

        String examJson = params[0];

        ChaincodeStub stub = ctx.getStub();

        Exam exam;
        try {
            exam = GsonWrapper.fromJson(examJson, Exam.class);
            exam.resetExamId();
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

    public void checkParamsGetExams(Context ctx, String[] params) throws SerializableError {
        if (params.length != 7) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        final String examIds = params[0];
        final String courseIds = params[1];
        final String lecturerIds = params[2];
        final String moduleIds = params[3];
        final String types = params[4];
        final String admittableAt = params[5];
        final String droppableAt = params[6];

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        try {
            GsonWrapper.fromJson(examIds, listType);
        } catch (Exception e) {
            invalidParams.add(getUnparsableParam("examIds"));
        }
        try {
            GsonWrapper.fromJson(courseIds, listType);
        } catch (Exception e) {
            invalidParams.add(getUnparsableParam("courseIds"));
        }
        try {
            GsonWrapper.fromJson(lecturerIds, listType);
        } catch (Exception e) {
            invalidParams.add(getUnparsableParam("lecturerIds"));
        }
        try {
            GsonWrapper.fromJson(moduleIds, listType);
        } catch (Exception e) {
            invalidParams.add(getUnparsableParam("moduleIds"));
        }

        try {
            GsonWrapper.fromJson(types, listType);
        } catch (Exception e) {
            invalidParams.add(getUnparsableParam("types"));
        }
        if (GsonWrapper.fromJson(admittableAt, Date.class) == null && !admittableAt.isEmpty()) {
            invalidParams.add(getUnparsableParam("admittableAt"));
        }
        if (GsonWrapper.fromJson(droppableAt, Date.class) == null && ! droppableAt.isEmpty()) {
            invalidParams.add(getUnparsableParam("droppableAt"));
        }
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
    }
}