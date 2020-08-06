package de.upb.cs.uc4.chaincode.mock;

import de.upb.cs.uc4.chaincode.GsonWrapper;
import de.upb.cs.uc4.chaincode.StudentChaincodeTest;
import de.upb.cs.uc4.chaincode.model.Student;
import org.hyperledger.fabric.shim.ledger.KeyValue;

import java.util.Objects;

final public class MockKeyValue implements KeyValue {

    private final String key;
    private final String value;

    public MockKeyValue(final String key, final String value) {
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