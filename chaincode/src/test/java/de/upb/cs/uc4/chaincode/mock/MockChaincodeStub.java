package de.upb.cs.uc4.chaincode.mock;

import org.hyperledger.fabric.protos.peer.ChaincodeEventPackage;
import org.hyperledger.fabric.protos.peer.ProposalPackage;
import org.hyperledger.fabric.shim.Chaincode;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MockChaincodeStub implements ChaincodeStub {

    public List<MockKeyValue> putStates;
    private int index = 0;

    public MockChaincodeStub() {
        putStates = new ArrayList<>();
    }

    @Override
    public void putStringState(String key, String value) {
        putStates.add(index++, new MockKeyValue(key, value));
    }

    @Override
    public String getStringState(String key) {
        for (MockKeyValue keyValue : putStates) {
            if (keyValue.getKey().equals(key))
                return keyValue.getStringValue();
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
        for (MockKeyValue keyValue : putStates) {
            if (keyValue.getKey().equals(key)) {
                putStates.remove(keyValue);
                index--;
                break;
            }
        }
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