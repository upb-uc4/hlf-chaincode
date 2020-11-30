package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.exceptions.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.LedgerStateNotFoundError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.GenericError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;
import java.util.List;
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
                .reason("Timestamp must be the following format \"(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\", e.g. \"2020-12-31T23:59:59\"");
    }

    public InvalidParameter getInvalidModuleAvailable(String parameterName) {
        return new InvalidParameter()
                .name(prefix + "." + parameterName)
                .reason("The student is not matriculated in any examinationRegulation containing the module he is trying to enroll in.");
    }

    public List<Admission> getAdmissions(ChaincodeStub stub, String enrollmentId, String courseId, String moduleId) {
        return this.getAllStates(stub, Admission.class).stream()
                .filter(item -> enrollmentId.isEmpty() || item.getEnrollmentId().equals(enrollmentId))
                .filter(item -> courseId.isEmpty() || item.getCourseId().equals(courseId))
                .filter(item -> moduleId.isEmpty() || item.getModuleId().equals(moduleId)).collect(Collectors.toList());
    }

    /**
     * Returns a list of errors describing everything wrong with the given admission parameters
     *
     * @param admission admission to return errors for
     * @return a list of all errors found for the given matriculationData
     */
    public ArrayList<InvalidParameter> getSemanticErrorsForAdmission(
            ChaincodeStub stub,
            Admission admission) {

        ArrayList<InvalidParameter> invalidParameters = new ArrayList<>();

        if (!this.checkModuleAvailable(stub, admission)) {
            invalidParameters.add(getInvalidModuleAvailable("enrollmentId"));
            invalidParameters.add(getInvalidModuleAvailable("moduleId"));
        }

        return invalidParameters;
    }

    private boolean checkModuleAvailable(ChaincodeStub stub, Admission admission) {
        ExaminationRegulationContractUtil erUtil = new ExaminationRegulationContractUtil();
        MatriculationDataContractUtil matUtil = new MatriculationDataContractUtil();

        try{
            MatriculationData matriculationData = matUtil.getState(stub, admission.getEnrollmentId(), MatriculationData.class);
            List<SubjectMatriculation> matriculations = matriculationData.getMatriculationStatus();
            for (SubjectMatriculation matriculation : matriculations) {
                String examinationRegulationIdentifier = matriculation.getFieldOfStudy();
                ExaminationRegulation examinationRegulation = erUtil.getState(stub, examinationRegulationIdentifier, ExaminationRegulation.class);
                List<ExaminationRegulationModule> modules = examinationRegulation.getModules();
                for(ExaminationRegulationModule module : modules){
                    if (module.getId().equals(admission.getModuleId())) {
                        return true;
                    }
                }
            }
        } catch (LedgerAccessError e){
            return false;
        }

        return false;
    }

    /**
     * Returns a list of errors describing everything wrong with the given admission parameters
     *
     * @param admission admission to return errors for
     * @return a list of all errors found for the given matriculationData
     */
    public ArrayList<InvalidParameter> getParameterErrorsForAdmission(
            Admission admission) {

        ArrayList<InvalidParameter> invalidparams = new ArrayList<>();

        if (valueUnset(admission.getEnrollmentId())) {
            invalidparams.add(getEmptyEnrollmentIdParam(prefix + "."));
        }
        if (valueUnset(admission.getCourseId())) {
            invalidparams.add(getEmptyParameterError(prefix + ".courseId"));
        }
        if (valueUnset(admission.getModuleId())) {
            invalidparams.add(getEmptyParameterError(prefix + ".moduleId"));
        }
        if (valueUnset(admission.getTimestamp())) {
            invalidparams.add(getEmptyParameterError(prefix + ".timestamp"));
        }

        if (!checkTimestampFormatValid(admission.getTimestamp())) {
            invalidparams.add(getInvalidTimestampParam());
        }

        return invalidparams;
    }

    /**
     * Checks the given semester string for validity.
     *
     * @param timestamp timestamp string to check for validity
     * @return true if input is a valid description of a timestamp, false otherwise
     */
    public boolean checkTimestampFormatValid(String timestamp) {
        Pattern pattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})");
        Matcher matcher = pattern.matcher(timestamp);
        return matcher.matches();
    }
}
