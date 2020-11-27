package de.upb.cs.uc4.chaincode;


import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.Approval;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.model.JsonIOTestSetup;
import de.upb.cs.uc4.chaincode.model.MatriculationData;
import de.upb.cs.uc4.chaincode.util.AdmissionContractUtil;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class AdmissionContractTest {

    private final AdmissionContract contract = new AdmissionContract();
    private final AdmissionContractUtil cUtil = new AdmissionContractUtil();

    @TestFactory
    List<DynamicTest> createTests() {
        String testConfigDir = "src/test/resources/test_configs/admission_contract";
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
                    case "addAdmission_SUCCESS":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                addAdmissionSuccessTest(setup, input, compare, test.getIds())
                        ));
                        break;
                    default:
                        throw new RuntimeException("Test " + test.getName() + " of type " + test.getType() + " could not be matched.");
                }
            }
        }
        return tests;
    }

    private Executable addAdmissionSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<Approval> ids
    ) {
        return () -> {
            MockChaincodeStub stub = TestUtil.mockStub(setup);
            /*
            ApprovalContract approvalContract = new ApprovalContract();
            for (Approval id: ids) {
                Context ctx = TestUtil.mockContext(stub, id);
                approvalContract.approveTransaction(ctx, contract.contractName,"addAdmission", input.get(0));
            }*/
            Context ctx = TestUtil.mockContext(stub);
            assertThat(contract.addAdmission(ctx, input.get(0), input.get(1), input.get(2), input.get(3)))
                    .isEqualTo(compare.get(0));
            MatriculationData matriculationData = GsonWrapper.fromJson(compare.get(0), MatriculationData.class);
            assertThat(cUtil.getStringState(ctx.getStub(), matriculationData.getEnrollmentId()))
                    .isEqualTo(compare.get(0));
        };
    }
}