package de.upb.cs.uc4.chaincode.contract.examinationregulation;

import de.upb.cs.uc4.chaincode.contract.ContractUtil;
import de.upb.cs.uc4.chaincode.exceptions.serializable.LedgerAccessError;
import de.upb.cs.uc4.chaincode.exceptions.serializable.ParameterError;
import de.upb.cs.uc4.chaincode.helper.GeneralHelper;
import de.upb.cs.uc4.chaincode.model.ExaminationRegulation.ExaminationRegulation;
import de.upb.cs.uc4.chaincode.model.ExaminationRegulation.ExaminationRegulationModule;
import de.upb.cs.uc4.chaincode.model.errors.InvalidParameter;
import de.upb.cs.uc4.chaincode.helper.GsonWrapper;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExaminationRegulationContractUtil extends ContractUtil {
    private final String prefix = "examinationRegulation";

    public ExaminationRegulationContractUtil() {
        keyPrefix = "examination-regulation";
        thing = "examination regulation";
        identifier = "name";
    }

    public InvalidParameter getUnparsableExaminationRegulationParam() {
        return new InvalidParameter()
                .name(prefix)
                .reason("The given parameter cannot be parsed from json");
    }

    public InvalidParameter getUnparsableNameListParam() {
        return new InvalidParameter()
                .name("names")
                .reason("The given parameter cannot be parsed from json");
    }

    public InvalidParameter getDuplicateModuleParam(String parameterName) {
        return new InvalidParameter()
                .name(parameterName)
                .reason("Each module must only appear once in examinationRegulation.modules");
    }

    public InvalidParameter getInconsistentModuleParam(String parameterName) {
        return new InvalidParameter()
                .name(parameterName)
                .reason("Each module must be consistent with the modules on chain, i.e. if the module.id is equal, then module.name must be too");
    }

    public HashSet<ExaminationRegulationModule> getValidModules(ChaincodeStub stub) {
        HashSet<ExaminationRegulationModule> validModules = new HashSet<>();
        List<ExaminationRegulation> regulations = getAllStates(stub, ExaminationRegulation.class);
        for (ExaminationRegulation regulation : regulations) {
            validModules.addAll(regulation.getModules());
        }
        return validModules;
    }

    public boolean checkModuleAvailable(ChaincodeStub stub, String moduleId) {
         return getValidModules(stub).stream().map(ExaminationRegulationModule::getId).anyMatch(item -> item.equals(moduleId));
    }

    public List<InvalidParameter> getErrorForExaminationRegulation(ExaminationRegulation examinationRegulation, Set<ExaminationRegulationModule> validModules) {
        List<InvalidParameter> invalidParams = new ArrayList<>();

        if (GeneralHelper.valueUnset(examinationRegulation.getName())) {
            invalidParams.add(getEmptyInvalidParameter(this.prefix + ".name"));
        }

        invalidParams.addAll(getErrorForModuleList(
                examinationRegulation.getModules(),
                this.prefix + ".modules",
                validModules));
        return invalidParams;
    }

    public List<InvalidParameter> getErrorForModuleList(List<ExaminationRegulationModule> modules, String errorName, Set<ExaminationRegulationModule> validModules) {
        List<InvalidParameter> invalidParams = new ArrayList<>();

        if (GeneralHelper.valueUnset(modules)) {
            invalidParams.add(getEmptyInvalidParameter(errorName));
        } else {
            List<String> existingModules = new ArrayList<>();

            for (int moduleIndex = 0; moduleIndex < modules.size(); moduleIndex++) {

                ExaminationRegulationModule module = modules.get(moduleIndex);

                if (GeneralHelper.valueUnset(module.getId())) {
                    invalidParams.add(getEmptyInvalidParameter(prefix + ".modules[" + moduleIndex + "].id"));
                } else {
                    if (existingModules.contains(module.getId())) {
                        invalidParams.add(getDuplicateModuleParam(prefix + ".modules[" + moduleIndex + "]"));
                    } else
                        existingModules.add(module.getId());
                }
                if (GeneralHelper.valueUnset(module.getName())) {
                    invalidParams.add(getEmptyInvalidParameter(prefix + ".modules[" + moduleIndex + "].name"));
                }
                for (ExaminationRegulationModule validModule : validModules) {
                    if (module.getId().equals(validModule.getId()) && !module.equals(validModule)) {
                        invalidParams.add(getInconsistentModuleParam(prefix + ".modules[" + moduleIndex + "]"));
                        break;
                    }
                }
            }
        }
        return invalidParams;
    }

    public void checkParamsAddExaminationRegulation(Context ctx, String[] params) throws ParameterError {
        if (params.length != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String examinationRegulation = params[0];

        ChaincodeStub stub = ctx.getStub();

        ExaminationRegulation newExaminationRegulation;
        try {
            newExaminationRegulation = GsonWrapper.fromJson(examinationRegulation, ExaminationRegulation.class);
        } catch (Exception e) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(getUnparsableExaminationRegulationParam())));
        }

        HashSet<ExaminationRegulationModule> validModules = getValidModules(stub);
        List<InvalidParameter> invalidParams = getErrorForExaminationRegulation(newExaminationRegulation, validModules);

        if (!invalidParams.isEmpty()) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(invalidParams)));
        }

        String result = getStringState(stub, newExaminationRegulation.getName());
        if (result != null && !result.equals("")) {
            throw new ParameterError(GsonWrapper.toJson(getConflictError()));
        }
    }

    public void checkParamsGetExaminationRegulations(String[] params) throws ParameterError {
        if (params.length != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String names = params[0];

        try {
            GsonWrapper.fromJson(names, String[].class);
        } catch (Exception e) {
            throw new ParameterError(GsonWrapper.toJson(getUnprocessableEntityError(getUnparsableNameListParam())));
        }
    }

    public void checkParamsCloseExaminationRegulation(Context ctx, String[] params) throws LedgerAccessError, ParameterError {
        if (params.length != 1) {
            throw new ParameterError(GsonWrapper.toJson(getParamNumberError()));
        }
        String name = params[0];

        ChaincodeStub stub = ctx.getStub();
        getState(stub, name, ExaminationRegulation.class);
    }

    public boolean moduleExists(ChaincodeStub stub, String moduleId) {
        return getAllStates(stub, ExaminationRegulation.class)
                .stream().anyMatch(er -> er.getModules().stream().anyMatch(module -> module.getId().equals(moduleId)));
    }
}
