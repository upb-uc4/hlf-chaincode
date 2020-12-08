package de.upb.cs.uc4.chaincode;


import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.model.JsonIOTestSetup;
import de.upb.cs.uc4.chaincode.model.MatriculationData;
import de.upb.cs.uc4.chaincode.util.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.util.MatriculationDataContractUtil;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import org.hyperledger.fabric.contract.Context;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public final class MatriculationDataContractTest extends TestCreationBase{

    private final MatriculationDataContract contract = new MatriculationDataContract();
    private final MatriculationDataContractUtil cUtil = new MatriculationDataContractUtil();


    String getTestConfigDir() {
        return "src/test/resources/test_configs/matriculation_data_contract";
    }

    DynamicTest CreateTest(JsonIOTest test) {
        String testType = test.getType();
        String testName = test.getName();
        JsonIOTestSetup setup = test.getSetup();
        List<String> input = TestUtil.toStringList(test.getInput());
        List<String> compare = TestUtil.toStringList(test.getCompare());
        List<ApprovalList> ids = test.getIds();

        switch (testType) {
            case "getMatriculationData":
                return DynamicTest.dynamicTest(testName, getMatriculationDataTest(setup, input, compare));
            case "addMatriculationData_SUCCESS":
                return DynamicTest.dynamicTest(testName, addMatriculationDataSuccessTest(setup, input, compare, ids));
            case "addMatriculationData_FAILURE":
                return DynamicTest.dynamicTest(testName, addMatriculationDataFailureTest(setup, input, compare));
            case "updateMatriculationData_SUCCESS":
                return DynamicTest.dynamicTest(testName, updateMatriculationDataSuccessTest(setup, input, compare));
            case "updateMatriculationData_FAILURE":
                return DynamicTest.dynamicTest(testName, updateMatriculationDataFailureTest(setup, input, compare));
            case "addEntryToMatriculationData_SUCCESS":
                return DynamicTest.dynamicTest(testName, addEntryToMatriculationDataSuccessTest(setup, input, compare));
            case "addEntryToMatriculationData_FAILURE":
                return DynamicTest.dynamicTest(testName, addEntryToMatriculationDataFailureTest(setup, input, compare));
            default:
                throw new RuntimeException("Test " + testName + " of type " + testType + " could not be matched.");
        }
    }

    private Executable getMatriculationDataTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            Context ctx = TestUtil.mockContext(stub);

            MatriculationData compareMatriculationData = GsonWrapper.fromJson(compare.get(0), MatriculationData.class);
            MatriculationData ledgerMatriculationData = GsonWrapper.fromJson(
                    contract.getMatriculationData(ctx, input.get(0)), MatriculationData.class);
            assertThat(ledgerMatriculationData).isEqualTo(compareMatriculationData);
        };
    }

    private Executable addMatriculationDataSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<ApprovalList> ids
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            ApprovalContract approvalContract = new ApprovalContract();
            for (ApprovalList id: ids) {
                Context ctx = TestUtil.mockContext(stub, id);
                approvalContract.approveTransaction(ctx, contract.contractName,"addMatriculationData", input.get(0));
            }
            Context ctx = TestUtil.mockContext(stub);

            String result = contract.addMatriculationData(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));

            MatriculationData compareMatriculationData = GsonWrapper.fromJson(compare.get(0), MatriculationData.class);
            MatriculationData ledgerMatriculationData =
                    cUtil.getState(ctx.getStub(), compareMatriculationData.getEnrollmentId(), MatriculationData.class);
            assertThat(ledgerMatriculationData).isEqualTo(compareMatriculationData);
        };
    }

    private Executable addMatriculationDataFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
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
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            Context ctx = TestUtil.mockContext(stub);

            String result = contract.updateMatriculationData(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));

            MatriculationData compareMatriculationData = GsonWrapper.fromJson(compare.get(0), MatriculationData.class);
            MatriculationData ledgerMatriculationData =
                    cUtil.getState(ctx.getStub(), compareMatriculationData.getEnrollmentId(), MatriculationData.class);
            assertThat(ledgerMatriculationData).isEqualTo(compareMatriculationData);
        };

    }

    private Executable updateMatriculationDataFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
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
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            Context ctx = TestUtil.mockContext(stub);

            String result = contract.addEntriesToMatriculationData(ctx, input.get(0), input.get(1));
            assertThat(result).isEqualTo(compare.get(0));

            MatriculationData compareMatriculationData = GsonWrapper.fromJson(compare.get(0), MatriculationData.class);
            MatriculationData ledgerMatriculationData =
                    cUtil.getState(ctx.getStub(), compareMatriculationData.getEnrollmentId(), MatriculationData.class);
            assertThat(ledgerMatriculationData).isEqualTo(compareMatriculationData);
        };
    }

    private Executable addEntryToMatriculationDataFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            Context ctx = TestUtil.mockContext(stub);

            String result = contract.addEntriesToMatriculationData(ctx, input.get(0), input.get(1));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }
}