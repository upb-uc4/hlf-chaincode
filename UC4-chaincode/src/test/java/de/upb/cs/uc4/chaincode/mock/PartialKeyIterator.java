package de.upb.cs.uc4.chaincode.mock;
import org.hyperledger.fabric.shim.ledger.KeyValue;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PartialKeyIterator implements Iterator<KeyValue> {
    private final Iterator<MockKeyValue> iterator;
    private final String partialKey;
    private KeyValue lastReturned;
    private KeyValue nextReturn;

    public PartialKeyIterator(Iterator<MockKeyValue> iterator, String partialKey) {
        this.iterator = iterator;
        this.partialKey = partialKey;
        this.nextReturn = null;
        this.lastReturned = null;
    }

    @Override
    public boolean hasNext() {
        if (!iterator.hasNext()) {
            return false;
        }
        if (nextReturn == lastReturned) {
            nextReturn = iterator.next();
            while (!nextReturn.getKey().startsWith(partialKey)) {
                if (iterator.hasNext()) {
                    nextReturn = iterator.next();
                } else {
                    return false;
                }
            }
            return true;
        } else {
            return nextReturn.getKey().startsWith(partialKey);
        }
    }

    @Override
    public KeyValue next() {
        if (nextReturn == lastReturned) {
            nextReturn = iterator.next();
            while (!nextReturn.getKey().startsWith(partialKey)) {
                if (iterator.hasNext()) {
                    nextReturn = iterator.next();
                } else {
                    throw new NoSuchElementException();
                }
            }
        }
        lastReturned = nextReturn;
        return nextReturn;
    }
}
