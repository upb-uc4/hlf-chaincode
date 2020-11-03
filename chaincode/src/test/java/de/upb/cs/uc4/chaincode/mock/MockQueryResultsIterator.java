package de.upb.cs.uc4.chaincode.mock;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.Iterator;

public class MockQueryResultsIterator implements QueryResultsIterator {
    private PartialKeyIterator iterator;

    public MockQueryResultsIterator(PartialKeyIterator iterator) {
        this.iterator = iterator;
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public PartialKeyIterator iterator() {
        return iterator;
    }
};
