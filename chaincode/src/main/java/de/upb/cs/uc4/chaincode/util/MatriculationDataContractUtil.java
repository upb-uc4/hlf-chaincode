package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.model.GenericError;
import de.upb.cs.uc4.chaincode.model.InvalidParameter;

public class MatriculationDataContractUtil extends ContractUtil {

    private final String thing = "MatriculationData";

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
}
