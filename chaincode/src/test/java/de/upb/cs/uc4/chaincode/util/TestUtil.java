package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.model.Dummy;

import java.util.List;
import java.util.stream.Collectors;

public class TestUtil {
    public static List<String> toStringList(List<Dummy> list) {
        return list.stream().map(dummy -> dummy.getContent()).collect(Collectors.toList());
    }
}
