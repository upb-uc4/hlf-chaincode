package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.exceptions.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.model.errors.GenericError;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GroupContractUtil extends ContractUtil {
    private final String thing = "Group";
    private final String prefix = thing.toLowerCase();
    private final String identifier = "groupId";

    public GroupContractUtil() {
        keyPrefix = "group";
    }

    @Override
    public GenericError getConflictError() {
        return super.getConflictError(thing, identifier);
    }

    @Override
    public GenericError getNotFoundError() {
        return super.getNotFoundError(thing, identifier);
    }

    /**
     * Returns a list of errors describing everything wrong with the given group parameters
     *
     * @param group group to return errors for
     * @return a list of all errors found for the given matriculationData
     */
    public ArrayList<InvalidParameter> getSemanticErrorsForGroup(
            ChaincodeStub stub,
            Group group) {

        ArrayList<InvalidParameter> invalidParameters = new ArrayList<>();

        if (!this.checkModuleAvailable(stub, group)) {
            invalidParameters.add(getInvalidModuleAvailable("enrollmentId"));
        }

        return invalidParameters;
    }

    /**
     * Returns a list of errors describing everything wrong with the given group parameters
     *
     * @param group group to return errors for
     * @return a list of all errors found for the given matriculationData
     */
    public ArrayList<InvalidParameter> getParameterErrorsForGroup(
            Group group) {

        ArrayList<InvalidParameter> invalidparams = new ArrayList<>();

        if (valueUnset(group.getEnrollmentId())) {
            invalidparams.add(getEmptyEnrollmentIdParam(prefix + "."));
        }
        if (valueUnset(group.getGroupId())) {
            invalidparams.add(getEmptyInvalidParameter(prefix + ".groupId"));
        }

        return invalidparams;
    }
}
