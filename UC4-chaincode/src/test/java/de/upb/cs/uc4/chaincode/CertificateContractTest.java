package de.upb.cs.uc4.chaincode;


import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContract;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContract;
import de.upb.cs.uc4.chaincode.contract.operation.OperationContract;
import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.model.JsonIOTestSetup;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContractUtil;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
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

public final class CertificateContractTest extends TestCreationBase {

    private final CertificateContract contract = new CertificateContract();
    private final CertificateContractUtil cUtil = new CertificateContractUtil();

    String getTestConfigDir() {
        return "src/test/resources/test_configs/certificate_contract";
    }

    DynamicTest CreateTest(JsonIOTest test) {
        String testType = test.getType();
        String testName = test.getName();
        JsonIOTestSetup setup = test.getSetup();
        List<String> input = TestUtil.toStringList(test.getInput());
        List<String> compare = TestUtil.toStringList(test.getCompare());
        List<String> ids = test.getIds();

        switch (testType) {
            case "getCertificate":
                return DynamicTest.dynamicTest(testName, getCertificateTest(setup, input, compare, ids));
            case "addCertificate_SUCCESS":
                return DynamicTest.dynamicTest(testName, addCertificateSuccessTest(setup, input, compare, ids));
            case "addCertificate_FAILURE":
                return DynamicTest.dynamicTest(testName, addCertificateFailureTest(setup, input, compare, ids));
            case "updateCertificate_SUCCESS":
                return DynamicTest.dynamicTest(testName, updateCertificateSuccessTest(setup, input, compare, ids));
            case "updateCertificate_FAILURE":
                return DynamicTest.dynamicTest(testName, updateCertificateFailureTest(setup, input, compare, ids));
            default:
                throw new RuntimeException("Test " + testName + " of type " + testType + " could not be matched.");
        }
    }

    private Executable getCertificateTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(CertificateContract.contractName, CertificateContract.transactionNameGetCertificate, setup, input, ids);

            String certificate = contract.getCertificate(ctx, input.get(0));
            assertThat(certificate).isEqualTo(compare.get(0));
        };
    }

    private Executable addCertificateSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(CertificateContract.contractName, CertificateContract.transactionNameAddCertificate, setup, input, ids);

            assertThat(contract.addCertificate(ctx, input.get(0), input.get(1)))
                    .isEqualTo(compare.get(0));
            assertThat(cUtil.getStringState(ctx.getStub(), input.get(0)))
                    .isEqualTo(compare.get(0));
        };
    }

    private Executable addCertificateFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(CertificateContract.contractName, CertificateContract.transactionNameAddCertificate, setup, input, ids);

            String result = contract.addCertificate(ctx, input.get(0), input.get(1));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable updateCertificateSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(CertificateContract.contractName, CertificateContract.transactionNameUpdateCertificate, setup, input, ids);

            assertThat(contract.updateCertificate(ctx, input.get(0), input.get(1)))
                    .isEqualTo(compare.get(0));
            assertThat(cUtil.getStringState(ctx.getStub(), input.get(0)))
                    .isEqualTo(compare.get(0));
        };

    }

    private Executable updateCertificateFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(CertificateContract.contractName, CertificateContract.transactionNameUpdateCertificate, setup, input, ids);

            String result = contract.updateCertificate(ctx, input.get(0), input.get(1));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }
}