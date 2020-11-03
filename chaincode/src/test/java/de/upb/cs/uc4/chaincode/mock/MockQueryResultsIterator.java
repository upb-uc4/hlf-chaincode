package de.upb.cs.uc4.chaincode.mock;
import com.sun.istack.internal.NotNull;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;


public class MockQueryResultsIterator implements QueryResultsIterator {
    private final PartialKeyIterator iterator;

    public MockQueryResultsIterator(PartialKeyIterator iterator) {
        this.iterator = iterator;
    }

    @Override
    public void close() {

    }

    @Override
    public PartialKeyIterator iterator() {
        return iterator;
    }
}
