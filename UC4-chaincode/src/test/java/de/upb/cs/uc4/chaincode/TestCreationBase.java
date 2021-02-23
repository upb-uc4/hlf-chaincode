package de.upb.cs.uc4.chaincode;


import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TestCreationBase {

    abstract DynamicTest CreateTest(JsonIOTest test);

    abstract String getTestConfigDir();

    @TestFactory
    List<DynamicTest> createTests() {
        String testConfigDir = getTestConfigDir();
        File dir = new File(testConfigDir);
        File[] testConfigs = dir.listFiles();

        List<JsonIOTest> testConfig;
        List<DynamicTest> tests = new ArrayList<>();

        if (testConfigs == null) {
            throw new RuntimeException("No test configurations found.");
        }

        for (File file : testConfigs) {
            testConfig = Arrays.asList(GsonWrapper.fromJson(Files.contentOf(file, Charset.defaultCharset()), JsonIOTest[].class));

            for (JsonIOTest test : testConfig) {
                test.setIds(test.getIds().stream().map(TestUtil::wrapEnrollmentId).collect(Collectors.toList()));
                tests.add(CreateTest(test));
            }
        }
        return tests;
    }
}
