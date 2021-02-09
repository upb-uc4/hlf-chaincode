package de.upb.cs.uc4.chaincode;


import com.google.common.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.examresult.ExamResultContract;
import de.upb.cs.uc4.chaincode.contract.examresult.ExamResultContractUtil;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContract;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.model.JsonIOTestSetup;
import de.upb.cs.uc4.chaincode.model.MatriculationData;
import de.upb.cs.uc4.chaincode.model.examresult.ExamResult;
import de.upb.cs.uc4.chaincode.model.examresult.ExamResultEntry;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import org.hyperledger.fabric.contract.Context;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class ExamResultContractTest extends TestCreationBase {

    private final ExamResultContract contract = new ExamResultContract();
    private final ExamResultContractUtil cUtil = new ExamResultContractUtil();


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

            Type listType = new TypeToken<ArrayList<ExamResultEntry>>() {}.getType();
            List<ExamResultEntry> compareExamResult = GsonWrapper.fromJson(compare.get(0), listType);
            List<ExamResultEntry> ledgerExamResult = GsonWrapper.fromJson(
                    contract.getExamResultEntries(ctx, input.get(0), input.get(1)), listType);
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