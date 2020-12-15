package de.upb.cs.uc4.chaincode.util.helper;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.model.ApprovalList;
import de.upb.cs.uc4.chaincode.model.Dummy;
import de.upb.cs.uc4.chaincode.model.MatriculationData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AccessManager {
    static final String ADMIN = "admin";

    public static ApprovalList getRequiredApprovals(String contractName, String transactionName, String params) {
        Type listType = new TypeToken<ArrayList<Dummy>>() {
        }.getType();
        List<Dummy> paramList = GsonWrapper.fromJson(params, listType);
        switch (contractName) {
            case "UC4.MatriculationData":
                switch (transactionName) {
                    case "addMatriculationData":
                        return getRequiredApprovalsForAddMatriculationData(paramList);
                    default:
                        // TODO throw exception
                }
                break;
            default:
                // TODO throw exception
        }
        return null;
    }

    private static ApprovalList getRequiredApprovalsForAddMatriculationData(List<Dummy> params) {
        MatriculationData matriculationData = GsonWrapper.fromJson(params.get(0).getContent(), MatriculationData.class);
        return new ApprovalList()
                .addUsersItem(matriculationData.getEnrollmentId())
                .addGroupsItem(ADMIN);
    }
}
