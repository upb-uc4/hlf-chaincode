package de.upb.cs.uc4.chaincode.mock;

import org.hyperledger.fabric.shim.ledger.CompositeKey;

import java.util.Arrays;

public class MockCompositeKey extends CompositeKey {
    private final String compositeKey;
    private final static String DELIMITER = ":::";
    public MockCompositeKey(String objectType, String... attributes) {
        super(objectType, attributes);
        if (attributes.length == 0) {
            compositeKey = objectType + DELIMITER;
        } else if (attributes.length == 1) {
            compositeKey = objectType + DELIMITER + attributes[0] + DELIMITER;
        } else {
            compositeKey = objectType + DELIMITER + Arrays.stream(attributes).reduce((a, b) -> a + DELIMITER + b).get();
        }
    }

    @Override
    public String toString() {
        return compositeKey;
    }

    public static MockCompositeKey parseCompositeKey(final String compositeKey) {
        String[] keyParts = compositeKey.split(DELIMITER);
        return new MockCompositeKey(keyParts[0], Arrays.copyOfRange(keyParts,1, keyParts.length));
    }
}
