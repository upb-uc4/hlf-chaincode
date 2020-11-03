package de.upb.cs.uc4.chaincode;


import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.model.ExaminationRegulation;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.util.ExaminationRegulationContractUtil;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import org.hyperledger.fabric.contract.Context;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class ExaminationRegulationContractTest {

    private final ExaminationRegulationContract contract = new ExaminationRegulationContract();
    private final ExaminationRegulationContractUtil cUtil = new ExaminationRegulationContractUtil();

    @TestFactory
    List<DynamicTest> createTests() {
        String testConfigDir = "src/test/resources/test_configs/examination_regulation_contract";
        File dir = new File(testConfigDir);
        File[] testConfigs = dir.listFiles();

        List<JsonIOTest> testConfig;
        Type type = new TypeToken<List<JsonIOTest>>() {}.getType();
        ArrayList<DynamicTest> tests = new ArrayList<>();

        if (testConfigs == null) {
            throw new RuntimeException("No test configurations found.");
        }

        for (File file: testConfigs) {
            try {
                testConfig = GsonWrapper.fromJson(new FileReader(file.getPath()), type);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            for (JsonIOTest test : testConfig) {
                List<String> setup = TestUtil.toStringList(test.getSetup());
                List<String> input = TestUtil.toStringList(test.getInput());
                List<String> compare = TestUtil.toStringList(test.getCompare());
                switch (test.getType()) {
                    case "getExaminationRegulations":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                getExaminationRegulationsTest(setup, input, compare)
                        ));
                        break;
                    case "addExaminationRegulation_SUCCESS":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                addExaminationRegulationSuccessTest(setup, input, compare)
                        ));
                        break;
                    case "addExaminationRegulation_FAILURE":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                addExaminationRegulationFailureTest(setup, input, compare)
                        ));
                        break;
                    case "closeExaminationRegulation_SUCCESS":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                closeExaminationRegulationSuccessTest(setup, input, compare)
                        ));
                        break;
                    case "closeExaminationRegulation_FAILURE":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                closeExaminationRegulationFailureTest(setup, input, compare)
                        ));
                        break;
                    default:
                        throw new RuntimeException("Test " + test.getName() + " of type " + test.getType() + " could not be matched.");
                }
            }
        }
        return tests;
    }

    private Executable getExaminationRegulationsTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            Context ctx = TestUtil.mockContext(setup, cUtil);

            Type listType = new TypeToken<ArrayList<ExaminationRegulation>>(){}.getType();
            ArrayList<ExaminationRegulation> regulations = GsonWrapper.fromJson(
                    contract.getExaminationRegulations(ctx, input.get(0)),
                    listType);
            assertThat(regulations)
                    .isEqualTo(GsonWrapper.fromJson(compare.get(0), listType));
        };
    }

    private Executable addExaminationRegulationSuccessTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            Context ctx = TestUtil.mockContext(setup, cUtil);

            assertThat(contract.addExaminationRegulation(ctx, input.get(0)))
                    .isEqualTo(compare.get(0));
            ExaminationRegulation regulation = GsonWrapper.fromJson(compare.get(0), ExaminationRegulation.class);
            assertThat(cUtil.getStringState(ctx.getStub(), regulation.getName()))
                    .isEqualTo(compare.get(0));
        };
    }

    private Executable addExaminationRegulationFailureTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            Context ctx = TestUtil.mockContext(setup, cUtil);

            String result = contract.addExaminationRegulation(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable closeExaminationRegulationSuccessTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            Context ctx = TestUtil.mockContext(setup, cUtil);

            assertThat(contract.closeExaminationRegulation(ctx, input.get(0)))
                    .isEqualTo(compare.get(0));
            ExaminationRegulation regulation = GsonWrapper.fromJson(compare.get(0), ExaminationRegulation.class);
            assertThat(cUtil.getStringState(ctx.getStub(), regulation.getName()))
                    .isEqualTo(compare.get(0));
        };

    }

    private Executable closeExaminationRegulationFailureTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            Context ctx = TestUtil.mockContext(setup, cUtil);

            String result = contract.closeExaminationRegulation(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }
}