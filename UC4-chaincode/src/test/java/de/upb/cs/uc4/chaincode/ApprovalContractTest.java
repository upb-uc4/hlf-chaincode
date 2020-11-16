package de.upb.cs.uc4.chaincode;


import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.Approval;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.model.JsonIOTestSetup;
import de.upb.cs.uc4.chaincode.util.ApprovalContractUtil;
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

public final class ApprovalContractTest {

    private final ApprovalContract contract = new ApprovalContract();
    private final ApprovalContractUtil cUtil = new ApprovalContractUtil();

    @TestFactory
    List<DynamicTest> createTests() {
        String testConfigDir = "src/test/resources/test_configs/approval_contract";
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
                    case "getApprovals":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                getApprovalsTest(setup, input, compare)
                        ));
                        break;
                    case "approveTransaction_SUCCESS":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                approveTransactionSuccessTest(setup, input, compare, test.getIds())
                        ));
                        break;
                    case "approveTransaction_FAILURE":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                approveTransactionFailureTest(setup, input, compare, test.getIds())
                        ));
                        break;
                    default:
                        throw new RuntimeException("Test " + test.getName() + " of type " + test.getType() + " could not be matched.");
                }
            }
        }
        return tests;
    }

    private Executable getApprovalsTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            Context ctx = TestUtil.mockContext(stub);

            String approvals = contract.getApprovals(ctx, contract(input), transaction(input), params(input));
            assertThat(approvals).isEqualTo(compare.get(0));
        };
    }

    private Executable approveTransactionSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<Approval> ids
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            for (Approval id: ids) {
                Context ctx = TestUtil.mockContext(stub, id);
                assertThat(contract.approveTransaction(ctx, contract(input), transaction(input), params(input)))
                        .isEqualTo(compare.get(0));
            }
            Context ctx = TestUtil.mockContext(stub);
            String key = cUtil.getDraftKey(contract(input), transaction(input), params(input));
            assertThat(cUtil.getStringState(ctx.getStub(), key))
                    .isEqualTo(compare.get(0));
        };
    }

    private Executable approveTransactionFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<Approval> ids
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            for (String s : compare) {
                Context ctx = cUtil.valueUnset(ids) ? TestUtil.mockContext(stub) : TestUtil.mockContext(stub, ids.get(0));
                String result = contract.approveTransaction(ctx, contract(input), transaction(input), params(input));
                assertThat(result).isEqualTo(s);
            }
        };
    }

    private String contract(List<String> input) {
        return input.get(0);
    }

    private String transaction(List<String> input) {
        return input.get(1);
    }

    private String[] params(List<String> input) {
        List<String> paramList = input.subList(2, input.size());
        String[] params = new String[paramList.size()];
        paramList.toArray(params);
        return params;
    }
}