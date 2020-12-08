package de.upb.cs.uc4.chaincode.mock;

import de.upb.cs.uc4.chaincode.util.helper.GsonWrapper;
import de.upb.cs.uc4.chaincode.model.MatriculationData;
import org.hyperledger.fabric.shim.ledger.KeyValue;

import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

final public class MockKeyValue implements KeyValue {

    private final String key;
    private final byte[] value;

    public MockKeyValue(final String key, final byte[] value) {
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
        return new String(this.value, UTF_8);
    }

    @Override
    public byte[] getValue() {
        return this.value;
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
        return Objects.equals(this.key, other.key) &&
                Objects.equals(
                        GsonWrapper.fromJson(new String(this.value, UTF_8), MatriculationData.class),
                        GsonWrapper.fromJson(new String(other.value, UTF_8), MatriculationData.class));
    }
}