package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContract;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.contract.admission.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.admission.CourseAdmission;
import de.upb.cs.uc4.chaincode.model.admission.ExamAdmission;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import org.hyperledger.fabric.contract.Context;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public final class AdmissionContractTest extends TestCreationBase {

    private final AdmissionContract contract = new AdmissionContract();
    private final AdmissionContractUtil cUtil = new AdmissionContractUtil();

    String getTestConfigDir() {
        return "src/test/resources/test_configs/admission_contract";
    }

    DynamicTest CreateTest(JsonIOTest test) {
        String testType = test.getType();
        String testName = test.getName();
        JsonIOTestSetup setup = test.getSetup();
        List<String> input = TestUtil.toStringList(test.getInput());
        List<String> compare = TestUtil.toStringList(test.getCompare());
        List<String> ids = test.getIds();

        switch (testType) {
            case "addAdmission_SUCCESS":
                return DynamicTest.dynamicTest(testName, addAdmissionSuccessTest(setup, input, compare, ids));
            case "addAdmission_FAILURE":
                return DynamicTest.dynamicTest(testName, addAdmissionFailureTest(setup, input, compare, ids));
            case "dropAdmission_SUCCESS":
                return DynamicTest.dynamicTest(testName, dropAdmissionSuccessTest(setup, input, compare, ids));
            case "dropAdmission_FAILURE":
                return DynamicTest.dynamicTest(testName, dropAdmissionFailureTest(setup, input, compare, ids));
            case "getCourseAdmissions_SUCCESS":
                return DynamicTest.dynamicTest(testName, getCourseAdmissionsSuccessTest(setup, input, compare, ids));
            case "getExamAdmissions_SUCCESS":
                return DynamicTest.dynamicTest(testName, getExamAdmissionsSuccessTest(setup, input, compare, ids));
            default:
                throw new RuntimeException("Test " + testName + " of type " + testType + " could not be matched.");
        }
    }

    private Executable addAdmissionSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(AdmissionContract.contractName, AdmissionContract.transactionNameAddAdmission, setup, input, ids);

            String addResult = contract.addAdmission(ctx, input.get(0));

            CourseAdmission compareAdmission = GsonWrapper.fromJson(compare.get(0), CourseAdmission.class);
            CourseAdmission returnAdmission = GsonWrapper.fromJson(addResult, CourseAdmission.class);
            CourseAdmission ledgerAdmission = cUtil.getState(ctx.getStub(), compareAdmission.getAdmissionId(), CourseAdmission.class);
            for(CourseAdmission testAdmission : new CourseAdmission[]{returnAdmission, ledgerAdmission}){
                assertThat(testAdmission.getCourseId()).isEqualTo(compareAdmission.getCourseId());
                assertThat(testAdmission.getModuleId()).isEqualTo(compareAdmission.getModuleId());
                assertThat(testAdmission.getAdmissionId()).isEqualTo(compareAdmission.getAdmissionId());
                assertThat(testAdmission.getEnrollmentId()).isEqualTo(compareAdmission.getEnrollmentId());
                assertThat(testAdmission.getType()).isEqualTo(compareAdmission.getType());
            }
        };
    }

    private Executable addAdmissionFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(AdmissionContract.contractName, AdmissionContract.transactionNameAddAdmission, setup, input, ids);
            String result = contract.addAdmission(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable dropAdmissionSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(AdmissionContract.contractName, AdmissionContract.transactionNameDropAdmission, setup, input, ids);

            String dropResult = contract.dropAdmission(ctx, input.get(0));

            assertThat(dropResult).isEqualTo(compare.get(0));
            List<CourseAdmission> ledgerState = cUtil.getAllStates(ctx.getStub(), CourseAdmission.class);
            assertThat(ledgerState).allMatch(item -> !(item.getAdmissionId().equals(input.get(0))));
        };
    }

    private Executable dropAdmissionFailureTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(AdmissionContract.contractName, AdmissionContract.transactionNameDropAdmission, setup, input, ids);

            String result = contract.dropAdmission(ctx, input.get(0));
            assertThat(result).isEqualTo(compare.get(0));
        };
    }

    private Executable getCourseAdmissionsSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(AdmissionContract.contractName, AdmissionContract.transactionNameGetCourseAdmissions, setup, input, ids);

            String getResult = contract.getCourseAdmissions(ctx, input.get(0), input.get(1), input.get(2));

            // compare
            List<CourseAdmission> courseAdmissions = Arrays.asList(GsonWrapper.fromJson(getResult, CourseAdmission[].class).clone());
            List<CourseAdmission> compareAdmissions = Arrays.asList(GsonWrapper.fromJson(compare.get(0), CourseAdmission[].class).clone());
            assertThat(courseAdmissions).isEqualTo(compareAdmissions);
            assertThat(courseAdmissions.toString()).isEqualTo(compareAdmissions.toString());
            assertThat(getResult).isEqualTo(compare.get(0));
        };
    }

    private Executable getExamAdmissionsSuccessTest(
            JsonIOTestSetup setup,
            List<String> input,
            List<String> compare,
            List<String> ids
    ) {
        return () -> {
            Context ctx = TestUtil.buildContext(AdmissionContract.contractName, AdmissionContract.transactionNameGetExamAdmissions, setup, input, ids);

            String getResult = contract.getExamAdmissions(ctx, input.get(0), input.get(1), input.get(2));

            // compare
            List<ExamAdmission> examAdmissions = Arrays.asList(GsonWrapper.fromJson(getResult, ExamAdmission[].class).clone());
            List<ExamAdmission> compareAdmissions = Arrays.asList(GsonWrapper.fromJson(compare.get(0), ExamAdmission[].class).clone());
            assertThat(examAdmissions).isEqualTo(compareAdmissions);
            assertThat(examAdmissions.toString()).isEqualTo(compareAdmissions.toString());
            assertThat(getResult).isEqualTo(compare.get(0));
        };
    }
}
