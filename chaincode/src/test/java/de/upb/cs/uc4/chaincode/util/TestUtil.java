package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.Approval;
import de.upb.cs.uc4.chaincode.model.Dummy;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtil {
    public static List<String> toStringList(List<Dummy> list) {
        return list.stream().map(Dummy::getContent).collect(Collectors.toList());
    }

    public static Context mockContext(MockChaincodeStub stub) {
        Approval id = new Approval().id("testId").type("admin");
        return mockContext(stub, id);
    }

    public static Context mockContext(MockChaincodeStub stub, Approval id) {
        Context ctx = mock(Context.class);
        when(ctx.getStub()).thenReturn(stub);
        stub.setCurrentId(id);
        ClientIdentity testId = mock(ClientIdentity.class);
        when(testId.getId()).thenReturn(id.getId());
        when(testId.getAttributeValue("hf.Type")).thenReturn(id.getType());
        when(ctx.getClientIdentity()).thenReturn(testId);
        return ctx;
    }

    public static MockChaincodeStub mockStub(List<String> setup, ContractUtil cUtil) {
        MockChaincodeStub stub = new MockChaincodeStub();
        for (int i=0; i<setup.size(); i+=2) {
            cUtil.putAndGetStringState(stub, setup.get(i), setup.get(i+1));
        }
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
