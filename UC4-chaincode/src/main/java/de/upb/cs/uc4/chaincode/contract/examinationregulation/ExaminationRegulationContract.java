package de.upb.cs.uc4.chaincode.contract.examinationregulation;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ledgeraccess.LedgerStateNotFoundError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.GeneralHelper;
import de.upb.cs.uc4.chaincode.helper.HyperledgerManager;
import de.upb.cs.uc4.chaincode.model.ExaminationRegulation;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Contract(
        name = ExaminationRegulationContract.contractName
)
public class ExaminationRegulationContract extends ContractBase {

    private final ExaminationRegulationContractUtil cUtil = new ExaminationRegulationContractUtil();
    public final static String contractName = "UC4.ExaminationRegulation";
    public final static String transactionNameAddExaminationRegulation = "addExaminationRegulation";
    public final static String transactionNameGetExaminationRegulations = "getExaminationRegulations";
    public final static String transactionNameCloseExaminationRegulation = "closeExaminationRegulation";

    /**
     * Adds an examination regulation to the ledger.
     *
     * @param ctx                   transaction context providing access to ChaincodeStub etc.
     * @param examinationRegulation examination regulation to be added
     * @return examination regulation on success, serialized error on failure
     */
    @Transaction()
    public String addExaminationRegulation(final Context ctx, final String examinationRegulation) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        final String[] args = new String[]{examinationRegulation};
        try {
            cUtil.checkParamsAddExaminationRegulation(ctx, args);
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.validateApprovals(ctx, contractName,  transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        ExaminationRegulation newExaminationRegulation = GsonWrapper.fromJson(examinationRegulation, ExaminationRegulation.class);
        try {
            cUtil.finishOperation(stub, contractName,  transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return cUtil.putAndGetStringState(stub, newExaminationRegulation.getName(), GsonWrapper.toJson(newExaminationRegulation));
    }

    /**
     * Gets examination regulations from the ledger.
     *
     * @param ctx   transaction context providing access to ChaincodeStub etc.
     * @param names names of the examination regulations to be returned
     * @return examination regulations on success, serialized error on failure
     */
    @Transaction()
    public String getExaminationRegulations(final Context ctx, final String names) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        final String[] args = new String[]{names};
        try {
            cUtil.checkParamsGetExaminationRegulations(args);
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.validateApprovals(ctx, contractName,  transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        List<String> nameList;
        try {
            nameList = Arrays.asList(GsonWrapper.fromJson(names, String[].class).clone());
        } catch (Exception e) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableNameListParam()));
        }

        List<ExaminationRegulation> regulations = new ArrayList<>();
        if (nameList.isEmpty()) {
            // read all existing information
            regulations = cUtil.getAllStates(stub, ExaminationRegulation.class);
        } else {
            // read information for names
            for (String name : nameList) {
                if (!GeneralHelper.valueUnset(name)) {
                    ExaminationRegulation regulation;
                    try {
                        regulation = cUtil.getState(stub, name, ExaminationRegulation.class);
                    } catch (LedgerStateNotFoundError e) {
                        continue;
                    } catch (LedgerAccessError e) {
                        return e.getJsonError();
                    }
                    regulations.add(regulation);
                }
            }
        }
        try {
            cUtil.finishOperation(stub, contractName,  transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return GsonWrapper.toJson(regulations);
    }

    /**
     * Closes the specified examination regulation (i.e. sets the active flag to false).
     *
     * @param ctx  transaction context providing access to ChaincodeStub etc.
     * @param name name of the examination regulation to be closed
     * @return examination regulation on success, serialized error on failure
     */
    @Transaction()
    public String closeExaminationRegulation(final Context ctx, final String name) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        final String[] args = new String[]{name};
        try {
            cUtil.checkParamsCloseExaminationRegulation(ctx, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.validateApprovals(ctx, contractName,  transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        ExaminationRegulation regulation;
        try {
            regulation = cUtil.getState(stub, name, ExaminationRegulation.class);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }

        regulation.setActive(false);
        try {
            cUtil.finishOperation(stub, contractName,  transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return cUtil.putAndGetStringState(stub, regulation.getName(), GsonWrapper.toJson(regulation));
    }
}
