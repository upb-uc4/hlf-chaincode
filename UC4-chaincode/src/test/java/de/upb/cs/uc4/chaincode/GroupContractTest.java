package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContract;
import de.upb.cs.uc4.chaincode.contract.examinationregulation.ExaminationRegulationContract;
import de.upb.cs.uc4.chaincode.contract.group.GroupContract;
import de.upb.cs.uc4.chaincode.contract.operation.OperationContract;
import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.DetailedError;
import de.upb.cs.uc4.chaincode.contract.group.GroupContractUtil;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import org.hyperledger.fabric.contract.Context;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class GroupContractTest extends TestCreationBase {

    private final GroupContract contract = new GroupContract();
    private final GroupContractUtil cUtil = new GroupContractUtil();

    String getTestConfigDir() {
        return "src/test/resources/test_configs/group_contract";
    }

    DynamicTest CreateTest(JsonIOTest test) {
        String testType = test.getType();
        String testName = test.getName();
        JsonIOTestSetup setup = test.getSetup();
        List<String> input = TestUtil.toStringList(test.getInput());
        List<String> compare = TestUtil.toStringList(test.getCompare());
        List<String> ids = test.getIds();

        switch (testType) {
            case "addUserToGroup_SUCCESS":
                return DynamicTest.dynamicTest(testName, addUserToGroupSuccessTest(setup, input, compare, ids));
            case "addUserToGroup_FAILURE":
                return DynamicTest.dynamicTest(testName, addUserToGroupFailureTest(setup, input, compare, ids));
            case "removeUserFromGroup_SUCCESS":
                return DynamicTest.dynamicTest(testName, removeUserFromGroupSuccessTest(setup, input, compare, ids));
            case "removeUserFromGroup_FAILURE":
                return DynamicTest.dynamicTest(testName, removeUserFromGroupFailureTest(setup, input, compare, ids));
            case "removeUserFromAllGroups_SUCCESS":
                return DynamicTest.dynamicTest(testName, removeUserFromAllGroupsSuccessTest(setup, input, compare, ids));
            case "removeUserFromAllGroups_FAILURE":
                return DynamicTest.dynamicTest(testName, removeUserFromAllGroupsFailureTest(setup, input, compare, ids));
            case "getAllGroups_SUCCESS":
                return DynamicTest.dynamicTest(testName, getAllGroupsSuccessTest(setup, input, compare, ids));
            case "getUsersForGroup_SUCCESS":
                return DynamicTest.dynamicTest(testName, getUsersForGroupSuccessTest(setup, input, compare, ids));
            case "getUsersForGroup_FAILURE":
                return DynamicTest.dynamicTest(testName, getUsersForGroupFailureTest(setup, input, compare, ids));
            case "getGroupsForUser_SUCCESS":
                return DynamicTest.dynamicTest(testName, getGroupsForUserSuccessTest(setup, input, compare, ids));
            case "getGroupsForUser_FAILURE":
                return DynamicTest.dynamicTest(testName, getGroupsForUserFailureTest(setup, input, compare, ids));
            default:
                throw new RuntimeException("Test " + testName + " of type " + testType + " could not be matched.");
        }
    }

    private Executable addUserToGroupSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(GroupContract.contractName, GroupContract.transactionNameAddUserToGroup, setup, input, ids);

            String addResult = contract.addUserToGroup(ctx, input.get(0), input.get(1));
            assertThat(addResult).isEqualTo("");

            Group compareGroup = GsonWrapper.fromJson(compare.get(0), Group.class);
            Group ledgerGroup = cUtil.getState(ctx.getStub(), compareGroup.getGroupId(), Group.class);
            assertThat(ledgerGroup).isEqualTo(compareGroup);
            assertThat(ledgerGroup.toString()).isEqualTo(compareGroup.toString());
        };
    }

    private Executable addUserToGroupFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(GroupContract.contractName, GroupContract.transactionNameAddUserToGroup, setup, input, ids);

            String result = contract.addUserToGroup(ctx, input.get(0), input.get(1));
            DetailedError actualError = GsonWrapper.fromJson(result, DetailedError.class);
            DetailedError expectedError = GsonWrapper.fromJson(compare.get(0), DetailedError.class);
            assertThat(actualError).isEqualTo(expectedError);
            assertThat(actualError.toString()).isEqualTo(expectedError.toString());
        };
    }

    private Executable removeUserFromGroupSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(GroupContract.contractName, GroupContract.transactionNameRemoveUserFromGroup, setup, input, ids);

            String dropResult = contract.removeUserFromGroup(ctx, input.get(0), input.get(1));
            assertThat(dropResult).isEqualTo("");

            for (String i : compare) {
                Group compareGroup = GsonWrapper.fromJson(i, Group.class);
                Group ledgerGroup = cUtil.getState(ctx.getStub(), compareGroup.getGroupId(), Group.class);
                assertThat(ledgerGroup).isEqualTo(compareGroup);
                assertThat(ledgerGroup.toString()).isEqualTo(compareGroup.toString());
            }
        };
    }

    private Executable removeUserFromGroupFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(GroupContract.contractName, GroupContract.transactionNameRemoveUserFromGroup, setup, input, ids);

            String result = contract.removeUserFromGroup(ctx, input.get(0), input.get(1));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }


    private Executable removeUserFromAllGroupsSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(GroupContract.contractName, GroupContract.transactionNameRemoveUserFromAllGroups, setup, input, ids);

            String dropResult = contract.removeUserFromAllGroups(ctx, input.get(0));
            assertThat(dropResult).isEqualTo("");

            for (String i : compare) {
                Group compareGroup = GsonWrapper.fromJson(i, Group.class);
                Group ledgerGroup = cUtil.getState(ctx.getStub(), compareGroup.getGroupId(), Group.class);
                assertThat(ledgerGroup).isEqualTo(compareGroup);
                assertThat(ledgerGroup.toString()).isEqualTo(compareGroup.toString());
            }
        };
    }

    private Executable removeUserFromAllGroupsFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(GroupContract.contractName, GroupContract.transactionNameRemoveUserFromAllGroups, setup, input, ids);

            String result = contract.removeUserFromAllGroups(ctx, input.get(0));
            DetailedError actualError = GsonWrapper.fromJson(result, DetailedError.class);
            DetailedError expectedError = GsonWrapper.fromJson(compare.get(0), DetailedError.class);
            assertThat(actualError).isEqualTo(expectedError);
            assertThat(actualError.toString()).isEqualTo(expectedError.toString());
        };
    }


    private Executable getAllGroupsSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(GroupContract.contractName, GroupContract.transactionNameGetAllGroups, setup, input, ids);

            String dropResult = contract.getAllGroups(ctx);
            assertThat(dropResult).isEqualTo(compare.get(0));
        };
    }

    private Executable getUsersForGroupSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(GroupContract.contractName, GroupContract.transactionNameGetUsersForGroup, setup, input, ids);

            String dropResult = contract.getUsersForGroup(ctx, input.get(0));
            assertThat(dropResult).isEqualTo(compare.get(0));
        };
    }

    private Executable getUsersForGroupFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(GroupContract.contractName, GroupContract.transactionNameGetUsersForGroup, setup, input, ids);

            String result = contract.getUsersForGroup(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable getGroupsForUserSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(GroupContract.contractName, GroupContract.transactionNameGetGroupsForUser, setup, input, ids);

            String dropResult = contract.getGroupsForUser(ctx, input.get(0));
            assertThat(dropResult).isEqualTo(compare.get(0));
        };
    }

    private Executable getGroupsForUserFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(GroupContract.contractName, GroupContract.transactionNameGetGroupsForUser, setup, input, ids);
            String result = contract.getGroupsForUser(ctx, input.get(0));
            DetailedError actualError = GsonWrapper.fromJson(result, DetailedError.class);
            DetailedError expectedError = GsonWrapper.fromJson(compare.get(0), DetailedError.class);
            assertThat(actualError).isEqualTo(expectedError);
            assertThat(actualError.toString()).isEqualTo(expectedError.toString());
        };
    }

}