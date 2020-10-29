package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.error.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.GenericError;
import de.upb.cs.uc4.chaincode.model.InvalidParameter;
import de.upb.cs.uc4.chaincode.model.MatriculationData;
import de.upb.cs.uc4.chaincode.model.SubjectMatriculation;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatriculationDataContractUtil extends ContractUtil {
    private final String thing = "MatriculationData";

    public MatriculationDataContractUtil() {
        keyPrefix = "matriculationData";
    }

    @Override
    public GenericError getConflictError() {
        return super.getConflictError(thing);
    }

    @Override
    public GenericError getNotFoundError() {
        return super.getNotFoundError(thing);
    }

    public InvalidParameter getUnparsableMatriculationDataParam() {
        return new InvalidParameter()
                .name("matriculationData")
                .reason("The given parameter cannot be parsed from json");
    }

    public InvalidParameter getUnparsableMatriculationParam() {
        return new InvalidParameter()
                .name("matriculations")
                .reason("The given parameter cannot be parsed from json");
    }

    public InvalidParameter getEmptyMatriculationStatusParam(String prefix) {
        return new InvalidParameter()
                .name(prefix)
                .reason("Matriculation status must not be empty");
    }

    public InvalidParameter getInvalidFieldOfStudyParam(String prefix) {
        return new InvalidParameter()
                .name(prefix+"fieldOfStudy")
                .reason("Field of study must be one of the specified values");
    }

    public InvalidParameter getDuplicateFieldOfStudyParam(String prefix, int index) {
        return new InvalidParameter()
                .name(prefix+"[" + index + "].fieldOfStudy")
                .reason("Each field of study must only appear in one matriculationStatus");
    }

    public InvalidParameter getEmptySemestersParam(String prefix) {
        return new InvalidParameter()
                .name(prefix + "semesters")
                .reason("Semesters must not be empty");
    }

    public InvalidParameter getDuplicateSemesterParam(String prefix, int index) {
        return new InvalidParameter()
                .name(prefix + "[" + index + "]")
                .reason("Each semester must only appear once in matriculationStatus.semesters");
    }

    public InvalidParameter getInvalidSemesterParam(String prefix, int index) {
        return new InvalidParameter()
                .name(prefix + "[" + index + "]")
                .reason("Semester must be the following format \"(WS\\d{4}/\\d{2}|SS\\d{4})\", e.g. \"WS2020/21\"");
    }

    public MatriculationData getState(ChaincodeStub stub, String key) throws LedgerAccessError {
        String jsonMatriculationData;
        jsonMatriculationData = getStringState(stub, key);
        if (valueUnset(jsonMatriculationData)) {
            throw new LedgerAccessError(GsonWrapper.toJson(getNotFoundError()));
        }
        MatriculationData matriculationData;
        try {
            matriculationData = GsonWrapper.fromJson(jsonMatriculationData, MatriculationData.class);
        } catch(Exception e) {
            throw new LedgerAccessError(GsonWrapper.toJson(getUnprocessableLedgerStateError()));
        }
        return matriculationData;
    }

    /**
     * Returns a list of errors describing everything wrong with the given matriculationData
     * @param matriculationData matriculationData to return errors for
     * @return a list of all errors found for the given matriculationData
     */
    public ArrayList<InvalidParameter> getErrorForMatriculationData(
            MatriculationData matriculationData,
            String prefix) {

        if (!prefix.isEmpty())
            prefix += ".";

        ArrayList<InvalidParameter> invalidparams = new ArrayList<>();

        if(valueUnset(matriculationData.getEnrollmentId())) {
            invalidparams.add(getEmptyEnrollmentIdParam(prefix));
        }

        invalidparams.addAll(getErrorForSubjectMatriculationList(
                matriculationData.getMatriculationStatus(),
                prefix+"matriculationStatus"));
        return invalidparams;
    }

    public ArrayList<InvalidParameter> getErrorForSubjectMatriculationList(
            List<SubjectMatriculation> matriculationStatus,
            String prefix) {

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();

        if (valueUnset(matriculationStatus)) {
            invalidParams.add(getEmptyMatriculationStatusParam(prefix));
        } else {
            ArrayList<SubjectMatriculation.FieldOfStudyEnum> existingFields = new ArrayList<>();

            for (int subMatIndex=0; subMatIndex<matriculationStatus.size(); subMatIndex++) {

                SubjectMatriculation subMat = matriculationStatus.get(subMatIndex);

                if (subMat.getFieldOfStudy() == null) {
                    invalidParams.add(getInvalidFieldOfStudyParam(prefix + "[" + subMatIndex + "]."));
                } else {
                    if (existingFields.contains(subMat.getFieldOfStudy())) {
                        invalidParams.add(getDuplicateFieldOfStudyParam(prefix, subMatIndex));
                    } else
                        existingFields.add(subMat.getFieldOfStudy());
                }

                List<String> semesters = subMat.getSemesters();
                if (valueUnset(semesters)) {
                    invalidParams.add(getEmptySemestersParam(prefix + "[" + subMatIndex + "]."));
                }

                ArrayList<String> existingSemesters = new ArrayList<>();
                for (int semesterIndex = 0; semesterIndex< Objects.requireNonNull(semesters).size(); semesterIndex++) {

                    String semester = semesters.get(semesterIndex);

                    if (!semesterFormatValid(semester)) {
                        invalidParams.add(getInvalidSemesterParam(prefix+"["+subMatIndex+"].semesters", semesterIndex));
                    } else {
                        if (semesterFormatValid(semester)) {
                            if (existingSemesters.contains(semester)) {
                                invalidParams.add(getDuplicateSemesterParam(prefix + "[" + subMatIndex + "].semesters", semesterIndex));
                            } else
                                existingSemesters.add(semester);
                        }
                    }
                }
            }
        }
        return invalidParams;
    }

    /**
     * Checks the given semester string for validity.
     * @param semester semester string to check for validity
     * @return true if semester is a valid description of a semester, false otherwise
     */
    public boolean semesterFormatValid(String semester) {
        Pattern pattern = Pattern.compile("^(WS\\d{4}/\\d{2}|SS\\d{4})");
        Matcher matcher = pattern.matcher(semester);
        if (!matcher.matches())
            return false;
        if ("WS".equals(semester.substring(0,2))) {
            int year1 = Integer.parseInt(semester.substring(4,6));
            int year2 = Integer.parseInt(semester.substring(7,9));
            return year2 == (year1 + 1) % 100;
        }
        return true;
    }
}
