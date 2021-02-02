package de.upb.cs.uc4.chaincode.contract.admission;

import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContractUtil;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

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

    public InvalidParameter getInvalidModuleAvailable(String parameterName) {
        return new InvalidParameter()
                .name(errorPrefix + "." + parameterName)
                .reason("The student is not matriculated in any examinationRegulation containing the module he is trying to enroll in");
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

        if (!this.checkModuleAvailable(stub, admission.getEnrollmentId(), admission.getModuleId())) {
            invalidParameters.add(getInvalidModuleAvailable("enrollmentId"));
            invalidParameters.add(getInvalidModuleAvailable("moduleId"));
        }

        return invalidParameters;
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
            invalidparams.add(getEmptyEnrollmentIdParam(errorPrefix + "."));
        }
        if (valueUnset(admission.getCourseId())) {
            invalidparams.add(getEmptyInvalidParameter(errorPrefix + ".courseId"));
        }
        if (valueUnset(admission.getModuleId())) {
            invalidparams.add(getEmptyInvalidParameter(errorPrefix + ".moduleId"));
        }
        if (valueUnset(admission.getTimestamp())) {
            invalidparams.add(getInvalidTimestampParam());
        }

        return invalidparams;
    }

    public void checkParamsAddAdmission(Context ctx, List<String> params) throws ParameterError {
        if (params.size() != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String admissionJson = params.get(0);

        ChaincodeStub stub = ctx.getStub();

        Admission newAdmission;
        try {
            newAdmission = GsonWrapper.fromJson(admissionJson, Admission.class);
            newAdmission.resetAdmissionId();
        } catch (Exception e) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(getUnparsableParam("admission"))));
        }

        if (keyExists(stub, newAdmission.getAdmissionId())) {
            throw new ParameterError(GsonWrapper.toJson(getConflictError()));
        }

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        invalidParams.addAll(getParameterErrorsForAdmission(newAdmission));
        invalidParams.addAll(getSemanticErrorsForAdmission(stub, newAdmission));
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
    }

    public void checkParamsDropAdmission(Context ctx, List<String> params) throws LedgerAccessError, ParameterError {
        if (params.size() != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String admissionId = params.get(0);

        ChaincodeStub stub = ctx.getStub();
        getState(stub, admissionId, Admission.class);
    }
}
