package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import de.upb.cs.uc4.chaincode.model.Dummy;
import de.upb.cs.uc4.chaincode.model.JsonIOTestSetup;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtil {
    public static List<String> toStringList(List<Dummy> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream().map(Dummy::getContent).collect(Collectors.toList());
    }

    public static Context mockContext(MockChaincodeStub stub) {
        ApprovalList id = new ApprovalList().id("testId").type("admin");
        return mockContext(stub, id);
    }

    public static Context mockContext(MockChaincodeStub stub, ApprovalList id) {
        Context ctx = mock(Context.class);
        when(ctx.getStub()).thenReturn(stub);
        stub.setCurrentId(id);
        ClientIdentity testId = mock(ClientIdentity.class);
        when(testId.getId()).thenReturn(id.getId());
        when(testId.getAttributeValue("hf.Type")).thenReturn(id.getType());
        when(ctx.getClientIdentity()).thenReturn(testId);
        return ctx;
    }

    public static MockChaincodeStub mockStub(JsonIOTestSetup setup) {
        MockChaincodeStub stub = new MockChaincodeStub();
        setup.prepareStub(stub);
        return stub;
    }

    public void setTransientMap(MockChaincodeStub stub, List<Dummy> input) {
        stub.setTransient(
                input.stream().collect(
                        Collectors.toMap(
                                entry -> String.valueOf(input.indexOf(entry)),
                                entry -> entry.getContent().getBytes())));
    }
}
