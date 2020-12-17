package de.upb.cs.uc4.chaincode.contract.group;

import de.upb.cs.uc4.chaincode.contract.ContractBase;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.model.Group;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.List;

@Contract(
        name = "UC4.Group"
)
@Default
public class GroupContract extends ContractBase {
    private final GroupContractUtil cUtil = new GroupContractUtil();

    protected String contractName = "UC4.Group";

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
        try {
            cUtil.checkParamsAddUserToGroup(ctx, enrollmentId, groupId);
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

        // TODO re-enable approval validation
        /*if (!cUtil.validateApprovals(
                stub,
                this.contractName,
                "addUserToGroup",
                new ArrayList<String>() {{add(enrollmentId);add(groupId);}})) {
            return GsonWrapper.toJson(cUtil.getInsufficientApprovalsError());
        }*/

        cUtil.putAndGetStringState(stub, groupId, GsonWrapper.toJson(group));

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
        try {
            cUtil.checkParamsRemoveUserFromGroup(ctx, enrollmentId, groupId);
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

        // check approval
        // TODO re-enable approval validation
        /*if (!cUtil.validateApprovals(
                stub,
                this.contractName,
                "removeUserFromGroup",
                new ArrayList<String>() {{add(enrollmentId);add(groupId);}})) {
            return GsonWrapper.toJson(cUtil.getInsufficientApprovalsError());
        }*/

        cUtil.putAndGetStringState(stub, groupId, GsonWrapper.toJson(group));

        // success
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
        try {
            cUtil.checkParamsRemoveUserFromAllGroups(enrollmentId);
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        // check approval
        // TODO re-enable approval validation
        /*if (!cUtil.validateApprovals(
                stub,
                this.contractName,
                "removeUserFromAllGroups",
                Collections.singletonList(enrollmentId))) {
            return GsonWrapper.toJson(cUtil.getInsufficientApprovalsError());
        }*/

        cUtil.getGroupsForUser(stub, enrollmentId).forEach(item -> {
            item.getUserList().remove(enrollmentId);
            cUtil.putAndGetStringState(stub, item.getGroupId(), GsonWrapper.toJson(item));
        });

        // success
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
        ChaincodeStub stub = ctx.getStub();
        List<Group> groupList = cUtil.getAllGroups(stub);
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
        try {
            cUtil.checkParamsGetUsersForGroup(ctx, groupId);
        } catch (SerializableError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        List<String> userList;
        try {
            userList = cUtil.getUsersForGroup(stub, groupId);
        } catch (LedgerAccessError e) {
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
        try {
            cUtil.checkParamsGetGroupsForUser(enrollmentId);
        } catch (ParameterError e) {
            return e.getJsonError();
        }

        ChaincodeStub stub = ctx.getStub();
        List<String> groupIdList = cUtil.getGroupNamesForUser(stub, enrollmentId);

        return GsonWrapper.toJson(groupIdList);
    }
}
