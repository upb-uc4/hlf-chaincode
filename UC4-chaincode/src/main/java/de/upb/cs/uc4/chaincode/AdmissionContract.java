package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.error.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.util.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;
import java.util.List;

@Contract(
        name="UC4.Admission"
)
@Default
public class AdmissionContract extends ContractBase {
    private final AdmissionContractUtil cUtil = new AdmissionContractUtil();

    protected String contractName = "UC4.Admission";

    /**
     * Adds MatriculationData to the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId student to submit admission for
     * @param courseId course to submit admission for
     * @param moduleId module to submit admission for
     * @param timestamp timestamp to submit admission for
     * @return newMatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String addAdmission(final Context ctx, String enrollmentId, String courseId, String moduleId, String timestamp) {

        ChaincodeStub stub = ctx.getStub();

        Admission newAdmission = new Admission(enrollmentId, courseId, moduleId, timestamp);

        ArrayList<InvalidParameter> invalidParams = cUtil.getParameterErrorsForAdmission(newAdmission);

        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        if (cUtil.keyExists(stub, newAdmission.getAdmissionId())) {
            return GsonWrapper.toJson(cUtil.getConflictError());
        }

        ArrayList<ValidationRule> validatedRules = cUtil.getSemanticErrorsForAdmission(stub, newAdmission);
        if (!validatedRules.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getInvalidActionError(validatedRules));
        }

        /* TODO: add approval Check
        List<String> requiredIds = Collections.singletonList(newAdmission.getEnrollmentId());
        List<String> requiredTypes = Collections.singletonList("admin");

        if (!cUtil.validateApprovals(
                ctx,
                requiredIds,
                requiredTypes,
                this.contractName,
                "addAdmission",
                Collections.singletonList(enrollmentId, courseId, moduleId, timestamp))) {
            return GsonWrapper.toJson(cUtil.getInsufficientApprovalsError());
        }
        */

        // TODO: add approval Check
        return cUtil.putAndGetStringState(stub, newAdmission.getAdmissionId(), GsonWrapper.toJson(newAdmission));
    }

    /**
     * Drops an existing admission from the ledger
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param admissionId identifier of admission to drop
     * @return empty string on success, serialized error on failure
     */
    @Transaction()
    public String dropAdmission(final Context ctx, String admissionId) {
        ChaincodeStub stub = ctx.getStub();

        // TODO: approval check

        try {
            cUtil.delState(stub, admissionId);
        } catch(LedgerAccessError e) {
            return e.getJsonError();
        }
        return "";
    }

    /**
     * Gets AdmissionList from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollment to find admissions for
     * @return Serialized List of Matching Admissions on success, serialized error on failure
     */
    @Transaction()
    public String getAdmissionForUser(final Context ctx, final String enrollmentId) {
        ChaincodeStub stub = ctx.getStub();

        // TODO: check empty parameter

        List<Admission> admissions = cUtil.getAdmissionsForUser(stub, enrollmentId);

        // TODO: can I just pass lists to GsonWrapper??
        return GsonWrapper.toJson(admissions);
    }

    /**
     * Gets AdmissionList from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param courseId course to find admissions for
     * @return Serialized List of Matching Admissions on success, serialized error on failure
     */
    @Transaction()
    public String getAdmissionForCourse(final Context ctx, final String courseId) {
        ChaincodeStub stub = ctx.getStub();

        // TODO: check empty parameter

        List<Admission> admissions = cUtil.getAdmissionsForCourse(stub, courseId);

        // TODO: can I just pass lists to GsonWrapper??
        return GsonWrapper.toJson(admissions);
    }

    /**
     * Gets AdmissionList from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param moduleId module to find admissions for
     * @return Serialized List of Matching Admissions on success, serialized error on failure
     */
    @Transaction()
    public String getAdmissionForModule(final Context ctx, final String moduleId) {
        ChaincodeStub stub = ctx.getStub();

        // TODO: check empty parameter

        List<Admission> admissions = cUtil.getAdmissionsForModule(stub, moduleId);

        // TODO: can I just pass lists to GsonWrapper??
        return GsonWrapper.toJson(admissions);
    }
}
