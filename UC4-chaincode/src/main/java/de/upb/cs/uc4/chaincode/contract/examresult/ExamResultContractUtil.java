package de.upb.cs.uc4.chaincode.contract.examresult;

import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContractUtil;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.ExamResult;
import de.upb.cs.uc4.chaincode.model.ExamResultEntry;
import de.upb.cs.uc4.chaincode.model.ExaminationRegulationModule;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ExamResultContractUtil extends ContractUtil {
    private final CertificateContractUtil certUtil = new CertificateContractUtil();
    private final ExaminationRegulationContractUtil exRegUtil = new ExaminationRegulationContractUtil();
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

    public InvalidParameter getNoCertificateForEnrollmentIDParam() {
        return new InvalidParameter()
                .name(errorPrefix + ".enrollmentId")
                .reason("The enrollmentId must have a certificate.");
    }
    public InvalidParameter getNoExaminationRegulationModuleForModuleIDParam() {
        return new InvalidParameter()
                .name(errorPrefix + ".moduleId")
                .reason("The moduleId must have an ExaminationRegulation which contains the moduleId.");
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
        List<String> gradesList = new ArrayList<String>(){{add("1.0");add("1.3");add("1.7"); add("2.0");add("2.3");add("2.7");add("3.0");
                add("3.3");add("3.7");add("4.0");add("5.0");}};
        if (!(gradesList.contains(grade))) {
            list.add(getIncorrectGradeParam());
        }
        return list;
    }

    public ArrayList<InvalidParameter> getParameterErrorsForExamResultEntry(Context ctx, ExamResultEntry entry) throws ParameterError {

        ChaincodeStub stub = ctx.getStub();
        ArrayList<InvalidParameter> invalidparams = new ArrayList<>();
        List<String> gradesList = new ArrayList<String>(){{add("1.0");add("1.3");add("1.7"); add("2.0");add("2.3");add("2.7");add("3.0");
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
        if(!(certUtil.keyExists(stub, entry.getEnrollmentId()))){
            invalidparams.add(getNoCertificateForEnrollmentIDParam());
        }

        // todo: nicer way get to get moduleId
        String examId= entry.getExamId();
        String moduleId= examId.substring(examId.indexOf(":")+1,examId.indexOf(":",examId.indexOf(":")+1));

        if (moduleId == null || moduleId.equals("")) {
            invalidparams.add(getEmptyModuleIdParam());
        }
        // Check, if there exists an ExaminationRegulation which contains the moduleId
        HashSet<ExaminationRegulationModule> validModules = exRegUtil.getValidModules(stub);
        if(!checkModuleId(validModules,moduleId)){
            invalidparams.add(getNoExaminationRegulationModuleForModuleIDParam());
        }
        // todo:Check, if examId exists

        return invalidparams;
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

    public void checkParamsAddExamResult(Context ctx, ExamResult examResult) throws ParameterError {
        ChaincodeStub stub = ctx.getStub();
        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        List<ExamResultEntry> examResultEntries=examResult.getExamResultEntries();
        //for each entry
        for (int i=0; i< examResultEntries.size();i++){
            ExamResultEntry entry= examResultEntries.get(i);
            // check if enrollmentId, examID, moduleID are not empty and grade is correct and certificate for enrollment ID exists and examID exists
            invalidParams.addAll(getParameterErrorsForExamResultEntry(ctx, entry));
        }
        // todo: Check All entries refer to the same examId
        // todo: Check All students referenced must be admitted (?)
        // todo: Check All students admitted must be referenced (?)
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }

    }

    public void checkParamsGetExamResultEntries(Context ctx, List<String> params) throws ParameterError {

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        // check if 2 params
        // check params: enrollmentId, examIds
        // examIds: Check, if valid jsonList of String

        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
    }
}
