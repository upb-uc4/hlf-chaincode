package de.upb.cs.uc4.chaincode.contract.examresult;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.HyperledgerManager;
import org.hyperledger.fabric.contract.annotation.Contract;
import de.upb.cs.uc4.chaincode.model.ExamResult;
import de.upb.cs.uc4.chaincode.model.ExamResultEntry;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Transaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Contract(
        name = ExamResultContract.contractName
)

public class ExamResultContract extends ContractBase {
    private final ExamResultContractUtil cUtil = new ExamResultContractUtil();

    public final static String contractName = "UC4.ExamResult";
    public final static String transactionNameAddExamResult = "addExamResult";
    public final static String transactionNameGetExamResultEntries= "getExamResultEntries";

    /**
     * Adds an examResult.
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param examResult    examResult to be added
     * @return examResult object on ledger on success, serialized error on failure
     */
    @Transaction()
    public String addExamResult(final Context ctx, ExamResult examResult) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        try {
            cUtil.checkParamsAddExamResult(ctx, examResult);
        } catch (ParameterError e) {
            return e.getJsonError();
        }
        // check required approvals
        // add to ledger
        return "";
    }

    /**
     * Gets ExamResultEntries from the ledger.
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId
     * @param examIds      examIds
     * @return the full list of existing ExamResultEntries matching the filter parameters or an empty list of none could be found
     */

    @Transaction()
    public String getExamResultEntries(final Context ctx, String enrollmentId, List<String> examIds) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        try {
            cUtil.checkParamsGetExamResultEntries(ctx, new ArrayList<String>(){{add(enrollmentId); addAll(examIds);}});
        } catch (ParameterError e) {
            return e.getJsonError();
        }
        // logical AND filter
        // filter for enrollment ID (if not empty)
        // filter for examIds (if not empty)
        // return results
        return "";
    }

}
