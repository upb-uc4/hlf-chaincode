package de.upb.cs.uc4.chaincode.util;

import com.google.gson.JsonSyntaxException;
import de.upb.cs.uc4.chaincode.exceptions.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.LedgerStateNotFoundError;
import de.upb.cs.uc4.chaincode.exceptions.UnprocessableLedgerStateError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.GenericError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.model.errors.ValidationRuleViolation;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AdmissionContractUtil extends ContractUtil {
    private final String thing = "Admission";
    private final String prefix = thing.toLowerCase();
    private final String identifier = "admissionId";

    public AdmissionContractUtil() {
        keyPrefix = "admission";
    }

    @Override
    public GenericError getConflictError() {
        return super.getConflictError(thing, identifier);
    }

    @Override
    public GenericError getNotFoundError() {
        return super.getNotFoundError(thing, identifier);
    }

    public InvalidParameter getInvalidTimestampParam() {
        return new InvalidParameter()
                .name(prefix + ".timestamp")
                .reason("Timestamp must be the following format \"(\\d{4}-\\d{2}-\\d{2}_\\d{2}:\\d{2}\", e.g. \"2020-12-31_23:59\"");
    }

    public ValidationRuleViolation ruleViolationModuleAvailable() {
        return new ValidationRuleViolation()
                .name(prefix + ".ModuleAccess")
                .reason("The student is not matriculated in an examinationRegulation, that contains the module he is trying to enroll in.");
    }


    public Admission getState(ChaincodeStub stub, String key) throws LedgerAccessError {
        String jsonAdmission;
        jsonAdmission = getStringState(stub, key);
        if (valueUnset(jsonAdmission)) {
            throw new LedgerStateNotFoundError(GsonWrapper.toJson(getNotFoundError()));
        }
        Admission admission;
        try {
            admission = GsonWrapper.fromJson(jsonAdmission, Admission.class);
        } catch(Exception e) {
            throw new UnprocessableLedgerStateError(GsonWrapper.toJson(getUnprocessableLedgerStateError()));
        }
        return admission;
    }

    public void delState(ChaincodeStub stub, String key) throws LedgerAccessError {
        String jsonAdmission;
        jsonAdmission = getStringState(stub, key);
        if (valueUnset(jsonAdmission)) {
            throw new LedgerStateNotFoundError(GsonWrapper.toJson(getNotFoundError()));
        }
        stub.delState(key);
    }

    public List<Admission> getAdmissionsForUser(ChaincodeStub stub, String enrollmentId){
        return getAllStates(stub).stream()
                .filter(item -> item.getEnrollmentId().equals(enrollmentId)).collect(Collectors.toList());
    }
    public List<Admission> getAdmissionsForCourse(ChaincodeStub stub, String courseId){
        return getAllStates(stub).stream()
                .filter(item -> item.getCourseId().equals(courseId)).collect(Collectors.toList());
    }
    public List<Admission> getAdmissionsForModule(ChaincodeStub stub, String moduleId){
        return getAllStates(stub).stream()
                .filter(item -> item.getModuleId().equals(moduleId)).collect(Collectors.toList());
    }
    private ArrayList<Admission> getAllStates(ChaincodeStub stub) {
        QueryResultsIterator<KeyValue> qrIterator;
        qrIterator = getAllRawStates(stub);
        ArrayList<Admission> admissions = new ArrayList<>();
        for (KeyValue item: qrIterator) {
            Admission admission;
            try {
                admission = GsonWrapper.fromJson(item.getStringValue(), Admission.class);
                admissions.add(admission);
            } catch(JsonSyntaxException e) {
                // ignore
            }
        }
        return admissions;
    }

    /**
     * Returns a list of errors describing everything wrong with the given admission parameters
     * @param admission admission to return errors for
     * @return a list of all errors found for the given matriculationData
     */
    public ArrayList<ValidationRuleViolation> getSemanticErrorsForAdmission(
            ChaincodeStub stub,
            Admission admission) {

        ArrayList<ValidationRuleViolation> validationRuleViolations = new ArrayList<>();

        if(checkModuleAvailable(stub, admission)) {
            validationRuleViolations.add(ruleViolationModuleAvailable());
        }

        return validationRuleViolations;
    }

    private boolean checkModuleAvailable(ChaincodeStub stub, Admission admission) {
        ExaminationRegulationContractUtil erUtil = new ExaminationRegulationContractUtil();
        MatriculationDataContractUtil matUtil = new MatriculationDataContractUtil();

        AtomicBoolean foundMatch = new AtomicBoolean(false);

        MatriculationData matriculationData;
        try{
            matriculationData = matUtil.getState(stub, admission.getEnrollmentId());
            List<SubjectMatriculation> matriculations = matriculationData.getMatriculationStatus();
            matriculations.forEach(matriculation -> {
                String examinationRegulationIdentifier = matriculation.getFieldOfStudy();
                ExaminationRegulation examinationRegulation;
                try {
                    examinationRegulation = erUtil.getState(stub, examinationRegulationIdentifier);
                    List<ExaminationRegulationModule> modules = examinationRegulation.getModules();
                    modules.forEach(module -> {
                        if(module.getId().equals(admission.getModuleId())){
                            foundMatch.set(true);
                        }
                    });
                } catch (LedgerAccessError e){
                    //TODO: something went horribly wrong:
                    // the examinationRegulation referenced in a subjectMatriculation of the student could not be read.
                }
            });
        } catch (LedgerAccessError e){
            //TODO: something went horribly wrong:
            // no matriculation for the given enrollmentId could be read.
        }

        return foundMatch.get();
    }

    /**
     * Returns a list of errors describing everything wrong with the given admission parameters
     * @param admission admission to return errors for
     * @return a list of all errors found for the given matriculationData
     */
    public ArrayList<InvalidParameter> getParameterErrorsForAdmission(
            Admission admission) {

        ArrayList<InvalidParameter> invalidparams = new ArrayList<>();

        if(valueUnset(admission.getEnrollmentId())) {
            invalidparams.add(getEmptyEnrollmentIdParam(prefix+"."));
        }
        if(valueUnset(admission.getCourseId())) {
            invalidparams.add(getEmptyParameterError(prefix+".courseId"));
        }
        if(valueUnset(admission.getModuleId())) {
            invalidparams.add(getEmptyParameterError(prefix+".moduleId"));
        }
        if(valueUnset(admission.getTimestamp())) {
            invalidparams.add(getEmptyParameterError(prefix+".timestamp"));
        }

        if(!checkTimestampFormatValid(admission.getTimestamp())){
            invalidparams.add(getInvalidTimestampParam());
        }

        return invalidparams;
    }

    /**
     * Checks the given semester string for validity.
     * @param timestamp timestamp string to check for validity
     * @return true if input is a valid description of a timestamp, false otherwise
     */
    public boolean checkTimestampFormatValid(String timestamp) {
        Pattern pattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}_\\d{2}:\\d{2})");
        Matcher matcher = pattern.matcher(timestamp);
        return matcher.matches();
    }
}
