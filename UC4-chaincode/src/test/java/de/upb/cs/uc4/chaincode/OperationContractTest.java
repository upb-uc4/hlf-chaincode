package de.upb.cs.uc4.chaincode;


import com.google.common.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.matriculationdata.MatriculationDataContract;
import de.upb.cs.uc4.chaincode.contract.operation.OperationContract;
import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.contract.operation.OperationContractUtil;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public final class OperationContractTest extends TestCreationBase {

    private final OperationContract contract = new OperationContract();
    private final OperationContractUtil cUtil = new OperationContractUtil();

    String getTestConfigDir() {
        return "src/test/resources/test_configs/operation_contract";
    }

    DynamicTest CreateTest(JsonIOTest test) {
        String testType = test.getType();
        String testName = test.getName();
        JsonIOTestSetup setup = test.getSetup();
        List<String> input = TestUtil.toStringList(test.getInput());
        List<String> compare = TestUtil.toStringList(test.getCompare());
        List<String> ids = test.getIds();

        switch (testType) {
            case "getOperations":
                return DynamicTest.dynamicTest(testName, getOperationsTest(setup, input, compare));
            case "approveTransaction_SUCCESS":
                return DynamicTest.dynamicTest(test.getName(), approveTransactionSuccessTest(setup, input, compare, ids));
            case "approveTransaction_FAILURE":
                return DynamicTest.dynamicTest(test.getName(), approveTransactionFailureTest(setup, input, compare, ids));
            case "rejectTransaction_SUCCESS":
                return DynamicTest.dynamicTest(test.getName(), rejectTransactionSuccessTest(setup, input, compare, ids));
            case "rejectTransaction_FAILURE":
                return DynamicTest.dynamicTest(test.getName(), rejectTransactionFailureTest(setup, input, compare, ids));
            case "getOperationData_SUCCESS":
                return DynamicTest.dynamicTest(test.getName(), getOperationDataSuccessTest(setup, input, compare, ids));
            case "finishOperation":
                return DynamicTest.dynamicTest(test.getName(), finishOperationTest(setup, input, compare, ids));
            default:
                throw new RuntimeException("Test " + testName + " of type " + testType + " could not be matched.");
        }
    }

    private Executable getOperationsTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, "UC4.OperationData:getOperations");
            Context ctx = TestUtil.mockContext(stub);

            for (int i = 0; i < compare.size(); i++) {
                String operations = contract.getOperations(ctx, input.get(i*4), input.get(i*4+1), input.get(i*4+2), input.get(i*4+3));
                Type listType = new TypeToken<ArrayList<OperationData>>() {

                }.getType();
                ArrayList<OperationData> operationDataList = GsonWrapper.fromJson(operations, listType);
                List<String> operationIds = operationDataList.stream().map(operationData -> operationData.getOperationId()).collect(Collectors.toList());
                assertThat(GsonWrapper.toJson(operationIds)).isEqualTo(compare.get(i));
            }


        };
    }

    private Executable approveTransactionSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, "UC4.OperationData:approveTransaction");
            for (int i = 0; i < ids.size(); i++) {
                Context ctx = TestUtil.mockContext(stub, ids.get(i));
                OperationData compareResult = GsonWrapper.fromJson(compare.get(i), OperationData.class);
                OperationData transactionResult = GsonWrapper.fromJson(contract.initiateOperation(ctx, initiator(input), contract(input), transaction(input), params(input)), OperationData.class);
                assertThat(GsonWrapper.toJson(transactionResult)).isEqualTo(GsonWrapper.toJson(compareResult)); // TODO remove serialization
            }
        };
    }

    private Executable approveTransactionFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, "UC4.OperationData:approveTransaction");
            for (String s : compare) {
                Context ctx = cUtil.valueUnset(ids) ? TestUtil.mockContext(stub) : TestUtil.mockContext(stub, ids.get(0));
                String result = contract.initiateOperation(ctx, "", contract(input), transaction(input), params(input));
                assertThat(result).isEqualTo(s);
            }
        };
    }

    private Executable rejectTransactionSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, "UC4.OperationData:rejectTransaction");
            /*for (int i=0; i< ids.size(); i++) {
                Context ctx = TestUtil.mockContext(stub, ids.get(i));
                OperationData compareResult = GsonWrapper.fromJson(compare.get(i), OperationData.class);
                OperationData transactionResult = GsonWrapper.fromJson(contract.approveTransaction(ctx, contract(input), transaction(input), params(input)), OperationData.class);
                assertThat(GsonWrapper.toJson(transactionResult)).isEqualTo(GsonWrapper.toJson(compareResult)); // TODO remove serialization
            }*/
            Context ctx = TestUtil.mockContext(stub);
            String rejectResult = contract.rejectTransaction(ctx, input.get(0), input.get(1));
            OperationData compareOperationData = GsonWrapper.fromJson(compare.get(compare.size() - 1), OperationData.class);
            OperationData ledgerOperationData = cUtil.getState(ctx.getStub(), compareOperationData.getOperationId(), OperationData.class);
            assertThat(GsonWrapper.toJson(compareOperationData)).isEqualTo(GsonWrapper.toJson(ledgerOperationData));

            assertThat(compareOperationData).isEqualTo(ledgerOperationData);
            assertThat(GsonWrapper.toJson(compareOperationData)).isEqualTo(rejectResult);
        };
    }

    private Executable rejectTransactionFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, "UC4.OperationData:rejectTransaction");
            /*for (String s : compare) {
                Context ctx = cUtil.valueUnset(ids) ? TestUtil.mockContext(stub) : TestUtil.mockContext(stub, ids.get(0));
                String result = contract.approveTransaction(ctx, contract(input), transaction(input), params(input));
                assertThat(result).isEqualTo(s);
            }*/
            Context ctx = TestUtil.mockContext(stub);
            String result = contract.rejectTransaction(ctx, input.get(0), input.get(1));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable getOperationDataSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, "UC4.OperationData:getOperationData");
            Context ctx = TestUtil.mockContext(stub);
            String getResult = contract.getOperationData(ctx, input.get(0));
            assertThat(getResult).isEqualTo(compare.get(0));
            assertThat(GsonWrapper.fromJson(getResult, OperationData.class)).isEqualTo(GsonWrapper.fromJson(compare.get(0), OperationData.class));
        };
    }

    private Executable finishOperationTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup, "UC4.OperationData:approveTransaction");
            String operationJson = "";
            for (String id: ids) {
                Context ctx = TestUtil.mockContext(stub, id);
                operationJson = contract.initiateOperation(ctx, "", MatriculationDataContract.contractName, "addMatriculationData", GsonWrapper.toJson(input));
            }
            Context ctx = TestUtil.mockContext(stub);
            MatriculationDataContract matriculationContract = new MatriculationDataContract();
            stub.setFunction(MatriculationDataContract.contractName + ":addMatriculationData");
            System.out.println("########################################################################");
            System.out.println(matriculationContract.addMatriculationData(ctx, input.get(0)));
            System.out.println("########################################################################");
            String operationId = GsonWrapper.fromJson(operationJson, OperationData.class).getOperationId();
            stub.setFunction("UC4.OperationData:approveTransaction");
            OperationData operation = GsonWrapper.fromJson(contract.getOperationData(ctx, operationId), OperationData.class);
            OperationDataState expectedState = GsonWrapper.fromJson(compare.get(0), OperationDataState.class);
            assertThat(operation.getState()).isEqualTo(expectedState);
        };
    }

    private String initiator(List<String> input) {
        return input.get(0);
    }

    private String contract(List<String> input) {
        return input.get(1);
    }

    private String transaction(List<String> input) {
        return input.get(2);
    }

    private String params(List<String> input) {
        return GsonWrapper.toJson(input.subList(3, input.size()));

    }

    private String operationId(List<String> input) throws NoSuchAlgorithmException {
        return OperationContractUtil.getDraftKey(contract(input), transaction(input), params(input));
    }
}
