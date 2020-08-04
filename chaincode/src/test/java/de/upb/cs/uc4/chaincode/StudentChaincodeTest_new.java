package de.upb.cs.uc4.chaincode;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.model.Dummy;
import de.upb.cs.uc4.chaincode.model.JsonIOTest;
import de.upb.cs.uc4.chaincode.model.Student;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.protos.peer.ChaincodeEventPackage;
import org.hyperledger.fabric.protos.peer.ProposalPackage;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.*;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class StudentChaincodeTest_new {

    private final class MockKeyValue implements KeyValue {

        private final String key;
        private final String value;

        MockKeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public byte[] getValue() {
            return this.value.getBytes();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MockKeyValue other = (MockKeyValue) o;
            GsonWrapper gson = new GsonWrapper();
            return Objects.equals(this.key, other.key) &&
                    Objects.equals(
                            gson.fromJson(this.value, Student.class),
                            gson.fromJson(other.value, Student.class));
        }
    }

    private final class MockStudentResultIterator implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> studentList;

        MockStudentResultIterator() {
            super();
            studentList = new ArrayList<>();
            studentList.add(new MockKeyValue("0000001",
                    "{\n" +
                            "  \"matriculationId\": \"0000001\",\n" +
                            "  \"firstName\": \"firstName1\",\n" +
                            "  \"lastName\": \"lastName1\",\n" +
                            "  \"birthDate\": \"2020-07-21\",\n" +
                            "  \"matriculationStatus\": [\n" +
                            "    {\n" +
                            "      \"fieldOfStudy\": \"Computer Science\",\n" +
                            "      \"intervals\": [\n" +
                            "        {\n" +
                            "          \"firstSemester\": \"WS2018\",\n" +
                            "          \"lastSemester\": \"SS2020\"\n" +
                            "        }\n" +
                            "      ]\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}"));
            studentList.add(new MockKeyValue("0000002",
                    "{\n" +
                            "  \"matriculationId\": \"0000002\",\n" +
                            "  \"firstName\": \"firstName2\",\n" +
                            "  \"lastName\": \"lastName2\",\n" +
                            "  \"birthDate\": \"2020-07-21\",\n" +
                            "  \"matriculationStatus\": [\n" +
                            "    {\n" +
                            "      \"fieldOfStudy\": \"Philosophy\",\n" +
                            "      \"intervals\": [\n" +
                            "        {\n" +
                            "          \"firstSemester\": \"SS214\",\n" +
                            "          \"lastSemester\": \"WS2015\"\n" +
                            "        },\n" +
                            "       {\n" +
                            "          \"firstSemester\": \"WS2018\",\n" +
                            "          \"lastSemester\": \"SS2020\"\n" +
                            "        }\n" +
                            "      ]\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}"));
            studentList.add(new MockKeyValue("0000003",
                    "{\n" +
                            "  \"matriculationId\": \"0000003\",\n" +
                            "  \"firstName\": \"firstName3\",\n" +
                            "  \"lastName\": \"lastName3\",\n" +
                            "  \"birthDate\": \"2020-07-21\",\n" +
                            "  \"matriculationStatus\": [\n" +
                            "    {\n" +
                            "      \"fieldOfStudy\": \"Economics\",\n" +
                            "      \"intervals\": [\n" +
                            "        {\n" +
                            "          \"firstSemester\": \"WS2018\",\n" +
                            "          \"lastSemester\": \"SS2020\"\n" +
                            "        }\n" +
                            "      ]\n" +
                            "    },\n" +
                            "    {\n" +
                            "      \"fieldOfStudy\": \"Physics\",\n" +
                            "      \"intervals\": [\n" +
                            "        {\n" +
                            "          \"firstSemester\": \"WS2018\",\n" +
                            "          \"lastSemester\": \"SS2020\"\n" +
                            "        }\n" +
                            "      ]\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}"));
        }

        @Override
        public void close() {

        }

        @Override
        public Iterator<KeyValue> iterator() {
            return studentList.iterator();
        }
    }

    private final class MockChaincodeStub implements ChaincodeStub {

        public List<MockKeyValue> putStates;
        private int index = 0;

        MockChaincodeStub() {
            putStates = new ArrayList<>();
        }

        @Override
        public void putStringState(String key, String value) {
            putStates.add(index++, new MockKeyValue(key, value));
        }

        @Override
        public String getStringState(String key) {
            for (MockKeyValue keyValue : putStates) {
                if (keyValue.key.equals(key))
                    return keyValue.value;
            }
            return "";
        }

        @Override
        public List<byte[]> getArgs() {
            return null;
        }

        @Override
        public List<String> getStringArgs() {
            return null;
        }

        @Override
        public String getFunction() {
            return null;
        }

        @Override
        public List<String> getParameters() {
            return null;
        }

        @Override
        public String getTxId() {
            return null;
        }

        @Override
        public String getChannelId() {
            return null;
        }

        @Override
        public Chaincode.Response invokeChaincode(String chaincodeName, List<byte[]> args, String channel) {
            return null;
        }

        @Override
        public byte[] getState(String key) {
            return new byte[0];
        }

        @Override
        public byte[] getStateValidationParameter(String key) {
            return new byte[0];
        }

        @Override
        public void putState(String key, byte[] value) {

        }

        @Override
        public void setStateValidationParameter(String key, byte[] value) {

        }

        @Override
        public void delState(String key) {

        }

        @Override
        public QueryResultsIterator<KeyValue> getStateByRange(String startKey, String endKey) {
            return null;
        }

        @Override
        public QueryResultsIteratorWithMetadata<KeyValue> getStateByRangeWithPagination(String startKey, String endKey, int pageSize, String bookmark) {
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String compositeKey) {
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(String objectType, String... attributes) {
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getStateByPartialCompositeKey(CompositeKey compositeKey) {
            return null;
        }

        @Override
        public QueryResultsIteratorWithMetadata<KeyValue> getStateByPartialCompositeKeyWithPagination(CompositeKey compositeKey, int pageSize, String bookmark) {
            return null;
        }

        @Override
        public CompositeKey createCompositeKey(String objectType, String... attributes) {
            return null;
        }

        @Override
        public CompositeKey splitCompositeKey(String compositeKey) {
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getQueryResult(String query) {
            return null;
        }

        @Override
        public QueryResultsIteratorWithMetadata<KeyValue> getQueryResultWithPagination(String query, int pageSize, String bookmark) {
            return null;
        }

        @Override
        public QueryResultsIterator<KeyModification> getHistoryForKey(String key) {
            return null;
        }

        @Override
        public byte[] getPrivateData(String collection, String key) {
            return new byte[0];
        }

        @Override
        public byte[] getPrivateDataHash(String collection, String key) {
            return new byte[0];
        }

        @Override
        public byte[] getPrivateDataValidationParameter(String collection, String key) {
            return new byte[0];
        }

        @Override
        public void putPrivateData(String collection, String key, byte[] value) {

        }

        @Override
        public void setPrivateDataValidationParameter(String collection, String key, byte[] value) {

        }

        @Override
        public void delPrivateData(String collection, String key) {

        }

        @Override
        public QueryResultsIterator<KeyValue> getPrivateDataByRange(String collection, String startKey, String endKey) {
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, String compositeKey) {
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, CompositeKey compositeKey) {
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getPrivateDataByPartialCompositeKey(String collection, String objectType, String... attributes) {
            return null;
        }

        @Override
        public QueryResultsIterator<KeyValue> getPrivateDataQueryResult(String collection, String query) {
            return null;
        }

        @Override
        public void setEvent(String name, byte[] payload) {

        }

        @Override
        public ChaincodeEventPackage.ChaincodeEvent getEvent() {
            return null;
        }

        @Override
        public ProposalPackage.SignedProposal getSignedProposal() {
            return null;
        }

        @Override
        public Instant getTxTimestamp() {
            return null;
        }

        @Override
        public byte[] getCreator() {
            return new byte[0];
        }

        @Override
        public Map<String, byte[]> getTransient() {
            return null;
        }

        @Override
        public byte[] getBinding() {
            return new byte[0];
        }

        @Override
        public String getMspId() {
            return null;
        }
    }

    @TestFactory
    List<DynamicTest> createTests() {
        File dir = new File("test_configs");
        File[] testConfigs = dir.listFiles();

        GsonWrapper gson = new GsonWrapper();
        List<JsonIOTest> testConfig;
        Type type = new TypeToken<List<JsonIOTest>>() {
        }.getType();
        ArrayList<DynamicTest> tests = new ArrayList<>();

        if (testConfigs == null) {
            throw new RuntimeException("No test configurations found.");
        }

        for (File file: testConfigs) {
            try {
                testConfig = gson.fromJson(
                        new FileReader(file.getPath()),
                        type);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            for (JsonIOTest test : testConfig) {
                switch (test.getType()) {
                    case "getMatriculationData":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                getMatriculationDataTest(test.getSetup(), test.getInput(), test.getCompare())
                        ));
                        break;
                    case "addMatriculationData_SUCCESS":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                addMatriculationDataSuccessTest(test.getSetup(), test.getInput(), test.getCompare())
                        ));
                        break;
                    case "addMatriculationData_FAILURE":
                        tests.add(DynamicTest.dynamicTest(
                                test.getName(),
                                addMatriculationDataFailureTest(test.getSetup(), test.getInput(), test.getCompare())
                        ));
                        break;
                }
            }
        }
        return tests;
    }

    static Executable getMatriculationDataTest(
            List<Dummy> setup,
            List<Dummy> input,
            List<Dummy> compare
    ) {
        return () -> {
            StudentChaincode contract = new StudentChaincode();
            GsonWrapper gson = new GsonWrapper();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStringState(setup.get(0).getContent()))
                    .thenReturn(setup.get(1).getContent());
            Student student = gson.fromJson(
                    contract.getMatriculationData(ctx, input.get(0).getContent()),
                    Student.class);
            assertThat(student).isEqualTo(gson.fromJson(
                    compare.get(0).getContent(),
                    Student.class
            ));
        };
    }

    private Executable addMatriculationDataSuccessTest(
            List<Dummy> setup,
            List<Dummy> input,
            List<Dummy> compare
    ) {
        return () -> {
            StudentChaincode contract = new StudentChaincode();
            GsonWrapper gson = new GsonWrapper();
            Context ctx = mock(Context.class);
            MockChaincodeStub stub = new MockChaincodeStub();
            when(ctx.getStub()).thenReturn(stub);
            if (!setup.isEmpty()) {
                when(stub.getStringState(setup.get(0).getContent()))
                        .thenReturn(setup.get(1).getContent());
            }
            contract.addMatriculationData(ctx, input.get(0).getContent());
            Student student = gson.fromJson(compare.get(0).getContent(), Student.class);
            assertThat(stub.putStates.get(0)).isEqualTo(new StudentChaincodeTest_new.MockKeyValue(
                    student.getMatriculationId(),
                    compare.get(0).getContent()));

        };
    }

    private Executable addMatriculationDataFailureTest(
            List<Dummy> setup,
            List<Dummy> input,
            List<Dummy> compare
    ) {
        return () -> {
            StudentChaincode contract = new StudentChaincode();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            if (!setup.isEmpty()) {
                when(stub.getStringState(setup.get(0).getContent()))
                        .thenReturn(setup.get(1).getContent());
            }
            String result = contract.addMatriculationData(ctx, input.get(0).getContent());
            assertThat(result).isEqualTo(compare.get(0).getContent());
        };
    }
}