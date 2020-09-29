package de.upb.cs.uc4.chaincode;


import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.Dummy;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.model.MatriculationData;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MatriculationDataChaincodeTest {

    @TestFactory
    List<DynamicTest> createTests() {
        String testConfigDir = "src/test/resources/test_configs/matriculation_data_contract";
        File dir = new File(testConfigDir);
        File[] testConfigs = dir.listFiles();

        List<JsonIOTest> testConfig;
        Type type = new TypeToken<List<JsonIOTest>>() {
        }.getType();
        ArrayList<DynamicTest> tests = new ArrayList<>();

        if (testConfigs == null) {
            throw new RuntimeException("No test configurations found.");
        }

        for (File file: testConfigs) {
            try {
                testConfig = GsonWrapper.fromJson(
                        new FileReader(file.getPath()),
                        type);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            for (JsonIOTest test : testConfig) {
                List<String> setup = TestUtil.toStringList(test.getSetup());
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
                                addMatriculationDataSuccessTest(setup, input, compare)
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

    static Executable getMatriculationDataTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MatriculationDataChaincode contract = new MatriculationDataChaincode();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState(null)).thenThrow(new RuntimeException());
            when(stub.getStringState("")).thenThrow(new RuntimeException());
            if (!setup.isEmpty())
                when(stub.getStringState(setup.get(0))).thenReturn(setup.get(1));
            MatriculationData matriculationData = GsonWrapper.fromJson(
                    contract.getMatriculationData(ctx, input.get(0)),
                    MatriculationData.class);
            assertThat(matriculationData)
                    .isEqualTo(GsonWrapper.fromJson(compare.get(0), MatriculationData.class));
        };
    }

    private Executable addMatriculationDataSuccessTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MatriculationDataChaincode contract = new MatriculationDataChaincode();
            Context ctx = mock(Context.class);
            MockChaincodeStub stub = new MockChaincodeStub();
            when(ctx.getStub()).thenReturn(stub);
            if (!setup.isEmpty()) {
                stub.putStringState(setup.get(0), setup.get(1));
            }
            assertThat(contract.addMatriculationData(ctx, input.get(0)))
                    .isEqualTo(compare.get(0));
            MatriculationData matriculationData = GsonWrapper.fromJson(compare.get(0), MatriculationData.class);
            assertThat(stub.getStringState(matriculationData.getEnrollmentId()))
                    .isEqualTo(compare.get(0));
        };
    }

    private Executable addMatriculationDataFailureTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MatriculationDataChaincode contract = new MatriculationDataChaincode();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState(null)).thenThrow(new RuntimeException());
            when(stub.getStringState("")).thenThrow(new RuntimeException());
            if (!setup.isEmpty()) {
                when(stub.getStringState(setup.get(0)))
                        .thenReturn(setup.get(1));
            }
            String result = contract.addMatriculationData(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable updateMatriculationDataSuccessTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MatriculationDataChaincode contract = new MatriculationDataChaincode();
            Context ctx = mock(Context.class);
            MockChaincodeStub stub = new MockChaincodeStub();
            when(ctx.getStub()).thenReturn(stub);
            stub.putStringState(setup.get(0), setup.get(1));
            assertThat(contract.updateMatriculationData(ctx, input.get(0)))
                    .isEqualTo(compare.get(0));
            MatriculationData matriculationData = GsonWrapper.fromJson(compare.get(0), MatriculationData.class);
            assertThat(stub.getStringState(matriculationData.getEnrollmentId()))
                    .isEqualTo(compare.get(0));
        };

    }

    private Executable updateMatriculationDataFailureTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MatriculationDataChaincode contract = new MatriculationDataChaincode();
            Context ctx = mock(Context.class);
            MockChaincodeStub stub = new MockChaincodeStub();
            when(ctx.getStub()).thenReturn(stub);
            stub.putStringState(setup.get(0), setup.get(1));
            String result = contract.updateMatriculationData(ctx, input.get(0));

            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable addEntryToMatriculationDataSuccessTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MatriculationDataChaincode contract = new MatriculationDataChaincode();
            Context ctx = mock(Context.class);
            MockChaincodeStub stub = new MockChaincodeStub();
            when(ctx.getStub()).thenReturn(stub);
            if (!setup.isEmpty()) {
                stub.putStringState(setup.get(0), setup.get(1));
            }
            assertThat(contract.addEntriesToMatriculationData(ctx, input.get(0), input.get(1)))
                    .isEqualTo(compare.get(0));
            MatriculationData matriculationData = GsonWrapper.fromJson(compare.get(0), MatriculationData.class);
            assertThat(stub.getStringState(matriculationData.getEnrollmentId()))
                    .isEqualTo(compare.get(0));
        };
    }

    private Executable addEntryToMatriculationDataFailureTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MatriculationDataChaincode contract = new MatriculationDataChaincode();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState(null)).thenThrow(new RuntimeException());
            when(stub.getStringState("")).thenThrow(new RuntimeException());
            if (!setup.isEmpty()) {
                when(stub.getStringState(setup.get(0)))
                        .thenReturn(setup.get(1));
            }
            String result = contract.addEntriesToMatriculationData(ctx, input.get(0), input.get(1));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private void setTransientMap(MockChaincodeStub stub, List<Dummy> input) {
        stub.setTransient(
                input.stream().collect(
                        Collectors.toMap(
                                entry -> String.valueOf(input.indexOf(entry)),
                                entry -> entry.getContent().getBytes())));
    }

    private void setTransientMapMock(ChaincodeStub stub, List<Dummy> input) {
        when(stub.getTransient()).thenReturn(
                input.stream().collect(
                        Collectors.toMap(
                                entry -> String.valueOf(input.indexOf(entry)),
                                entry -> entry.getContent().getBytes())));
    }
}