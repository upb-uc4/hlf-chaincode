package de.upb.cs.uc4.chaincode.contract.examresult;

import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContract;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.Admission;
import de.upb.cs.uc4.chaincode.model.ExamResult;
import de.upb.cs.uc4.chaincode.model.ExamResultEntry;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.contract.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExamResultContractUtil extends ContractUtil {

    public ExamResultContractUtil() {
        keyPrefix = "examResult";
        thing = "ExamResult";
        identifier = "";
    }

    public InvalidParameter getEmptyExamIdParam() {
        return new InvalidParameter()
                .name(errorPrefix + ".examId")
                .reason("The examId must not be empty or null.");
    }

    public InvalidParameter getEmptyModuleIdParam() {
        return new InvalidParameter()
                .name(errorPrefix + ".moduleId")
                .reason("The moduleId must not be empty or null.");
    }

    public InvalidParameter getIncorrectGradeParam() {
        return new InvalidParameter()
                .name(errorPrefix + ".grade")
                .reason("Grade must be AnyOf(\"1.0\", \"1.3\", \"1.7\", \"2.0\", \"2.3\", \"2.7\", \"3.0\", \"3.3\", \"3.7\", \"4.0\", \"5.0\")");
    }

    private ArrayList<InvalidParameter> getErrorForEnrollmentId(final String enrollmentId){
        ArrayList<InvalidParameter> list = new ArrayList<>();
        if (enrollmentId == null || enrollmentId.equals("")) {
            list.add(getEmptyEnrollmentIdParam());
        }
        return list;
    }

    private ArrayList<InvalidParameter> getErrorForExamId(final String examId){
        ArrayList<InvalidParameter> list = new ArrayList<>();
        if (examId == null || examId.equals("")) {
            list.add(getEmptyExamIdParam());
        }
        return list;
    }

    private ArrayList<InvalidParameter> getErrorForModuleId(final String moduleId){
        ArrayList<InvalidParameter> list = new ArrayList<>();
        if (moduleId == null || moduleId.equals("")) {
            list.add(getEmptyModuleIdParam());
        }
        return list;
    }

    private ArrayList<InvalidParameter> getErrorForGrade(final String grade){
        ArrayList<InvalidParameter> list = new ArrayList<>();
        List<String> gradesList = new ArrayList<>(){{add("1.0");add("1.3");add("1.7"); add("2.0");add("2.3");add("2.7");add("3.0");
                add("3.3");add("3.7");add("4.0");add("5.0");}};
        if (!(gradesList.contains(grade))) {
            list.add(getIncorrectGradeParam());
        }
        return list;
    }

    public ArrayList<InvalidParameter> getParameterErrorsForExamResultEntry(
            ExamResultEntry entry) {

        ArrayList<InvalidParameter> invalidparams = new ArrayList<>();
        List<String> gradesList = new ArrayList<>(){{add("1.0");add("1.3");add("1.7"); add("2.0");add("2.3");add("2.7");add("3.0");
            add("3.3");add("3.7");add("4.0");add("5.0");}};

        if (!(gradesList.contains(entry.getGrade()))) {
            invalidparams.add(getIncorrectGradeParam());
        }
        if (entry.getExamId() == null || entry.getExamId().equals("")) {
            invalidparams.add(getEmptyExamIdParam());
        }
        if (entry.getEnrollmentId() == null || entry.getEnrollmentId().equals("")) {
            invalidparams.add(getEmptyEnrollmentIdParam());
        }
        //todo: check module ID not null or an empty String
        return invalidparams;
    }

    public void checkParamsAddExamResult(Context ctx, ExamResult examResult) throws ParameterError {

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        List<ExamResultEntry> examResultEntries=examResult.getExamResultEntries();
        //for each entry
        for (int i=0; i< examResultEntries.size();i++){
            ExamResultEntry entry= examResultEntries.get(i);
            // check if enrollmentId, examID, module ID are empty and grade is correct
            invalidParams.addAll(getParameterErrorsForExamResultEntry(entry));
            // Check, if Certificate for enrollmentId exists

            // Check, if examId exists
            // Check, if there exists an ExaminationRegulation which contains the moduleId
        }

        // All entries refer to the same examId
        // All students referenced must be admitted
        // All students admitted must be referenced
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }

    }

    public void checkParamsGetExamResultEntries(Context ctx, List<String> params) throws ParameterError {
        // examIds: Check, if valid jsonList of String
    }
}
