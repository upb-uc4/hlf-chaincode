package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.exceptions.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.Group;
import de.upb.cs.uc4.chaincode.model.errors.GenericError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
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
    public ArrayList<InvalidParameter> getSemanticErrorsForUserInGroup(
            ChaincodeStub stub,
            String enrollmentId) {

        CertificateContractUtil certificateUtil = new CertificateContractUtil();

        ArrayList<InvalidParameter> invalidParameters = new ArrayList<>();

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
    public ArrayList<InvalidParameter> getParameterErrorsForEnrollmentId(
            String enrollmentId) {

        ArrayList<InvalidParameter> invalidparams = new ArrayList<>();

        if (valueUnset(enrollmentId)) {
            invalidparams.add(getEmptyInvalidParameter(errorPrefix + ".enrollmentId"));
        }

        return invalidparams;
    }

    public ArrayList<InvalidParameter> getParameterErrorsForGroupId(
            String groupId) {

        ArrayList<InvalidParameter> invalidparams = new ArrayList<>();

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
}