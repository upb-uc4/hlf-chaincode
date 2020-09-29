package de.upb.cs.uc4.chaincode;


import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.Dummy;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CertificateChaincodeTest {

    @TestFactory
    List<DynamicTest> createTests() {
        String testConfigDir = "src/test/resources/test_configs/certificate_contract";
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

    static Executable getCertificateTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            CertificateChaincode contract = new CertificateChaincode();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState(null)).thenThrow(new RuntimeException());
            when(stub.getStringState("")).thenThrow(new RuntimeException());
            if (!setup.isEmpty())
                when(stub.getStringState(setup.get(0)))
                        .thenReturn(setup.get(1));
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
            CertificateChaincode contract = new CertificateChaincode();
            Context ctx = mock(Context.class);
            MockChaincodeStub stub = new MockChaincodeStub();
            when(ctx.getStub()).thenReturn(stub);
            if (!setup.isEmpty()) {
                stub.putStringState(setup.get(0), setup.get(1));
            }
            assertThat(
                    contract.addCertificate(ctx, input.get(0), input.get(1)))
                            .isEqualTo(compare.get(0));
            String certificate = compare.get(0);
            assertThat(
                    stub.getStringState(input.get(0)))
                    .isEqualTo(compare.get(0));
        };
    }

    private Executable addCertificateFailureTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            CertificateChaincode contract = new CertificateChaincode();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState(null)).thenThrow(new RuntimeException());
            when(stub.getStringState("")).thenThrow(new RuntimeException());
            if (!setup.isEmpty()) {
                when(stub.getStringState(setup.get(0)))
                        .thenReturn(setup.get(1));
            }
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
            CertificateChaincode contract = new CertificateChaincode();
            Context ctx = mock(Context.class);
            MockChaincodeStub stub = new MockChaincodeStub();
            when(ctx.getStub()).thenReturn(stub);
            stub.putStringState(setup.get(0), setup.get(1));
            assertThat(
                    contract.updateCertificate(ctx, input.get(0), input.get(1)))
                    .isEqualTo(compare.get(0));
            String certificate = compare.get(0);
            assertThat(
                    stub.getStringState(
                            input.get(0)))
                    .isEqualTo(compare.get(0));
        };

    }

    private Executable updateCertificateFailureTest(
            List<String> setup,
            List<String> input,
            List<String> compare
    ) {
        return () -> {
            CertificateChaincode contract = new CertificateChaincode();
            Context ctx = mock(Context.class);
            MockChaincodeStub stub = new MockChaincodeStub();
            when(ctx.getStub()).thenReturn(stub);
            stub.putStringState(setup.get(0), setup.get(1));
            String result = contract.updateCertificate(ctx, input.get(0), input.get(1));

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