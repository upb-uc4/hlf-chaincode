package de.upb.cs.uc4.chaincode.contract.examresult;

import com.google.common.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContractUtil;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.examresult.ExamResult;
import de.upb.cs.uc4.chaincode.model.examresult.ExamResultEntry;
import de.upb.cs.uc4.chaincode.model.ExaminationRegulationModule;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.model.examresult.GradeType;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExamResultContractUtil extends ContractUtil {
    private final CertificateContractUtil certUtil = new CertificateContractUtil();
    private final ExaminationRegulationContractUtil exRegUtil = new ExaminationRegulationContractUtil();
    public ExamResultContractUtil() {
        keyPrefix = "examResultEntries";
        thing = "ExamResult";
        identifier = "";
    }

    public InvalidParameter getModuleForExamIdDoesNotExistParam(int index) {
        return new InvalidParameter()
                .name(errorPrefix + "[" + index + "].examId")
                .reason("There is no module for the given examId");
    }

    public InvalidParameter getNoCertificateForEnrollmentIdParam(int index) {
        return new InvalidParameter()
                .name(errorPrefix + "[" + index + "].enrollmentId")
                .reason("The enrollmentId must have a certificate");
    }

    public InvalidParameter getInvalidExamIdParam(int index) {
        return new InvalidParameter()
                .name(errorPrefix + "[" + index + "].examId")
                .reason("The examId must have format <courseId>:<moduleId>:<type>:<date>");
    }

    public InvalidParameter getDistinctExamIdsParam() {
        return new InvalidParameter()
                .name(errorPrefix)
                .reason("There are exam-result entries for different examIds");
    }

    public InvalidParameter getIncorrectGradeParam(int index) {
        return new InvalidParameter()
                .name(errorPrefix + "[" + index + "].grade")
                .reason("Grade must be AnyOf(\"1.0\", \"1.3\", \"1.7\", \"2.0\", \"2.3\", \"2.7\", \"3.0\", \"3.3\", \"3.7\", \"4.0\", \"5.0\")");
    }

    public String getKey(ExamResult examResult) throws NoSuchAlgorithmException {
        return hashAndEncodeBase64url(GsonWrapper.toJson(examResult));
    }

    public List<InvalidParameter> getParameterErrorsForExamResultEntry(Context ctx, ExamResultEntry entry, int index) {

        ChaincodeStub stub = ctx.getStub();
        List<InvalidParameter> invalidParams = getParameterErrorsForExamId(ctx, entry.getExamId(), index);

        if (entry.getGrade() == null) {
            invalidParams.add(getIncorrectGradeParam(index));
        }
        if (valueUnset(entry.getEnrollmentId())) {
            invalidParams.add(getEmptyEnrollmentIdParam());
        }
        if(!(certUtil.keyExists(stub, entry.getEnrollmentId()))){
            invalidParams.add(getNoCertificateForEnrollmentIdParam(index));
        }
        return invalidParams;
    }

    public List<InvalidParameter> getParameterErrorsForExamId(Context ctx, String examId, int index) {
        String moduleId = null;
        try {
            moduleId = getModuleFromKey(examId);
        } catch (ArrayIndexOutOfBoundsException e) {
            return Collections.singletonList(getInvalidExamIdParam(index));
        }
        // TODO Check, if examId exists
        if (!exRegUtil.checkModuleAvailable(ctx.getStub(), moduleId)) {
            return Collections.singletonList(getModuleForExamIdDoesNotExistParam(index));
        }
        return new ArrayList<>();
    }

    private String getModuleFromKey(String examId) throws ArrayIndexOutOfBoundsException {
        return examId.split(":")[1];
    }

    public boolean checkModuleId(HashSet<ExaminationRegulationModule> validModules, String moduleId){
        Iterator<ExaminationRegulationModule> it = validModules.iterator();
        while(it.hasNext()){
            if(it.next().getId().equals(moduleId)){
                return true;
            }
        }
        return false;
    }

    public void checkParamsAddExamResult(Context ctx, List<String> params) throws ParameterError {
        if (params.size() != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String examResult = params.get(0);

        ChaincodeStub stub = ctx.getStub();

        ExamResult newExamResult;
        try {
            newExamResult = GsonWrapper.fromJson(examResult, ExamResult.class);
        } catch (Exception e) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(getUnparsableParam("examResult"))));
        }
        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        List<ExamResultEntry> examResultEntries = newExamResult.getExamResultEntries();
        //for each entry
        for (int i=0; i< examResultEntries.size();i++){
            ExamResultEntry entry = examResultEntries.get(i);
            invalidParams.addAll(getParameterErrorsForExamResultEntry(ctx, entry, i));
        }
        if (distinctExamIds(examResultEntries)) {
            invalidParams.add(getDistinctExamIdsParam());
        }
        // todo: Check All students referenced must be admitted (?)
        // todo: Check All students admitted must be referenced (?)
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }

    }

    private boolean distinctExamIds(List<ExamResultEntry> entries) {
        return entries.stream().map(entry -> entry.getExamId()).distinct().count() > 1;
    }

    public void checkParamsGetExamResultEntries(Context ctx, List<String> params) throws ParameterError {
        if (params.size() != 2) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String enrollmentId = params.get(0);
        String examIds = params.get(1);

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        try {
            GsonWrapper.fromJson(examIds, listType);
        } catch (Exception e) {
            invalidParams.add(getUnparsableParam("examIds"));
        }
        if (valueUnset(enrollmentId)) {
            invalidParams.add(getEmptyEnrollmentIdParam());
        }
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
    }

    public List<ExamResultEntry> getExamResultEntries(
            ChaincodeStub stub,
            final String enrollmentId,
            final List<String> examIds) {

        return this.getAllStates(stub, ExamResult.class).stream()
                .map(result -> result.getExamResultEntries().stream())
                .reduce((entries1, entries2) -> Stream.concat(entries1, entries2))
                .orElse(Stream.empty())
                .filter(item -> enrollmentId.isEmpty() ||
                        enrollmentId.equals(item.getEnrollmentId()))
                .filter(item -> valueUnset(examIds) ||
                        examIds.contains(item.getExamId()))
                .collect(Collectors.toList());
    }
}
