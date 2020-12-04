package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.exceptions.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.util.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.*;
import java.util.stream.Collectors;

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
     * @param admissionJson json representation of new Admission to add.
     * @return newAdmission on success, serialized error on failure
     */
    @Transaction()
    public String addAdmission(final Context ctx, String admissionJson) {
        ChaincodeStub stub = ctx.getStub();

        Admission newAdmission;
        try {
            newAdmission = GsonWrapper.fromJson(admissionJson, Admission.class);
            newAdmission.resetAdmissionId();
        } catch (Exception e) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableParam("admission")));
        }

        if (cUtil.keyExists(stub, newAdmission.getAdmissionId())) {
            return GsonWrapper.toJson(cUtil.getConflictError());
        }

        ArrayList<InvalidParameter> invalidParams = cUtil.getParameterErrorsForAdmission(newAdmission);
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }


        // check for semantic errors
        ArrayList<InvalidParameter> invalidParameters = cUtil.getSemanticErrorsForAdmission(stub, newAdmission);
        if (!invalidParameters.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getInvalidActionError(invalidParameters));
        }

        List<String> requiredIds = Collections.singletonList(newAdmission.getEnrollmentId());
        List<String> requiredTypes = Collections.singletonList("admin");

        // TODO: approval Check!! I DO NOT KNOW IF I CAN JUST BUILD THE PARAMETER LIST LIKE THAT
        if (!cUtil.validateApprovals(
                ctx,
                requiredIds,
                requiredTypes,
                this.contractName,
                "addAdmission",
                Arrays.stream(new String[]{admissionJson}).collect(Collectors.toList()))) {
            return GsonWrapper.toJson(cUtil.getInsufficientApprovalsError());
        }

        // TODO: can we create a composite key of all inputs to improve reading performance for get...forUser/Module/Course
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

        // check empty
        Admission admission;
        try {
            admission = cUtil.<Admission>getState(stub, admissionId, Admission.class);
        } catch(LedgerAccessError e) {
            return e.getJsonError();
        }

        // check approval
        List<String> requiredIds = Collections.singletonList(admission.getEnrollmentId());
        List<String> requiredTypes = Collections.singletonList("admin");
        if (!cUtil.validateApprovals(
                ctx,
                requiredIds,
                requiredTypes,
                this.contractName,
                "dropAdmission",
                Collections.singletonList(admissionId))) {
            return GsonWrapper.toJson(cUtil.getInsufficientApprovalsError());
        }

        // perform delete
        try {
            cUtil.delState(stub, admissionId);
        } catch(LedgerAccessError e) {
            return e.getJsonError();
        }

        // success
        return "";
    }

    /**
     * Gets AdmissionList from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollment to find admissions for
     * @return Serialized List of Matching Admissions on success, serialized error on failure
     */
    @Transaction()
    public String getAdmissions(final Context ctx, final String enrollmentId, final String courseId, final String moduleId) {
        ChaincodeStub stub = ctx.getStub();

        List<Admission> admissions = cUtil.getAdmissions(stub, enrollmentId, courseId, moduleId);
        return GsonWrapper.toJson(admissions);
    }
}
