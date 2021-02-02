package de.upb.cs.uc4.chaincode.contract.examresult;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;

@Contract(
        name = ExamResultContract.contractName
)

public class ExamResultContract extends ContractBase {
    private final ExamResultContractUtil cUtil = new ExamResultContractUtil();

    public final static String contractName = "UC4.ExamResult";
    public final static String transactionNameAddExamResult = "addExamResult";
    public final static String transactionNameGetExamResultEntries= "getExamResultEntries";

}
