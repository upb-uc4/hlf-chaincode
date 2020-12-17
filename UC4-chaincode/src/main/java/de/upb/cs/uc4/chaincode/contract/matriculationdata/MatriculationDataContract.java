package de.upb.cs.uc4.chaincode.contract.matriculationdata;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.ParameterError;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.model.MatriculationData;
import de.upb.cs.uc4.chaincode.model.SubjectMatriculation;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

@Contract(
        name = "UC4.MatriculationData"
)
public class MatriculationDataContract extends ContractBase {
    private final MatriculationDataContractUtil cUtil = new MatriculationDataContractUtil();

    public final String contractName = "UC4.MatriculationData";

    /**
     * Adds MatriculationData to the ledger.
     *
     * @param ctx               transaction context providing access to ChaincodeStub etc.
     * @param matriculationData MatriculationData to be added
     * @return newMatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String addMatriculationData(final Context ctx, String matriculationData) {
        try {
            cUtil.checkParamsAddMatriculationData(ctx, matriculationData);
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.validateApprovals(stub, this.contractName,  "addMatriculationData", Collections.singletonList(matriculationData));
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        MatriculationData newMatriculationData = GsonWrapper.fromJson(matriculationData, MatriculationData.class);
        return cUtil.putAndGetStringState(stub, newMatriculationData.getEnrollmentId(), GsonWrapper.toJson(newMatriculationData));
    }

    /**
     * Updates MatriculationData on the ledger.
     *
     * @param ctx               transaction context providing access to ChaincodeStub etc.
     * @param matriculationData json-representation of the new MatriculationData to replace the old with
     * @return updatedMatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String updateMatriculationData(final Context ctx, String matriculationData) {
        try {
            cUtil.checkParamsUpdateMatriculationData(ctx, matriculationData);
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        MatriculationData newMatriculationData = GsonWrapper.fromJson(matriculationData, MatriculationData.class);
        return cUtil.putAndGetStringState(stub, newMatriculationData.getEnrollmentId(), GsonWrapper.toJson(newMatriculationData));
    }

    /**
     * Gets MatriculationData from the ledger.
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId of the MatriculationData to be returned
     * @return Serialized MatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String getMatriculationData(final Context ctx, final String enrollmentId) {
        try {
            cUtil.checkParamsGetMatriculationData(ctx, enrollmentId);
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        MatriculationData matriculationData = null;
        try {
            matriculationData = cUtil.getState(stub, enrollmentId, MatriculationData.class);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }
        return GsonWrapper.toJson(matriculationData);
    }

    /**
     * Adds a semester entry to a fieldOfStudy of MatriculationData on the ledger.
     *
     * @param ctx            transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId   enrollmentId to add the matriculations to
     * @param matriculations list of matriculations
     * @return Updated MatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String addEntriesToMatriculationData(
            final Context ctx,
            final String enrollmentId,
            final String matriculations) {
        try {
            cUtil.checkParamsAddEntriesToMatriculationData(ctx, enrollmentId, matriculations);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        Type listType = new TypeToken<ArrayList<SubjectMatriculation>>() {}.getType();
        ArrayList<SubjectMatriculation> matriculationStatus;
        matriculationStatus = GsonWrapper.fromJson(matriculations, listType);

        MatriculationData matriculationData;
        try {
            matriculationData = cUtil.getState(stub, enrollmentId, MatriculationData.class);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }

        matriculationData.addAbsent(matriculationStatus);
        return cUtil.putAndGetStringState(stub, matriculationData.getEnrollmentId(), GsonWrapper.toJson(matriculationData));
    }
}
