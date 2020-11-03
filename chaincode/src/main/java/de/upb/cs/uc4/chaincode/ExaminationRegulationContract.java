package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.error.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.util.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Contract(
        name="UC4.ExaminationRegulation"
)
public class ExaminationRegulationContract extends ContractBase {

    private final ExaminationRegulationContractUtil cUtil = new ExaminationRegulationContractUtil();

    /**
     * Adds an examination regulation to the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param examinationRegulation examination regulation to be added
     * @return examination regulation on success, serialized error on failure
     */
    @Transaction()
    public String addExaminationRegulation(final Context ctx, final String examinationRegulation) {

        ChaincodeStub stub = ctx.getStub();

        ExaminationRegulation newExaminationRegulation;
        try {
            newExaminationRegulation = GsonWrapper.fromJson(examinationRegulation, ExaminationRegulation.class);
        } catch(Exception e) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableExaminationRegulationParam()));
        }

        HashSet<Module> validModules = new HashSet<>();
        List<ExaminationRegulation> regulations;
        try {
            regulations = cUtil.getAllStates(stub);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }
        for (ExaminationRegulation regulation: regulations) {
            validModules.addAll(regulation.getModules());
        }

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        invalidParams.addAll(cUtil.getErrorForExaminationRegulation(newExaminationRegulation, validModules));

        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        String result = cUtil.getStringState(stub, newExaminationRegulation.getName());
        if (result != null && !result.equals("")) {
            return GsonWrapper.toJson(cUtil.getConflictError());
        }

        return cUtil.putAndGetStringState(stub, newExaminationRegulation.getName(), GsonWrapper.toJson(newExaminationRegulation));
    }

    /**
     * Gets examination regulations from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param names names of the examination regulations to be returned
     * @return examination regulations on success, serialized error on failure
     */
    @Transaction()
    public String getExaminationRegulations(final Context ctx, final String... names) {

        ChaincodeStub stub = ctx.getStub();

        ArrayList<ExaminationRegulation> regulations = new ArrayList<>();
        for (String name: names) {
            if (!cUtil.valueUnset(name)) {
                ExaminationRegulation regulation;
                try {
                    regulation = cUtil.getState(stub, name);
                } catch (LedgerAccessError e) {
                    // TODO: only abort on unprocessable state, not on state not found
                    return e.getJsonError();
                }
                regulations.add(regulation);
            }
        }
        return GsonWrapper.toJson(regulations);
    }

    /**
     * Closes the specified examination regulation (i.e. sets the active flag to false).
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param name name of the examination regulation to be closed
     * @return examination regulation on success, serialized error on failure
     */
    @Transaction()
    public String closeExaminationRegulation(final Context ctx, final String name) {

        ChaincodeStub stub = ctx.getStub();

        ExaminationRegulation regulation;
        try {
            regulation = cUtil.getState(stub, name);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }

        regulation.setActive(false);
        return cUtil.putAndGetStringState(stub, regulation.getName(), GsonWrapper.toJson(regulation));
    }
}
