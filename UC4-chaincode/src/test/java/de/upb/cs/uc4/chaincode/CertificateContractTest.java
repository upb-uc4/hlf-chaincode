package de.upb.cs.uc4.chaincode;


import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.util.CertificateContractUtil;
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

public final class CertificateContractTest {

    private final CertificateContract contract = new CertificateContract();
    private final CertificateContractUtil cUtil = new CertificateContractUtil();

    @TestFactory
    List<DynamicTest> createTests() {
        String testConfigDir = "src/test/resources/test_configs/certificate_contract";
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
                    case "getCertificate":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                getCertificateTest(setup, input, compare)
                        ));
                        break;
                    case "addCertificate_SUCCESS":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                addCertificateSuccessTest(setup, input, compare)
                        ));
                        break;
                    case "addCertificate_FAILURE":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                addCertificateFailureTest(setup, input, compare)
                        ));
                        break;
                    case "updateCertificate_SUCCESS":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                updateCertificateSuccessTest(setup, input, compare)
                        ));
                        break;
                    case "updateCertificate_FAILURE":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                updateCertificateFailureTest(setup, input, compare)
                        ));
                        break;
                    default:
                        throw new RuntimeException("Test " + test.getName() + " of type " + test.getType() + " could not be matched.");
                }
            }
        }
        return tests;
    }

    private Executable getCertificateTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            Context ctx = TestUtil.mockContext(setup, cUtil);

            String certificate = contract.getCertificate(ctx, input.get(0));
            assertThat(certificate).isEqualTo(compare.get(0));
        };
    }

    private Executable addCertificateSuccessTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            Context ctx = TestUtil.mockContext(setup, cUtil);

            assertThat(contract.addCertificate(ctx, input.get(0), input.get(1)))
                    .isEqualTo(compare.get(0));
            assertThat(cUtil.getStringState(ctx.getStub(), input.get(0)))
                    .isEqualTo(compare.get(0));
        };
    }

    private Executable addCertificateFailureTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            Context ctx = TestUtil.mockContext(setup, cUtil);

            String result = contract.addCertificate(ctx, input.get(0), input.get(1));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable updateCertificateSuccessTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            Context ctx = TestUtil.mockContext(setup, cUtil);

            assertThat(contract.updateCertificate(ctx, input.get(0), input.get(1)))
                    .isEqualTo(compare.get(0));
            assertThat(cUtil.getStringState(ctx.getStub(), input.get(0)))
                    .isEqualTo(compare.get(0));
        };

    }

    private Executable updateCertificateFailureTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            Context ctx = TestUtil.mockContext(setup, cUtil);

            String result = contract.updateCertificate(ctx, input.get(0), input.get(1));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }
}