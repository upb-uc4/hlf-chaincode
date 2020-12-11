package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.exceptions.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.util.GroupContractUtil;
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

    /**
     * Adds user to group.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId to add to group
     * @param groupId groupId to which the user is added
     * @return userList on success, including the newly added user
     */
    @Transaction()
    public String addUserToGroup(final Context ctx, String enrollmentId, String groupId) {
        ChaincodeStub stub = ctx.getStub();

        ArrayList<InvalidParameter> invalidParams = cUtil.getParameterErrorsForEnrollmentId(enrollmentId);
        invalidParams.addAll(cUtil.getParameterErrorsForGroupId(groupId));
        invalidParams.addAll(cUtil.getSemanticErrorsForUserInGroup(stub, enrollmentId));
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        Group group;
        try {
            group = cUtil.getState(stub, groupId, Group.class);
        } catch (LedgerAccessError e) {
            group = new Group();
            group.setGroupId(groupId);
        }

        if(!(group.getUserList().contains(enrollmentId))){
            group.getUserList().add(enrollmentId);
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
                Arrays.stream(new String[]{enrollmentId, groupId}).collect(Collectors.toList()))) {
            return GsonWrapper.toJson(cUtil.getInsufficientApprovalsError());
        }

        cUtil.putAndGetStringState(stub, groupId, GsonWrapper.toJson(group));

        return "";
    }

    /**
     * Removes a user from a group
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId identifier of user to remove
     * @param groupId identifier of a group to remove user from
     * @return userList list of users left in the group
     */
    @Transaction()
    public String removeUserFromGroup(final Context ctx, String enrollmentId, String groupId) {
        ChaincodeStub stub = ctx.getStub();

        ArrayList<InvalidParameter> invalidParams = cUtil.getParameterErrorsForEnrollmentId(enrollmentId);
        invalidParams.addAll(cUtil.getParameterErrorsForGroupId(groupId));
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        Group group;
        try {
            group = cUtil.getState(stub, groupId, Group.class);
        } catch (LedgerAccessError e) {
            return e.getJsonError();
        }

        if(!(group.getUserList().contains(enrollmentId))){
            return GsonWrapper.toJson(cUtil.getUserNoRemoveError(enrollmentId, groupId));
        }
            group.getUserList().remove(enrollmentId);

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

        cUtil.putAndGetStringState(stub, groupId, GsonWrapper.toJson(group));

        // success
        return "";
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

        ArrayList<InvalidParameter> invalidParams = cUtil.getParameterErrorsForEnrollmentId(enrollmentId);
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
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
                Collections.singletonList(enrollmentId))) {
            return GsonWrapper.toJson(cUtil.getInsufficientApprovalsError());
        }

        cUtil.getGroupsForUser(stub, enrollmentId).forEach(item ->{
            item.getUserList().remove(enrollmentId);
            cUtil.putAndGetStringState(stub, item.getGroupId(),GsonWrapper.toJson(item));
        });

        // success
        return "";
    }

    /**
     * Gets GroupList from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return Serialized List of Matching Groups on success, serialized error on failure
     */
    @Transaction()
    public String getAllGroups(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();


        List<Group> groupList = cUtil.getAllGroups(stub);

        return GsonWrapper.toJson(groupList );
    }

    /**
     * Gets GroupList from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @return Serialized List of Matching Groups on success, serialized error on failure
     */
    @Transaction()
    public String getUsersForGroup(final Context ctx, String groupId) {
        ChaincodeStub stub = ctx.getStub();

        ArrayList<InvalidParameter> invalidParams = cUtil.getParameterErrorsForGroupId(groupId);
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

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
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId to filter groups for
     * @return Serialized List of Matching Groups on success, serialized error on failure
     */
    @Transaction()
    public String getGroupsForUser(final Context ctx, String enrollmentId) {
        ChaincodeStub stub = ctx.getStub();

        ArrayList<InvalidParameter> invalidParams = cUtil.getParameterErrorsForEnrollmentId(enrollmentId);
        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        List<String> groupIdList = cUtil.getGroupNamesForUser(stub, enrollmentId);

        return GsonWrapper.toJson(groupIdList);
    }
}
