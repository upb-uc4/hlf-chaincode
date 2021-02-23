package de.upb.cs.uc4.chaincode;


import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContract;
import de.upb.cs.uc4.chaincode.model.ExaminationRegulation.ExaminationRegulation;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.model.JsonIOTestSetup;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.operation.OperationData;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import org.hyperledger.fabric.contract.Context;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class ExaminationRegulationContractTest extends TestCreationBase {

    private final ExaminationRegulationContract contract = new ExaminationRegulationContract();
    private final ExaminationRegulationContractUtil cUtil = new ExaminationRegulationContractUtil();

    String getTestConfigDir() {
        return "src/test/resources/test_configs/examination_regulation_contract";
    }

    DynamicTest CreateTest(JsonIOTest test) {
        String testType = test.getType();
        String testName = test.getName();
        JsonIOTestSetup setup = test.getSetup();
        List<String> input = TestUtil.toStringList(test.getInput());
        List<String> compare = TestUtil.toStringList(test.getCompare());
        List<String> ids = test.getIds();

        switch (testType) {
            case "getExaminationRegulations":
                return DynamicTest.dynamicTest(testName, getExaminationRegulationsTest(setup, input, compare, ids));
            case "addExaminationRegulation_SUCCESS":
                return DynamicTest.dynamicTest(testName, addExaminationRegulationSuccessTest(setup, input, compare, ids));
            case "addExaminationRegulation_FAILURE":
                return DynamicTest.dynamicTest(testName, addExaminationRegulationFailureTest(setup, input, compare, ids));
            case "closeExaminationRegulation_SUCCESS":
                return DynamicTest.dynamicTest(testName, closeExaminationRegulationSuccessTest(setup, input, compare, ids));
            case "closeExaminationRegulation_FAILURE":
                return DynamicTest.dynamicTest(testName, closeExaminationRegulationFailureTest(setup, input, compare, ids));
            default:
                throw new RuntimeException("Test " + testName + " of type " + testType + " could not be matched.");
        }
    }

    private Executable getExaminationRegulationsTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(ExaminationRegulationContract.contractName, ExaminationRegulationContract.transactionNameGetExaminationRegulations, setup, input, ids);

            String regulations = contract.getExaminationRegulations(ctx, input.get(0));

            assertThat(regulations).isEqualTo(compare.get(0));
        };
    }

    private Executable addExaminationRegulationSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(ExaminationRegulationContract.contractName, ExaminationRegulationContract.transactionNameAddExaminationRegulation, setup, input, ids);

            String result = contract.addExaminationRegulation(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));

            ExaminationRegulation compareRegulation = GsonWrapper.fromJson(compare.get(0), ExaminationRegulation.class);
            ExaminationRegulation ledgerRegulation = cUtil.getState(ctx.getStub(), compareRegulation.getName(), ExaminationRegulation.class);
            assertThat(ledgerRegulation).isEqualTo(compareRegulation);
            assertThat(ledgerRegulation.toString()).isEqualTo(compareRegulation.toString());
        };
    }

    private Executable addExaminationRegulationFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(ExaminationRegulationContract.contractName, ExaminationRegulationContract.transactionNameAddExaminationRegulation, setup, input, ids);

            String result = contract.addExaminationRegulation(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable closeExaminationRegulationSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(ExaminationRegulationContract.contractName, ExaminationRegulationContract.transactionNameCloseExaminationRegulation, setup, input, ids);

            String result = contract.closeExaminationRegulation(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));

            ExaminationRegulation compareRegulation = GsonWrapper.fromJson(compare.get(0), ExaminationRegulation.class);
            ExaminationRegulation ledgerRegulation = cUtil.getState(ctx.getStub(), compareRegulation.getName(), ExaminationRegulation.class);
            assertThat(ledgerRegulation).isEqualTo(compareRegulation);
            assertThat(ledgerRegulation.toString()).isEqualTo(compareRegulation.toString());
        };

    }

    private Executable closeExaminationRegulationFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(ExaminationRegulationContract.contractName, ExaminationRegulationContract.transactionNameCloseExaminationRegulation, setup, input, ids);

            String result = contract.closeExaminationRegulation(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }
}