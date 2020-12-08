package de.upb.cs.uc4.chaincode.mock;

import de.upb.cs.uc4.chaincode.ApprovalContract;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import de.upb.cs.uc4.chaincode.util.ApprovalContractUtil;
import de.upb.cs.uc4.chaincode.util.TestUtil;
import de.upb.cs.uc4.chaincode.util.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.protos.peer.ChaincodeEventPackage;
import org.hyperledger.fabric.protos.peer.ProposalPackage;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.shim.ledger.*;

import java.time.Instant;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class MockChaincodeStub implements ChaincodeStub {

    private final Map<String, List<MockKeyValue>> dataCollections;
    private final String defaultCollection = "default";
    private Map<String, byte[]> transientMap;
    private ApprovalList currentId;

    public void setCurrentId(ApprovalList currentId) {
        this.currentId = currentId;
    }

    public MockChaincodeStub() {
        dataCollections = new HashMap<>();
        dataCollections.put(defaultCollection, new ArrayList<>());
    }

    public void setTransient(Map<String, byte[]> transientMap) {
        this.transientMap = transientMap;
    }

    private void putByteState(String collection, String key, byte[] value) {
        if (!dataCollections.containsKey(collection)) {
            dataCollections.put(collection, new ArrayList<>());
        }
        MockKeyValue existing = null;
        for (MockKeyValue entry: dataCollections.get(collection)) {
            if (entry.getKey().equals(key))
                existing = entry;
        }
        if (existing != null)
            dataCollections.get(collection).remove(existing);
        dataCollections.get(collection).add(new MockKeyValue(key, value));
    }

    private void putByteState(String key, byte[] value) {
        putByteState(defaultCollection, key, value);
    }

    private byte[] getByteState(String collection, String key) {
        if (key == null || key.equals(""))
            throw new RuntimeException();
        if (!dataCollections.containsKey(collection))
            return new byte[0];
        for (MockKeyValue keyValue : dataCollections.get(collection)) {
            if (keyValue.getKey().equals(key))
                return keyValue.getValue();
        }
        return new byte[0];
    }

    private byte[] getByteState(String key) {
        return getByteState(defaultCollection, key);
    }

    private void delByteState(String collection, String key) {
        if (!dataCollections.containsKey(collection))
            return;
        for (MockKeyValue keyValue : dataCollections.get(collection)) {
            if (keyValue.getKey().equals(key)) {
                dataCollections.get(collection).remove(keyValue);
                return;
            }
        }
    }

    private void delByteState(String key) {
        delByteState(defaultCollection, key);
    }

    @Override
    public void putStringState(String key, String value) {
        putByteState(key, value.getBytes());
    }

    @Override
    public String getStringState(String key) {
        return new String(getByteState(key), UTF_8);
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
    public Chaincode.Response invokeChaincodeWithStringArgs(String chaincodeName, List<String> args) {
        String transactionName = args.get(0);
        switch (transactionName) {
            case "getApprovals":
                ApprovalContract contract = new ApprovalContract();
                ApprovalContractUtil cUtil = new ApprovalContractUtil();
                Context ctx = TestUtil.mockContext(this, currentId);
                String contractName = args.get(1);
                String transName = args.get(2);
                List<String> params = args.size() > 3 ? args.subList(3, args.size()) : new ArrayList<>();
                String result;
                try {
                    result = contract.getApprovals(ctx, contractName, transName, GsonWrapper.toJson(params));
                } catch (Exception e) {
                    return ResponseUtils.newErrorResponse();
                }
                return ResponseUtils.newSuccessResponse(result); // TODO: assert message is right (not payload?)
            default:
                throw new RuntimeException(transactionName + " did not match any callable transaction");
        }
    }

    @Override
    public byte[] getState(String key) {
        return getByteState(key);
    }

    @Override
    public byte[] getStateValidationParameter(String key) {
        return null;
    }

    @Override
    public void putState(String key, byte[] value) {
        putByteState(key, value);
    }

    @Override
    public void setStateValidationParameter(String key, byte[] value) {

    }

    @Override
    public void delState(String key) {
        delByteState(key);
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
    public MockQueryResultsIterator getStateByPartialCompositeKey(CompositeKey compositeKey) {
        PartialKeyIterator iterator = new PartialKeyIterator(dataCollections.get(defaultCollection).iterator(), compositeKey.toString());
        return new MockQueryResultsIterator(iterator);
    }

    @Override
    public QueryResultsIteratorWithMetadata<KeyValue> getStateByPartialCompositeKeyWithPagination(CompositeKey compositeKey, int pageSize, String bookmark) {
        return null;
    }

    @Override
    public CompositeKey createCompositeKey(String objectType, String... attributes) {
        return new MockCompositeKey(objectType, attributes);
    }

    @Override
    public CompositeKey splitCompositeKey(String compositeKey) {
        return MockCompositeKey.parseCompositeKey(compositeKey);
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
        return getByteState(collection, key);
    }

    @Override
    public byte[] getPrivateDataHash(String collection, String key) {
        return null;
    }

    @Override
    public byte[] getPrivateDataValidationParameter(String collection, String key) {
        return null;
    }

    @Override
    public void putPrivateData(String collection, String key, byte[] value) {
        putByteState(collection, key, value);
    }

    @Override
    public void setPrivateDataValidationParameter(String collection, String key, byte[] value) {

    }

    @Override
    public void delPrivateData(String collection, String key) {
        delByteState(collection, key);
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
        return null;
    }

    @Override
    public Map<String, byte[]> getTransient() {
        return transientMap;
    }

    @Override
    public byte[] getBinding() {
        return null;
    }

    @Override
    public String getMspId() {
        return null;
    }
}