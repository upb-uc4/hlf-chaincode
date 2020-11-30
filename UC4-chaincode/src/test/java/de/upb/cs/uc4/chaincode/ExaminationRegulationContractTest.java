package de.upb.cs.uc4.chaincode;


import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.Approval;
import de.upb.cs.uc4.chaincode.model.ExaminationRegulation;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.model.JsonIOTestSetup;
import de.upb.cs.uc4.chaincode.util.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import org.hyperledger.fabric.contract.Context;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class ExaminationRegulationContractTest extends TestCreationBase {

    private final ExaminationRegulationContract contract = new ExaminationRegulationContract();
    private final ExaminationRegulationContractUtil cUtil = new ExaminationRegulationContractUtil();

    String GetTestConfigDir() {
        return "src/test/resources/test_configs/examination_regulation_contract";
    }

    DynamicTest CreateTest(JsonIOTest test) {
        String testType = test.getType();
        String testName = test.getName();
        JsonIOTestSetup setup = test.getSetup();
        List<String> input = TestUtil.toStringList(test.getInput());
        List<String> compare = TestUtil.toStringList(test.getCompare());
        List<Approval> ids = test.getIds();

        switch (testType) {
            case "getExaminationRegulations":
                return DynamicTest.dynamicTest(testName, getExaminationRegulationsTest(setup, input, compare));
            case "addExaminationRegulation_SUCCESS":
                return DynamicTest.dynamicTest(testName, addExaminationRegulationSuccessTest(setup, input, compare));
            case "addExaminationRegulation_FAILURE":
                return DynamicTest.dynamicTest(testName, addExaminationRegulationFailureTest(setup, input, compare));
            case "closeExaminationRegulation_SUCCESS":
                return DynamicTest.dynamicTest(testName, closeExaminationRegulationSuccessTest(setup, input, compare));
            case "closeExaminationRegulation_FAILURE":
                return DynamicTest.dynamicTest(testName, closeExaminationRegulationFailureTest(setup, input, compare));
            default:
                throw new RuntimeException("Test " + testName + " of type " + testType + " could not be matched.");
        }
    }

    private Executable getExaminationRegulationsTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            Context ctx = TestUtil.mockContext(stub);

            String regulations = contract.getExaminationRegulations(ctx, input.get(0));
            assertThat(regulations)
                    .isEqualTo(compare.get(0));
        };
    }

    private Executable addExaminationRegulationSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            Context ctx = TestUtil.mockContext(stub);

            assertThat(contract.addExaminationRegulation(ctx, input.get(0)))
                    .isEqualTo(compare.get(0));
            ExaminationRegulation regulation = GsonWrapper.fromJson(compare.get(0), ExaminationRegulation.class);
            assertThat(cUtil.getStringState(ctx.getStub(), regulation.getName()))
                    .isEqualTo(compare.get(0));
        };
    }

    private Executable addExaminationRegulationFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            Context ctx = TestUtil.mockContext(stub);

            String result = contract.addExaminationRegulation(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable closeExaminationRegulationSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            Context ctx = TestUtil.mockContext(stub);

            assertThat(contract.closeExaminationRegulation(ctx, input.get(0)))
                    .isEqualTo(compare.get(0));
            ExaminationRegulation regulation = GsonWrapper.fromJson(compare.get(0), ExaminationRegulation.class);
            assertThat(cUtil.getStringState(ctx.getStub(), regulation.getName()))
                    .isEqualTo(compare.get(0));
        };

    }

    private Executable closeExaminationRegulationFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            Context ctx = TestUtil.mockContext(stub);

            String result = contract.closeExaminationRegulation(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }
}