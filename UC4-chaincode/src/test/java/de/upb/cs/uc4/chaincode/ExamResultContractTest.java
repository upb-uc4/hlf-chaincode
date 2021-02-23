package de.upb.cs.uc4.chaincode;


import de.upb.cs.uc4.chaincode.contract.examresult.ExamResultContract;
import de.upb.cs.uc4.chaincode.contract.examresult.ExamResultContractUtil;
import de.upb.cs.uc4.chaincode.contract.operation.OperationContract;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.model.JsonIOTestSetup;
import de.upb.cs.uc4.chaincode.model.examresult.ExamResult;
import de.upb.cs.uc4.chaincode.model.examresult.ExamResultEntry;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import org.hyperledger.fabric.contract.Context;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class ExamResultContractTest extends TestCreationBase {

    private final ExamResultContract contract = new ExamResultContract();
    private final ExamResultContractUtil cUtil = new ExamResultContractUtil();

    @Test
    void simpleDateTest(){
        String addExamParams = "[\"{\\\"examId\\\":\\\"\\\",\\\"courseId\\\":\\\"1b88275a-6d49-11eb-a92c-57114907e620\\\",\\\"moduleId\\\":\\\"Some Module ID\\\",\\\"lecturerEnrollmentId\\\":\\\"5634c3abc66e5e674ae67f551af04317a7e29498918701f121e3aaa500a270e0\\\",\\\"type\\\":\\\"Written Exam\\\",\\\"date\\\":\\\"2021-02-12T15:47:18.983Z\\\",\\\"ects\\\":5,\\\"admittableUntil\\\":\\\"2021-02-12T15:45:18.983Z\\\",\\\"droppableUntil\\\":\\\"2021-02-12T15:46:18.983Z\\\"}\"]";
        Context ctx = TestUtil.mockContext(TestUtil.mockStub(new JsonIOTestSetup(), "initiateOperation"));
        OperationContract contract = new OperationContract();
        String result = contract.initiateOperation(ctx, "test-id", "UC4.Exam", "addExam", addExamParams);
        System.out.println(result);
    }

    String getTestConfigDir() {
        return "src/test/resources/test_configs/exam_result_contract";
    }

    DynamicTest CreateTest(JsonIOTest test) {
        String testType = test.getType();
        String testName = test.getName();
        System.out.println(testName);
        JsonIOTestSetup setup = test.getSetup();
        List<String> input = TestUtil.toStringList(test.getInput());
        List<String> compare = TestUtil.toStringList(test.getCompare());
        List<String> ids = test.getIds();

        switch (testType) {
            case "getExamResultEntries":
                return DynamicTest.dynamicTest(testName, getExamResultEntriesTest(setup, input, compare, ids));
            case "addExamResult_SUCCESS":
                return DynamicTest.dynamicTest(testName, addExamResultSuccessTest(setup, input, compare, ids));
            case "addExamResult_FAILURE":
                return DynamicTest.dynamicTest(testName, addExamResultFailureTest(setup, input, compare, ids));
            default:
                throw new RuntimeException("Test " + testName + " of type " + testType + " could not be matched.");
        }
    }

    private Executable getExamResultEntriesTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(ExamResultContract.contractName, ExamResultContract.transactionNameGetExamResultEntries, setup, input, ids);

            List<ExamResultEntry> compareExamResult = Arrays.asList(GsonWrapper.fromJson(compare.get(0), ExamResultEntry[].class).clone());
            List<ExamResultEntry> ledgerExamResult = Arrays.asList(GsonWrapper.fromJson(
                    contract.getExamResultEntries(ctx, input.get(0), input.get(1)), ExamResultEntry[].class).clone());
            assertThat(ledgerExamResult).isEqualTo(compareExamResult);
        };
    }

    private Executable addExamResultSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(ExamResultContract.contractName, ExamResultContract.transactionNameAddExamResult, setup, input, ids);

            String result = contract.addExamResult(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));

            ExamResult compareExamResult = GsonWrapper.fromJson(compare.get(0), ExamResult.class);
            ExamResult ledgerExamResult =
                    cUtil.getState(ctx.getStub(), cUtil.getKey(compareExamResult), ExamResult.class);
            assertThat(ledgerExamResult).isEqualTo(compareExamResult);
            assertThat(ledgerExamResult.toString()).isEqualTo(compareExamResult.toString());
        };
    }

    private Executable addExamResultFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(ExamResultContract.contractName, ExamResultContract.transactionNameAddExamResult, setup, input, ids);

            String result = contract.addExamResult(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }
}