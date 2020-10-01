package de.upb.cs.uc4.chaincode;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.model.*;
import de.upb.cs.uc4.chaincode.util.GsonWrapper;
import de.upb.cs.uc4.chaincode.util.MatriculationDataContractUtil;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Contract(
        name="UC4.MatriculationData"
)
@Default
public class MatriculationDataChaincode implements ContractInterface {

    private final MatriculationDataContractUtil cUtil = new MatriculationDataContractUtil();

    @Transaction()
    public void initLedger(final Context ctx) {

    }

    /**
     * Adds MatriculationData to the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param newMatriculationData MatriculationData to be added
     * @return newMatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String addMatriculationData(final Context ctx, String newMatriculationData) {

        ChaincodeStub stub = ctx.getStub();

        MatriculationData matriculationData;
        try {
            matriculationData = GsonWrapper.fromJson(newMatriculationData, MatriculationData.class);
        } catch(Exception e) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableMatriculationDataParam()));
        }

        ArrayList<InvalidParameter> invalidParams = getErrorForMatriculationData(
                matriculationData, "matriculationData");

        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        String result = stub.getStringState(matriculationData.getEnrollmentId());
        if (result != null && !result.equals("")) {
            return GsonWrapper.toJson(cUtil.getConflictError());
        }

        return cUtil.putAndGetStringState(stub, matriculationData.getEnrollmentId(), GsonWrapper.toJson(matriculationData));
    }

    /**
     * Updates MatriculationData on the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param updatedMatriculationData json-representation of the new MatriculationData to replace the old with
     * @return updatedMatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String updateMatriculationData(final Context ctx, String updatedMatriculationData) {

        ChaincodeStub stub = ctx.getStub();

        MatriculationData matriculationData;
        try {
            matriculationData = GsonWrapper.fromJson(updatedMatriculationData, MatriculationData.class);
        } catch(Exception e) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableMatriculationDataParam()));
        }

        ArrayList<InvalidParameter> invalidParams = getErrorForMatriculationData(
                matriculationData, "matriculationData");

        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

        String MatriculationDataOnLedger = stub.getStringState(matriculationData.getEnrollmentId());

        if (MatriculationDataOnLedger == null || MatriculationDataOnLedger.equals("")) {
            return GsonWrapper.toJson(cUtil.getNotFoundError());
        }

        return cUtil.putAndGetStringState(stub, matriculationData.getEnrollmentId(), GsonWrapper.toJson(matriculationData));
    }

    /**
     * Gets MatriculationData from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId of the MatriculationData to be returned
     * @return Serialized MatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String getMatriculationData(final Context ctx, final String enrollmentId) {

        ChaincodeStub stub = ctx.getStub();
        MatriculationData matriculationData;

        try {
            matriculationData = GsonWrapper.fromJson(stub.getStringState(enrollmentId), MatriculationData.class);
        } catch(Exception e) {
            return GsonWrapper.toJson(cUtil.getUnprocessableLedgerStateError());
        }

        if (matriculationData == null) {
            return GsonWrapper.toJson(cUtil.getNotFoundError());
        }
        return GsonWrapper.toJson(matriculationData);
    }

    /**
     * Adds a semester entry to a fieldOfStudy of MatriculationData on the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param enrollmentId enrollmentId to add the matriculations to
     * @param matriculations list of matriculations
     * @return Updated MatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String addEntriesToMatriculationData (
            final Context ctx,
            final String enrollmentId,
            final String matriculations) {

        ChaincodeStub stub = ctx.getStub();

        // retrieve jsonMatriculationData
        String jsonMatriculationData;
        try {
            jsonMatriculationData = stub.getStringState(enrollmentId);
        } catch(Exception e) {
            return GsonWrapper.toJson(cUtil.getNotFoundError());
        }
        if (jsonMatriculationData == null || jsonMatriculationData.equals("")) {
            return GsonWrapper.toJson(cUtil.getNotFoundError());
        }

        // retrieve MatriculationData Object
        MatriculationData matriculationData;

        try {
            matriculationData = GsonWrapper.fromJson(jsonMatriculationData, MatriculationData.class);
        } catch(Exception e) {
            return GsonWrapper.toJson(cUtil.getUnprocessableLedgerStateError());
        }

        // manipulate object as intended
        Type listType = new TypeToken<ArrayList<SubjectMatriculation>>(){}.getType();
        ArrayList<SubjectMatriculation> matriculationStatus;
        try {
            matriculationStatus = GsonWrapper.fromJson(matriculations, listType);
        } catch(Exception e) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(cUtil.getUnparsableMatriculationParam()));
        }

        ArrayList<InvalidParameter> invalidParams = getErrorForSubjectMatriculationList(
                matriculationStatus, "matriculations");

        if (!invalidParams.isEmpty()) {
            return GsonWrapper.toJson(cUtil.getUnprocessableEntityError(invalidParams));
        }

       for (SubjectMatriculation newItem: matriculationStatus) {
           boolean exists = false;
            for (SubjectMatriculation item : matriculationData.getMatriculationStatus()) {
                if (item.getFieldOfStudy() == newItem.getFieldOfStudy()) {
                    exists = true;
                    for (String newSemester : newItem.getSemesters()) {
                        if (item.getSemesters().contains(newSemester))
                            continue;
                        item.addsemestersItem(newSemester);
                    }
                }
            }
            if (!exists) {
                SubjectMatriculation item = new SubjectMatriculation().fieldOfStudy(newItem.getFieldOfStudy());
                matriculationData.getMatriculationStatus().add(item);
                for (String newSemester : newItem.getSemesters()) {
                    item.addsemestersItem(newSemester);
                }
            }
        }

        return cUtil.putAndGetStringState(stub, matriculationData.getEnrollmentId(), GsonWrapper.toJson(matriculationData));
    }

    /**
     * Returns a list of errors describing everything wrong with the given matriculationData
     * @param matriculationData matriculationData to return errors for
     * @return a list of all errors found for the given matriculationData
     */
    private ArrayList<InvalidParameter> getErrorForMatriculationData(
            MatriculationData matriculationData,
            String prefix) {

        if (!prefix.isEmpty())
            prefix += ".";

        ArrayList<InvalidParameter> list = new ArrayList<>();

        if(matriculationData.getEnrollmentId() == null || matriculationData.getEnrollmentId().equals("")) {
            addAbsent(list, cUtil.getEmptyEnrollmentIdParam(prefix));
        }

        List<SubjectMatriculation> matriculationStatus = matriculationData.getMatriculationStatus();
        list.addAll(getErrorForSubjectMatriculationList(
                matriculationStatus,
                prefix+"matriculationStatus"));
        return list;
    }

    private ArrayList<InvalidParameter> getErrorForSubjectMatriculationList(
            List<SubjectMatriculation> matriculationStatus,
            String prefix) {

        ArrayList<InvalidParameter> list = new ArrayList<>();

        if (matriculationStatus == null || matriculationStatus.isEmpty()) {
            addAbsent(list, cUtil.getEmptyMatriculationStatusParam(prefix));
        } else {

            ArrayList<SubjectMatriculation.FieldOfStudyEnum> existingFields = new ArrayList<>();

            for (int subMatIndex=0; subMatIndex<matriculationStatus.size(); subMatIndex++) {

                SubjectMatriculation subMat = matriculationStatus.get(subMatIndex);

                if (subMat.getFieldOfStudy() == null) {
                    addAbsent(list, cUtil.getInvalidFieldOfStudyParam(prefix + "[" + subMatIndex + "]."));
                } else {
                    if (existingFields.contains(subMat.getFieldOfStudy())) {
                        addAbsent(list, cUtil.getDuplicateFieldOfStudyParam(prefix, subMatIndex));
                    } else
                        existingFields.add(subMat.getFieldOfStudy());
                }

                List<String> semesters = subMat.getSemesters();
                if (semesters == null || semesters.isEmpty()) {
                    addAbsent(list, cUtil.getEmptySemestersParam(prefix + "[" + subMatIndex + "]."));
                }

                ArrayList<String> existingSemesters = new ArrayList<>();
                for (int semesterIndex = 0; semesterIndex< Objects.requireNonNull(semesters).size(); semesterIndex++) {

                    String semester = semesters.get(semesterIndex);

                    if (semesterFormatValid(semester)) {
                        if (existingSemesters.contains(semester)) {
                            addAbsent(list, cUtil.getDuplicateSemesterParam(prefix + "["+subMatIndex+"].semesters", semesterIndex));
                        } else
                            existingSemesters.add(semester);
                    }

                    if (!semesterFormatValid(semester)) {
                        addAbsent(list, cUtil.getInvalidSemesterParam(prefix+"["+subMatIndex+"].semesters", semesterIndex));
                    }
                }
            }
        }
        return list;
    }

    /**
     * Checks the given semester string for validity.
     * @param semester semester string to check for validity
     * @return true if semester is a valid description of a semester, false otherwise
     */
    public boolean semesterFormatValid(String semester) {
        Pattern pattern = Pattern.compile("^(WS\\d{4}/\\d{2}|SS\\d{4})");
        Matcher matcher = pattern.matcher(semester);
        if (!matcher.matches())
            return false;
        if ("WS".equals(semester.substring(0,2))) {
            int year1 = Integer.parseInt(semester.substring(4,6));
            int year2 = Integer.parseInt(semester.substring(7,9));
            return year2 == (year1 + 1) % 100;
        }
        return true;
    }

    /**
     * Adds invParam to list, if list does not already contain the invParam. Otherwise does nothing.
     * @param list list to add the invParam to
     * @param invParam invParam to add to list
     */
    private void addAbsent (List<InvalidParameter> list, InvalidParameter invParam) {
        if (!list.contains(invParam))
            list.add(invParam);
    }
}
