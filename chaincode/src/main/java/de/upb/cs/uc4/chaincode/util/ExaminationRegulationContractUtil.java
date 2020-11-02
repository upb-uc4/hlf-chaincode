package de.upb.cs.uc4.chaincode.util;

import de.upb.cs.uc4.chaincode.error.LedgerAccessError;
import de.upb.cs.uc4.chaincode.model.*;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.*;

public class ExaminationRegulationContractUtil extends ContractUtil {
    private final String thing = "examination regulation";

    public ExaminationRegulationContractUtil() {
        keyPrefix = "examination-regulation";
    }

    @Override
    public GenericError getConflictError() {
        return super.getConflictError(thing);
    }

    @Override
    public GenericError getNotFoundError() {
        return super.getNotFoundError(thing);
    }

    public InvalidParameter getUnparsableExaminationRegulationParam() {
        return new InvalidParameter()
                .name("examinationRegulation")
                .reason("The given parameter cannot be parsed from json");
    }

    private InvalidParameter getEmptyNameParam() {
        return new InvalidParameter()
                .name("examinationRegulation")
                .reason("Examination regulation must not be empty");
    }

    public InvalidParameter getEmptyModulesParam(String prefix) {
        return new InvalidParameter()
                .name(prefix)
                .reason("Modules must not be empty");
    }

    public InvalidParameter getEmptyModuleIdParam(String prefix) {
        return new InvalidParameter()
                .name(prefix + "id")
                .reason("Module id must not be empty");
    }

    public InvalidParameter getEmptyModuleNameParam(String prefix) {
        return new InvalidParameter()
                .name(prefix + "name")
                .reason("Module name must not be empty");
    }

    public InvalidParameter getDuplicateModuleParam(String prefix, int index) {
        return new InvalidParameter()
                .name(prefix + "[" + index + "]")
                .reason("Each module must only appear once in examinationRegulation.modules");
    }

    public InvalidParameter getInconsistentModuleParam(String prefix) {
        return new InvalidParameter()
                .name(prefix)
                .reason("Each module must be consistent with the modules on chain, i.e. if the module.id is equal, then module.name must be too");
    }

    public ExaminationRegulation getState(ChaincodeStub stub, String key) throws LedgerAccessError {
        String jsonExaminationRegulation;
        jsonExaminationRegulation = getStringState(stub, key);
        if (valueUnset(jsonExaminationRegulation)) {
            throw new LedgerAccessError(GsonWrapper.toJson(getNotFoundError()));
        }
        ExaminationRegulation examinationRegulation;
        try {
            examinationRegulation = GsonWrapper.fromJson(jsonExaminationRegulation, ExaminationRegulation.class);
        } catch(Exception e) {
            throw new LedgerAccessError(GsonWrapper.toJson(getUnprocessableLedgerStateError()));
        }
        return examinationRegulation;
    }

    public ArrayList<ExaminationRegulation> getAllStates(ChaincodeStub stub) throws LedgerAccessError {
        QueryResultsIterator<KeyValue> qrIterator;
        qrIterator = getAllRawStates(stub);
        ArrayList<ExaminationRegulation> examinationRegulations = new ArrayList<>();
        for (KeyValue item: qrIterator) {
            ExaminationRegulation examinationRegulation;
            try {
                examinationRegulation = GsonWrapper.fromJson(item.getStringValue(), ExaminationRegulation.class);
            } catch(Exception e) {
                throw new LedgerAccessError(GsonWrapper.toJson(getUnprocessableLedgerStateError()));
            }
            examinationRegulations.add(examinationRegulation);
        }
        return examinationRegulations;
    }

    public ArrayList<InvalidParameter> getErrorForExaminationRegulation(ExaminationRegulation examinationRegulation, Set<Module> validModules) {
        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();

        if(valueUnset(examinationRegulation.getName())) {
            invalidParams.add(getEmptyNameParam());
        }

        invalidParams.addAll(getErrorForModuleList(
                examinationRegulation.getModules(),
                "examinationRegulation.modules",
                validModules));
        return invalidParams;
    }

    public ArrayList<InvalidParameter> getErrorForModuleList(List<Module> modules, String prefix, Set<Module> validModules) {
        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();

        if (valueUnset(modules)) {
            invalidParams.add(getEmptyModulesParam(prefix));
        } else {
            ArrayList<String> existingModules = new ArrayList<>();

            for (int moduleIndex=0; moduleIndex<modules.size(); moduleIndex++) {

                Module module = modules.get(moduleIndex);

                if (valueUnset(module.getId())) {
                    invalidParams.add(getEmptyModuleIdParam(prefix + "[" + moduleIndex + "]."));
                } else {
                    if (existingModules.contains(module.getId())) {
                        invalidParams.add(getDuplicateModuleParam(prefix, moduleIndex));
                    } else
                        existingModules.add(module.getId());
                }
                if (valueUnset(module.getName())) {
                    invalidParams.add(getEmptyModuleNameParam(prefix + "[" + moduleIndex + "]."));
                }
                for (Module validModule: validModules) {
                    if (module.getId().equals(validModule.getId()) && !module.equals(validModule)) {
                        invalidParams.add(getInconsistentModuleParam(prefix + "[" + moduleIndex + "]"));
                        break;
                    }
                }
            }
        }
        return invalidParams;
    }
}
