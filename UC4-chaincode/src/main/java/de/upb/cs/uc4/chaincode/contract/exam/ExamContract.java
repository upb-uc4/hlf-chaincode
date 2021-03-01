package de.upb.cs.uc4.chaincode.contract.exam;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.helper.HyperledgerManager;
import de.upb.cs.uc4.chaincode.model.exam.Exam;
import de.upb.cs.uc4.chaincode.model.exam.ExamType;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.time.Instant;
import java.util.Arrays;
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
        final String[] args = new String[]{examJson};
        try {
            cUtil.checkParamsAddExam(ctx, args);
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        Exam exam = GsonWrapper.fromJson(examJson, Exam.class);
        exam.resetExamId();

        try {
            cUtil.validateApprovals(ctx, contractName, transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        try {
            cUtil.finishOperation(stub, contractName, transactionName, args);
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
    public String getExams(
            final Context ctx,
            final String examIds,
            final String courseIds,
            final String lecturerIds,
            final String moduleIds,
            final String types,
            final String admittableAt,
            final String droppableAt) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        final String[] args = new String[]{examIds, courseIds, lecturerIds, moduleIds, types, admittableAt, droppableAt};
        try {
            cUtil.checkParamsGetExams(ctx, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        try {
            cUtil.validateApprovals(ctx, contractName, transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        try {
            cUtil.finishOperation(ctx.getStub(), contractName, transactionName, args);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        List<Exam> examList = cUtil.getExams(
                ctx.getStub(),
                Arrays.asList(GsonWrapper.fromJson(examIds, String[].class).clone()),
                Arrays.asList(GsonWrapper.fromJson(courseIds, String[].class).clone()),
                Arrays.asList(GsonWrapper.fromJson(lecturerIds, String[].class).clone()),
                Arrays.asList(GsonWrapper.fromJson(moduleIds, String[].class).clone()),
                Arrays.asList(GsonWrapper.fromJson(types, ExamType[].class).clone()),
                GsonWrapper.fromJson(admittableAt, Instant.class),
                GsonWrapper.fromJson(droppableAt, Instant.class));
        return GsonWrapper.toJson(examList);
    }
}
