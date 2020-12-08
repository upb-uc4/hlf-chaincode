package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.exceptions.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.util.AdmissionContractUtil;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.*;
import java.util.stream.Collectors;

@Contract(
        name="UC4.Group"
)
@Default
public class GroupContract extends ContractBase {
    private final GroupContractUtil cUtil = new GroupContractUtil();

    protected String contractName = "UC4.Group";
<
    /**
     * Adds user to group.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId
     * @param groupId
     * @return userList on success, including the newly added user
     */
    @Transaction()
    public List<String> addUserToGroup(final Context ctx, String enrollmentId, String groupId) {
        ChaincodeStub stub = ctx.getStub();

        Group group;
        List<String> userList;
        try {
            userList = GsonWrapper.fromJson(groupJson, Group.class);
        } catch (Exception e) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableParam("group")));
        }

        if (cUtil.keyExists(stub, userList.getUserId())) {
            return GsonWrapper.toJson(cUtil.getConflictError());
        }

        ArrayList<InvalidParameter> invalidParams = cUtil.getParameterErrorsForGroup(group);
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        if (cUtil.keyExists(stub, group.getGroupId())) {
            return GsonWrapper.toJson(cUtil.getConflictError());
        }

        // check for semantic errors
        ArrayList<InvalidParameter> invalidParameters = cUtil.getSemanticErrorsForGroup(stub, group);
        if (!invalidParameters.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getInvalidActionError(invalidParameters));
        }

        List<String> requiredIds = Collections.singletonList(enrollmentId);
        List<String> requiredTypes = Collections.singletonList("admin");

        // TODO: approval Check!! I DO NOT KNOW IF I CAN JUST BUILD THE PARAMETER LIST LIKE THAT
        if (!cUtil.validateApprovals(
                ctx,
                requiredIds,
                requiredTypes,
                this.contractName,
                "addUserToGroup",
                Arrays.stream(new String[]{groupJson}).collect(Collectors.toList()))) {
            return GsonWrapper.toJson(cUtil.getInsufficientApprovalsError());
        }

        return userList;
    }

    /**
     * Removes a user from a group
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId identifier of user to remove
     * @param groupId identifier of a group to remove user from
     * @return userList list of users left in the group
     */
    @Transaction()
    public List<String> removeUserFromGroup(final Context ctx, String enrollmentId, String groupId) {
        ChaincodeStub stub = ctx.getStub();

        List<String> userList;

        // check empty
        Group group;
        try {
            group = cUtil.<Group>getState(stub, groupId, Group.class);
        } catch(LedgerAccessError e) {
            return e.getJsonError();
        }

        // check approval
        List<String> requiredIds = Collections.singletonList(enrollmentId);
        List<String> requiredTypes = Collections.singletonList("admin");
        if (!cUtil.validateApprovals(
                ctx,
                requiredIds,
                requiredTypes,
                this.contractName,
                "removeUserFromGroup",
                Collections.singletonList(groupId))) {
            return GsonWrapper.toJson(cUtil.getInsufficientApprovalsError());
        }

        // perform delete
        try {
            cUtil.delState(stub, enrollmentId);
        } catch(LedgerAccessError e) {
            return e.getJsonError();
        }

        // success
        return userList;
    }

    /**
     * Removes a user from all groups
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId identifier of user to remove
     * @return empty string on success, serialized error on failure
     */
    @Transaction()
    public String removeUserFromAllGroups(final Context ctx, String enrollmentId) {
        ChaincodeStub stub = ctx.getStub();

        List<Group> groupList;

        // check empty
        Group group;
        try {
            group = cUtil.<Group>getState(stub, groupId, Group.class);
        } catch(LedgerAccessError e) {
            return e.getJsonError();
        }

        // check approval
        List<String> requiredIds = Collections.singletonList(enrollmentId);
        List<String> requiredTypes = Collections.singletonList("admin");
        if (!cUtil.validateApprovals(
                ctx,
                requiredIds,
                requiredTypes,
                this.contractName,
                "removeUserFromAllGroups",
                Collections.singletonList(groupId))) {
            return GsonWrapper.toJson(cUtil.getInsufficientApprovalsError());
        }

        // perform delete
        // do I need to traverse groupList?
        try {
            cUtil.delState(stub, enrollmentId);
        } catch(LedgerAccessError e) {
            return e.getJsonError();
        }

        // success
        return "";
    }

    /**
     * Gets GroupList from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return Serialized List of Matching Groups on success, serialized error on failure
     */
    @Transaction()
    public List<Group> getAllGroups(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Group> groupList = cUtil.getGroups(stub);
        groupList.add(this.getAllStates(stub, Group.class).stream())

        return GsonWrapper.toJson(groupList);
    }

    /**
     * Gets GroupList from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return Serialized List of Matching Groups on success, serialized error on failure
     */
    @Transaction()
    public List<String> getUsersForGroup(final Context ctx), String groupId) {
        ChaincodeStub stub = ctx.getStub();

        List<String> userList;

        userList.add(this.getAllStates(stub, Group.class).stream()
                .filter(item -> groupId.isEmpty() || item.getGroupId().equals(groupId))

        return userList;
    }

    /**
     * Gets GroupList for a specific user from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId
     * @return Serialized List of Matching Groups on success, serialized error on failure
     */
    @Transaction()
    public List<String> getGroupsForUser(final Context ctx), String enrollmentId) {
        ChaincodeStub stub = ctx.getStub();

        List<String> groupList;

        // TODO
        groupList.add(this.getAllGroups()
                .filter(item -> enrollmentId.isEmpty() || item.getEnrollmentId().equals(entollmentId))


        return groupList;
    }
}
