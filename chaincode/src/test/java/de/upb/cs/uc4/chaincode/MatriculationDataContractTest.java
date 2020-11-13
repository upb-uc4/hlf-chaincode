package de.upb.cs.uc4.chaincode;


import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.Approval;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.model.JsonIOTestSetup;
import de.upb.cs.uc4.chaincode.model.MatriculationData;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import de.upb.cs.uc4.chaincode.util.MatriculationDataContractUtil;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import org.hyperledger.fabric.contract.Context;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public final class MatriculationDataContractTest {

    private final MatriculationDataContract contract = new MatriculationDataContract();
    private final MatriculationDataContractUtil cUtil = new MatriculationDataContractUtil();

    @TestFactory
    List<DynamicTest> createTests() {
        String testConfigDir = "src/test/resources/test_configs/matriculation_data_contract";
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
                JsonIOTestSetup setup = test.getSetup();
                List<String> input = TestUtil.toStringList(test.getInput());
                List<String> compare = TestUtil.toStringList(test.getCompare());
                switch (test.getType()) {
                    case "getMatriculationData":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                getMatriculationDataTest(setup, input, compare)
                        ));
                        break;
                    case "addMatriculationData_SUCCESS":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                addMatriculationDataSuccessTest(setup, input, compare, test.getIds())
                        ));
                        break;
                    case "addMatriculationData_FAILURE":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                addMatriculationDataFailureTest(setup, input, compare)
                        ));
                        break;
                    case "updateMatriculationData_SUCCESS":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                updateMatriculationDataSuccessTest(setup, input, compare)
                        ));
                        break;
                    case "updateMatriculationData_FAILURE":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                updateMatriculationDataFailureTest(setup, input, compare)
                        ));
                        break;
                    case "addEntryToMatriculationData_SUCCESS":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                addEntryToMatriculationDataSuccessTest(setup, input, compare)
                        ));
                        break;
                    case "addEntryToMatriculationData_FAILURE":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                addEntryToMatriculationDataFailureTest(setup, input, compare)
                        ));
                        break;
                    default:
                        throw new RuntimeException("Test " + test.getName() + " of type " + test.getType() + " could not be matched.");
                }
            }
        }
        return tests;
    }

    private Executable getMatriculationDataTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, cUtil);
            Context ctx = TestUtil.mockContext(stub);

            MatriculationData matriculationData = GsonWrapper.fromJson(
                    contract.getMatriculationData(ctx, input.get(0)),
                    MatriculationData.class);
            assertThat(matriculationData)
                    .isEqualTo(GsonWrapper.fromJson(compare.get(0), MatriculationData.class));
        };
    }

    private Executable addMatriculationDataSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<Approval> ids
    ) {
        return () -> {
            // TODO: add ExaminationRegulations to setup (for fieldOfStudy verification)
            MockChaincodeStub stub = TestUtil.mockStub(setup, cUtil);
            ApprovalContract approvalContract = new ApprovalContract();
            for (Approval id: ids) {
                Context ctx = TestUtil.mockContext(stub, id);
                approvalContract.approveTransaction(ctx, contract.getContractName(),"addMatriculationData", input.get(0));
            }
            Context ctx = TestUtil.mockContext(stub);
            assertThat(contract.addMatriculationData(ctx, input.get(0)))
                    .isEqualTo(compare.get(0));
            MatriculationData matriculationData = GsonWrapper.fromJson(compare.get(0), MatriculationData.class);
            assertThat(cUtil.getStringState(ctx.getStub(), matriculationData.getEnrollmentId()))
                    .isEqualTo(compare.get(0));
        };
    }

    private Executable addMatriculationDataFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, cUtil);
            Context ctx = TestUtil.mockContext(stub);

            String result = contract.addMatriculationData(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable updateMatriculationDataSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, cUtil);
            Context ctx = TestUtil.mockContext(stub);

            assertThat(contract.updateMatriculationData(ctx, input.get(0)))
                    .isEqualTo(compare.get(0));
            MatriculationData matriculationData = GsonWrapper.fromJson(compare.get(0), MatriculationData.class);
            assertThat(cUtil.getStringState(ctx.getStub(), matriculationData.getEnrollmentId()))
                    .isEqualTo(compare.get(0));
        };

    }

    private Executable updateMatriculationDataFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, cUtil);
            Context ctx = TestUtil.mockContext(stub);

            String result = contract.updateMatriculationData(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable addEntryToMatriculationDataSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, cUtil);
            Context ctx = TestUtil.mockContext(stub);

            assertThat(contract.addEntriesToMatriculationData(ctx, input.get(0), input.get(1)))
                    .isEqualTo(compare.get(0));
            MatriculationData matriculationData = GsonWrapper.fromJson(compare.get(0), MatriculationData.class);
            assertThat(cUtil.getStringState(ctx.getStub(), matriculationData.getEnrollmentId()))
                    .isEqualTo(compare.get(0));
        };
    }

    private Executable addEntryToMatriculationDataFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, cUtil);
            Context ctx = TestUtil.mockContext(stub);

            String result = contract.addEntriesToMatriculationData(ctx, input.get(0), input.get(1));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }
}