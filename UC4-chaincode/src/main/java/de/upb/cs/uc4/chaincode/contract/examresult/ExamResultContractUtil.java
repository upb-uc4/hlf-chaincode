package de.upb.cs.uc4.chaincode.contract.examresult;

import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import org.hyperledger.fabric.contract.Context;

import java.util.List;

public class ExamResultContractUtil extends ContractUtil {

    public ExamResultContractUtil() {
        keyPrefix = "examResult";
        thing = "ExamResult";
        identifier = "";
    }



    public void checkParamsAddExamResult(Context ctx, List<String> params) throws ParameterError {

    }

    public void checkParamsGetExamResultEntries(Context ctx, List<String> params) throws ParameterError {

    }
}
