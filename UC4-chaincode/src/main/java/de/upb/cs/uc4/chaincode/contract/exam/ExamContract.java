package de.upb.cs.uc4.chaincode.contract.exam;

import com.google.common.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.AccessManager;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.helper.HyperledgerManager;
import de.upb.cs.uc4.chaincode.model.Admission;
import de.upb.cs.uc4.chaincode.model.Exam;
import de.upb.cs.uc4.chaincode.model.OperationData;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Contract(
        name = ExamContract.contractName
)
public class ExamContract extends ContractBase {
    private final ExamContractUtil cUtil = new ExamContractUtil();

    public final static String contractName = "UC4.Exam";
    public final static String transactionNameAddExam = "addExam";
    public final static String transactionNameGetExams = "getExams";

    /**
     * Adds exam.
     *
     * @param examJson exam to be added
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @return exam on success, including the newly added exam
     */
    @Transaction()
    public String addExam(final Context ctx, String examJson) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        try {
            cUtil.checkParamsAddExam(ctx, Collections.singletonList(examJson));
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        Exam exam = GsonWrapper.fromJson(examJson, Exam.class);

        try {
            cUtil.validateApprovals(ctx, contractName, transactionName, new String[]{examJson});
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        try {
            cUtil.finishOperation(stub, contractName, transactionName, new String[]{examJson});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return cUtil.putAndGetStringState(stub, exam.getExamId(), GsonWrapper.toJson(exam));
    }


    /**
     * Gets ExamList from the ledger.
     *
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return Serialized List of Matching Exams on success, serialized error on failure
     */
    @Transaction()
    public String getExams(final Context ctx, final String examIds, final String courseIds, final String lecturerIds,
                           final String moduleIds, final String types, final String admittableAt, final String droppableAt) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        List<String> examIdList = new ArrayList<>();
        if(!cUtil.valueUnset(examIds)) {
            try {
                examIdList = GsonWrapper.fromJson(examIds, listType);
            } catch (Exception e) {
                return  new ParameterError(GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableParam("examIds")))).getJsonError();
            }
        }

        List<String> courseIdList = new ArrayList<>();
        if(!cUtil.valueUnset(examIds)) {
            try {
                courseIdList = GsonWrapper.fromJson(courseIds, listType);
            } catch (Exception e) {
                return  new ParameterError(GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableParam("courseIds")))).getJsonError();
            }
        }

        List<String> lecturerIdList = new ArrayList<>();
        if(!cUtil.valueUnset(examIds)) {
            try {
                lecturerIdList = GsonWrapper.fromJson(lecturerIds, listType);
            } catch (Exception e) {
                return  new ParameterError(GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableParam("lecturerIds")))).getJsonError();
            }
        }

        List<String> moduleIdList = new ArrayList<>();
        if(!cUtil.valueUnset(examIds)) {
            try {
                moduleIdList = GsonWrapper.fromJson(moduleIds, listType);
            } catch (Exception e) {
                return  new ParameterError(GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableParam("moduleIds")))).getJsonError();
            }
        }

        List<String> typeList = new ArrayList<>();
        if(!cUtil.valueUnset(examIds)) {
            try {
                typeList = GsonWrapper.fromJson(types, listType);
            } catch (Exception e) {
                return  new ParameterError(GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableParam("types")))).getJsonError();
            }
        }

        List<Exam> examList = cUtil.getExams(
                ctx.getStub(),
                examIdList,
                courseIdList,
                lecturerIdList,
                moduleIdList,
                typeList,
                admittableAt,
                droppableAt);
        return GsonWrapper.toJson(examList);
    }
}
