package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.mock.MockChaincodeStub;
import de.upb.cs.uc4.chaincode.model.Dummy;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;

import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtil {
    public static List<String> toStringList(List<Dummy> list) {
        return list.stream().map(Dummy::getContent).collect(Collectors.toList());
    }

    public static Context mockContext(List<String> setup, ContractUtil cUtil) {
        Context ctx = mock(Context.class);
        when(ctx.getStub()).thenReturn(mockStub(setup, cUtil));
        ClientIdentity testId = mock(ClientIdentity.class);
        when(testId.getMSPID()).thenReturn("testMspId");
        when(testId.getId()).thenReturn("testId");
        when(ctx.getClientIdentity()).thenReturn(testId);
        return ctx;
    }

    private static MockChaincodeStub mockStub(List<String> setup, ContractUtil cUtil) {
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
