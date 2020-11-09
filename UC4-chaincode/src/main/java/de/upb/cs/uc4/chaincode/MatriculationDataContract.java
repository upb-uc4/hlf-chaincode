package de.upb.cs.uc4.chaincode;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.error.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import de.upb.cs.uc4.chaincode.util.MatriculationDataContractUtil;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.Type;
import java.util.ArrayList;

@Contract(
        name="UC4.MatriculationData"
)
@Default
public class MatriculationDataContract extends ContractBase {

    private final MatriculationDataContractUtil cUtil = new MatriculationDataContractUtil();

    /**
     * Adds MatriculationData to the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param matriculationData MatriculationData to be added
     * @return newMatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String addMatriculationData(final Context ctx, String matriculationData) {

        ChaincodeStub stub = ctx.getStub();

        MatriculationData newMatriculationData;
        try {
            newMatriculationData = GsonWrapper.fromJson(matriculationData, MatriculationData.class);
        } catch(Exception e) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableMatriculationDataParam()));
        }

        ArrayList<InvalidParameter> invalidParams = cUtil.getErrorForMatriculationData(
                newMatriculationData, "matriculationData");
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        if (cUtil.keyExists(stub, newMatriculationData.getEnrollmentId())) {
            return GsonWrapper.toJson(cUtil.getConflictError());
        }

        return cUtil.putAndGetStringState(stub, newMatriculationData.getEnrollmentId(), GsonWrapper.toJson(newMatriculationData));
    }

    /**
     * Updates MatriculationData on the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param matriculationData json-representation of the new MatriculationData to replace the old with
     * @return updatedMatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String updateMatriculationData(final Context ctx, String matriculationData) {

        ChaincodeStub stub = ctx.getStub();

        MatriculationData newMatriculationData;
        try {
            newMatriculationData = GsonWrapper.fromJson(matriculationData, MatriculationData.class);
        } catch(Exception e) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableMatriculationDataParam()));
        }

        ArrayList<InvalidParameter> invalidParams = cUtil.getErrorForMatriculationData(
                newMatriculationData, "matriculationData");
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        if (!cUtil.keyExists(stub, newMatriculationData.getEnrollmentId())) {
            return GsonWrapper.toJson(cUtil.getNotFoundError());
        }

        return cUtil.putAndGetStringState(stub, newMatriculationData.getEnrollmentId(), GsonWrapper.toJson(newMatriculationData));
    }

    /**
     * Gets MatriculationData from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId of the MatriculationData to be returned
     * @return Serialized MatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String getMatriculationData(final Context ctx, final String enrollmentId) {

        ChaincodeStub stub = ctx.getStub();
        MatriculationData matriculationData;
        try {
            matriculationData = cUtil.getState(stub, enrollmentId);
        } catch(LedgerAccessError e) {
            return e.getJsonError();
        }
        return GsonWrapper.toJson(matriculationData);
    }

    /**
     * Adds a semester entry to a fieldOfStudy of MatriculationData on the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId to add the matriculations to
     * @param matriculations list of matriculations
     * @return Updated MatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String addEntriesToMatriculationData (
            final Context ctx,
            final String enrollmentId,
            final String matriculations) {

        ChaincodeStub stub = ctx.getStub();

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        if (cUtil.valueUnset(enrollmentId))
            invalidParams.add(cUtil.getEmptyEnrollmentIdParam());
        Type listType = new TypeToken<ArrayList<SubjectMatriculation>>(){}.getType();
        ArrayList<SubjectMatriculation> matriculationStatus;
        try {
            matriculationStatus = GsonWrapper.fromJson(matriculations, listType);
        } catch(Exception e) {
            invalidParams.add(cUtil.getUnparsableMatriculationParam());
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }
        invalidParams.addAll(cUtil.getErrorForSubjectMatriculationList(matriculationStatus, "matriculations"));
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        MatriculationData matriculationData;
        try {
            matriculationData = cUtil.getState(stub, enrollmentId);
        } catch(LedgerAccessError e) {
            return e.getJsonError();
        }

        matriculationData.addAbsent(matriculationStatus);
        return cUtil.putAndGetStringState(stub, matriculationData.getEnrollmentId(), GsonWrapper.toJson(matriculationData));
    }
}