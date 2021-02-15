package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.contract.exam.ExamContract;
import de.upb.cs.uc4.chaincode.contract.exam.ExamContractUtil;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.Exam;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.model.JsonIOTestSetup;
import de.upb.cs.uc4.chaincode.model.errors.DetailedError;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import org.hyperledger.fabric.contract.Context;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class ExamContractTest extends TestCreationBase {

    private final ExamContract contract = new ExamContract();
    private final ExamContractUtil cUtil = new ExamContractUtil();

    String getTestConfigDir() {
        return "src/test/resources/test_configs/exam_contract";
    }

    DynamicTest CreateTest(JsonIOTest test) {
        String testType = test.getType();
        String testName = test.getName();
        JsonIOTestSetup setup = test.getSetup();
        List<String> input = TestUtil.toStringList(test.getInput());
        List<String> compare = TestUtil.toStringList(test.getCompare());
        List<String> ids = test.getIds();

        switch (testType) {
            case "addExam_SUCCESS":
                return DynamicTest.dynamicTest(testName, addExamSuccessTest(setup, input, compare, ids));
            case "addExam_FAILURE":
                return DynamicTest.dynamicTest(testName, addExamFailureTest(setup, input, compare, ids));
            case "getExam_SUCCESS":
                return DynamicTest.dynamicTest(testName, getExamsSuccessTest(setup, input, compare, ids));
            default:
                throw new RuntimeException("Test " + testName + " of type " + testType + " could not be matched.");
        }
    }

    private Executable addExamSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(ExamContract.contractName, ExamContract.transactionNameAddExam, setup, input, ids);

            String addResult = contract.addExam(ctx, input.get(0));

            assertThat(addResult).isEqualTo(compare.get(0));
            Exam compareExam = GsonWrapper.fromJson(compare.get(0), Exam.class);
            Exam ledgerExam = cUtil.getState(ctx.getStub(), compareExam.getExamId(), Exam.class);
            assertThat(ledgerExam).isEqualTo(compareExam);
            assertThat(ledgerExam.toString()).isEqualTo(compareExam.toString());
        };
    }

    private Executable addExamFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(ExamContract.contractName, ExamContract.transactionNameAddExam, setup, input, ids);
            String result = contract.addExam(ctx, input.get(0));
            DetailedError actualError = GsonWrapper.fromJson(result, DetailedError.class);
            DetailedError expectedError = GsonWrapper.fromJson(compare.get(0), DetailedError.class);
            assertThat(actualError).isEqualTo(expectedError);
        };
    }

    private Executable getExamsSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(ExamContract.contractName, ExamContract.transactionNameGetExams, setup, input, ids);

            String getResult = contract.getExams(ctx, input.get(0), input.get(1), input.get(2), input.get(3), input.get(4), input.get(5), input.get(6));
            assertThat(getResult).isEqualTo(compare.get(0));
        };
    }

}
