package de.upb.cs.uc4.chaincode.contract.group;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.helper.HyperledgerManager;
import de.upb.cs.uc4.chaincode.model.Group;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Contract(
        name = GroupContract.contractName
)
public class GroupContract extends ContractBase {
    private final GroupContractUtil cUtil = new GroupContractUtil();

    public final static String contractName = "UC4.Group";

    /**
     * Adds user to group.
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId to add to group
     * @param groupId      groupId to which the user is added
     * @return userList on success, including the newly added user
     */
    @Transaction()
    public String addUserToGroup(final Context ctx, String enrollmentId, String groupId) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        try {
            cUtil.checkParamsAddUserToGroup(ctx, new ArrayList<String>(){{add(enrollmentId); add(groupId);}});
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        Group group;
        try {
            group = cUtil.getState(stub, groupId, Group.class);
        } catch (LedgerAccessError e) {
            group = new Group();
            group.setGroupId(groupId);
        }

        if (!(group.getUserList().contains(enrollmentId))) {
            group.getUserList().add(enrollmentId);
        }

        try {
            cUtil.validateApprovals(ctx, contractName, transactionName, new String[]{enrollmentId, groupId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        cUtil.putAndGetStringState(stub, groupId, GsonWrapper.toJson(group));
        try {
            cUtil.finishOperation(stub, contractName, transactionName, new String[]{enrollmentId, groupId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return "";
    }

    /**
     * Removes a user from a group
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId identifier of user to remove
     * @param groupId      identifier of a group to remove user from
     * @return userList list of users left in the group
     */
    @Transaction()
    public String removeUserFromGroup(final Context ctx, String enrollmentId, String groupId) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        try {
            cUtil.checkParamsRemoveUserFromGroup(ctx, new ArrayList<String>(){{add(enrollmentId); add(groupId);}});
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        Group group;
        try {
            group = cUtil.getState(stub, groupId, Group.class);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }
        group.getUserList().remove(enrollmentId);

        try {
            cUtil.validateApprovals(ctx, contractName, transactionName, new String[]{enrollmentId, groupId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        cUtil.putAndGetStringState(stub, groupId, GsonWrapper.toJson(group));
        try {
            cUtil.finishOperation(stub, contractName, transactionName, new String[]{enrollmentId, groupId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return "";
    }

    /**
     * Removes a user from all groups
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId identifier of user to remove
     * @return empty string on success, serialized error on failure
     */
    @Transaction()
    public String removeUserFromAllGroups(final Context ctx, String enrollmentId) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        try {
            cUtil.checkParamsRemoveUserFromAllGroups(Collections.singletonList(enrollmentId));
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.validateApprovals(ctx, contractName, transactionName, new String[]{enrollmentId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        cUtil.getGroupsForUser(stub, enrollmentId).forEach(group -> {
            group.getUserList().remove(enrollmentId);
            cUtil.putAndGetStringState(stub, group.getGroupId(), GsonWrapper.toJson(group));
        });
        try {
            cUtil.finishOperation(stub, contractName, transactionName, new String[]{enrollmentId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return "";
    }

    /**
     * Gets GroupList from the ledger.
     *
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return Serialized List of Matching Groups on success, serialized error on failure
     */
    @Transaction()
    public String getAllGroups(final Context ctx) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.validateApprovals(ctx, contractName, transactionName, new String[]{});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        List<Group> groupList = cUtil.getAllGroups(stub);
        try {
            cUtil.finishOperation(stub, contractName, transactionName, new String[]{});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return GsonWrapper.toJson(groupList);
    }

    /**
     * Gets GroupList from the ledger.
     *
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return Serialized List of Matching Groups on success, serialized error on failure
     */
    @Transaction()
    public String getUsersForGroup(final Context ctx, String groupId) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        try {
            cUtil.checkParamsGetUsersForGroup(ctx, Collections.singletonList(groupId));
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.validateApprovals(ctx, contractName, transactionName, new String[]{groupId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        List<String> userList;
        try {
            userList = cUtil.getUsersForGroup(stub, groupId);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }
        try {
            cUtil.finishOperation(stub, contractName, transactionName, new String[]{groupId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return GsonWrapper.toJson(userList);
    }

    /**
     * Gets GroupList for a specific user from the ledger.
     *
     * @param ctx          transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId to filter groups for
     * @return Serialized List of Matching Groups on success, serialized error on failure
     */
    @Transaction()
    public String getGroupsForUser(final Context ctx, String enrollmentId) {
        String transactionName = HyperledgerManager.getTransactionName(ctx.getStub());
        try {
            cUtil.checkParamsGetGroupsForUser(Collections.singletonList(enrollmentId));
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        try {
            cUtil.validateApprovals(ctx, contractName, transactionName, new String[]{enrollmentId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        List<String> groupIdList = cUtil.getGroupNamesForUser(stub, enrollmentId);
        try {
            cUtil.finishOperation(stub, contractName, transactionName, new String[]{enrollmentId});
        } catch (SerializableError e) {
            return e.getJsonError();
        }
        return GsonWrapper.toJson(groupIdList);
    }
}
