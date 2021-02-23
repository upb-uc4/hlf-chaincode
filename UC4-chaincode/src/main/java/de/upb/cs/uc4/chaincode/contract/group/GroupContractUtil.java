package de.upb.cs.uc4.chaincode.contract.group;

import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.contract.certificate.CertificateContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.exceptions.SerializableError;
import de.upb.cs.uc4.chaincode.model.Group;
import de.upb.cs.uc4.chaincode.model.errors.GenericError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GroupContractUtil extends ContractUtil {

    public GroupContractUtil() {
        keyPrefix = "group";
        thing = "Group";
        errorPrefix = thing.toLowerCase();
        identifier = "groupId";
    }

    public InvalidParameter getInvalidUserNotRegistered() {
        return new InvalidParameter()
                .name(errorPrefix + ".enrollmentId")
                .reason("The user you are trying to add to a group is not registered in the system.");
    }

    public GenericError getUserNoRemoveError(String enrollmentId, String groupId) {
        return new GenericError()
                .type("HLNotFound")
                .title("There is no user with enrollmentId " + enrollmentId + " in the group " + groupId);
    }

    /**
     * Returns a list of errors describing everything wrong with the given group parameters
     *
     * @param enrollmentId group to return errors for
     * @return a list of all errors found for the given matriculationData
     */
    public List<InvalidParameter> getSemanticErrorsForUserInGroup(
            ChaincodeStub stub,
            String enrollmentId) {

        CertificateContractUtil certificateUtil = new CertificateContractUtil();

        List<InvalidParameter> invalidParameters = new ArrayList<>();

        if (!(certificateUtil.keyExists(stub, enrollmentId))) {
            invalidParameters.add(getInvalidUserNotRegistered());
        }

        return invalidParameters;
    }

    /**
     * Returns a list of errors describing everything wrong with the given group parameters
     *
     * @param enrollmentId enrollmentId to return errors for
     * @return a list of all errors found for the given matriculationData
     */
    public List<InvalidParameter> getParameterErrorsForEnrollmentId(String enrollmentId) {

        List<InvalidParameter> invalidparams = new ArrayList<>();

        if (valueUnset(enrollmentId)) {
            invalidparams.add(getEmptyInvalidParameter(errorPrefix + ".enrollmentId"));
        }

        return invalidparams;
    }

    public List<InvalidParameter> getParameterErrorsForGroupId(String groupId) {
        List<InvalidParameter> invalidparams = new ArrayList<>();

        if (valueUnset(groupId)) {
            invalidparams.add(getEmptyInvalidParameter(errorPrefix + ".groupId"));
        }

        return invalidparams;
    }

    public List<Group> getAllGroups(ChaincodeStub stub) {
        return this.getAllStates(stub, Group.class);
    }

    public List<Group> getGroupsForUser(ChaincodeStub stub, String enrollmentId) {
        return this.getAllGroups(stub).stream().filter(item -> item.getUserList().contains(enrollmentId)).collect(Collectors.toList());
    }

    public List<String> getUsersForGroup(ChaincodeStub stub, String groupId) throws LedgerAccessError {
        return this.getState(stub, groupId, Group.class).getUserList();
    }

    public List<String> getGroupNamesForUser(ChaincodeStub stub, String enrollmentId) {
        return this.getGroupsForUser(stub, enrollmentId).stream().map(Group::getGroupId).collect(Collectors.toList());
    }

    public void checkParamsAddUserToGroup(Context ctx, String[] params) throws ParameterError {
        if (params.length != 2) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String enrollmentId = params[0];
        String groupId = params[1];

        ChaincodeStub stub = ctx.getStub();

        List<InvalidParameter> invalidParams = getParameterErrorsForEnrollmentId(enrollmentId);
        invalidParams.addAll(getParameterErrorsForGroupId(groupId));
        invalidParams.addAll(getSemanticErrorsForUserInGroup(stub, enrollmentId));
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
    }

    public void checkParamsRemoveUserFromGroup(Context ctx, String[] params) throws SerializableError {
        if (params.length != 2) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String enrollmentId = params[0];
        String groupId = params[1];

        ChaincodeStub stub = ctx.getStub();
        List<InvalidParameter> invalidParams = getParameterErrorsForEnrollmentId(enrollmentId);
        invalidParams.addAll(getParameterErrorsForGroupId(groupId));
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
        Group group = getState(stub, groupId, Group.class);
        if (!(group.getUserList().contains(enrollmentId))) {
            throw new ParameterError(GsonWrapper.toJson(getUserNoRemoveError(enrollmentId, groupId)));
        }
    }

    public void checkParamsRemoveUserFromAllGroups(String[] params) throws ParameterError {
        if (params.length != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String enrollmentId = params[0];

        List<InvalidParameter> invalidParams = getParameterErrorsForEnrollmentId(enrollmentId);
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
    }

    public void checkParamsGetUsersForGroup(Context ctx, String[] params) throws SerializableError {
        if (params.length != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String groupId = params[0];

        ChaincodeStub stub = ctx.getStub();
        List<InvalidParameter> invalidParams = getParameterErrorsForGroupId(groupId);
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
        getUsersForGroup(stub, groupId);

    }

    public void checkParamsGetGroupsForUser(String[] params) throws ParameterError {
        if (params.length != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String enrollmentId = params[0];

        List<InvalidParameter> invalidParams = getParameterErrorsForEnrollmentId(enrollmentId);
        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }
    }
}