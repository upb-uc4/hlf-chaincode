package de.upb.cs.uc4.chaincode;

import com.google.gson.reflect.TypeToken;
import de.upb.cs.uc4.chaincode.model.*;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Contract(
        name="UC4.MatriculationData"
)
@Default
public class MatriculationDataChaincode implements ContractInterface {

    // setup gson (de-)serializer capable of (de-)serializing dates
    private static final GsonWrapper GSON = new GsonWrapper();

    @Transaction()
    public void initLedger(final Context ctx) {

    }

    /**
     * Adds MatriculationData to the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param jsonMatriculationData json-representation of a MatriculationData to be added
     * @return Empty string on success, serialized error on failure
     */
    @Transaction()
    public String addMatriculationData(final Context ctx, final String jsonMatriculationData) {

        ChaincodeStub stub = ctx.getStub();
        MatriculationData matriculationData;

        try {
            matriculationData = GSON.fromJson(jsonMatriculationData, MatriculationData.class);
        } catch(Exception e) {
            return GSON.toJson(new GenericError()
                    .type("hl: unprocessable entity")
                    .title("The given parameter does not conform to the specified format."));
        }

        ArrayList<InvalidParameter> invalidParams = getErrorForMatriculationData(matriculationData);

        if (!invalidParams.isEmpty()) {
            return GSON.toJson(new DetailedError()
                    .type("hl: unprocessable field")
                    .title("The following fields in the given parameters do not conform to the specified format.")
                    .invalidParams(invalidParams));
        }

        String result = stub.getStringState(matriculationData.getMatriculationId());
        if (result != null && !result.equals("")) {
            return GSON.toJson(new GenericError()
                    .type("hl: conflict")
                    .title("There is already a MatriculationData for the given matriculationId."));
        }

        stub.putStringState(matriculationData.getMatriculationId(),GSON.toJson(matriculationData));
        return "";
    }

    /**
     * Updates MatriculationData on the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param jsonMatriculationData json-representation of the new MatriculationData to replace the old with
     * @return Empty string on success, serialized error on failure
     */
    @Transaction()
    public String updateMatriculationData(final Context ctx, final String jsonMatriculationData) {

        ChaincodeStub stub = ctx.getStub();

        MatriculationData updatedMatriculationData;
        try {
            updatedMatriculationData = GSON.fromJson(jsonMatriculationData, MatriculationData.class);
        } catch(Exception e) {
        return GSON.toJson(new GenericError()
                .type("hl: unprocessable entity")
                .title("The given parameter does not conform to the specified format."));
        }

        ArrayList<InvalidParameter> invalidParams = getErrorForMatriculationData(updatedMatriculationData);

        if (!invalidParams.isEmpty()) {
            return GSON.toJson(new DetailedError()
                    .type("hl: unprocessable field")
                    .title("The following fields in the given parameters do not conform to the specified format.")
                    .invalidParams(invalidParams));
        }

        String MatriculationDataOnLedger = stub.getStringState(updatedMatriculationData.getMatriculationId());

        if (MatriculationDataOnLedger == null || MatriculationDataOnLedger.equals("")) {
            return GSON.toJson(new GenericError()
                    .type("hl: not found")
                    .title("There is no MatriculationData for the given matriculationId."));
        }

        stub.delState(updatedMatriculationData.getMatriculationId());
        stub.putStringState(updatedMatriculationData.getMatriculationId(), GSON.toJson(updatedMatriculationData));
        return "";
    }

    /**
     * Gets MatriculationData from the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * @param matriculationId matriculationId of the MatriculationData to be returned
     * @return Serialized MatriculationData on success, serialized error on failure
     */
    @Transaction()
    public String getMatriculationData(final Context ctx, final String matriculationId) {

        ChaincodeStub stub = ctx.getStub();
        MatriculationData matriculationData;

        try {
            matriculationData = GSON.fromJson(stub.getStringState(matriculationId), MatriculationData.class);
        } catch(Exception e) {
            return GSON.toJson(new GenericError()
                    .type("hl: unprocessable ledger state")
                    .title("The state on the ledger does not conform to the specified format."));
        }

        if (matriculationData == null) {
            return GSON.toJson(new DetailedError()
                    .type("hl: not found")
                    .title("There is no MatriculationData for the given matriculationId."));
        }
        return GSON.toJson(matriculationData);
    }

    /**
     * Adds a semester entry to a fieldOfStudy of MatriculationData on the ledger.
     * @param ctx transaction context providing access to ChaincodeStub etc.
     * TODO add param
     * @return Empty string on success, serialized error on failure
     */
    @Transaction()
    public String addEntryToMatriculationData (
            final Context ctx,
            final String matriculationId,
            final String jsonSubjectMatriculation) {

        ChaincodeStub stub = ctx.getStub();

        String jsonMatriculationData = stub.getStringState(matriculationId);

        if (jsonMatriculationData == null || jsonMatriculationData.equals("")) {
            return GSON.toJson(new GenericError()
                    .type("hl: not found")
                    .title("There is no MatriculationData for the given matriculationId."));
        }

        MatriculationData matriculationData;

        try {
            matriculationData = GSON.fromJson(jsonMatriculationData, MatriculationData.class);
        } catch(Exception e) {
            return GSON.toJson(new GenericError()
                    .type("hl: unprocessable ledger state")
                    .title("The state on the ledger does not conform to the specified format."));
        }

        Type listType = new TypeToken<ArrayList<MatriculationData>>(){}.getType();
        ArrayList<SubjectMatriculation> matriculationStatus = GSON.fromJson(jsonSubjectMatriculation, listType);

        ArrayList<InvalidParameter> invalidParams = getErrorForSubjectMatriculationList(matriculationStatus);

        if (!invalidParams.isEmpty()) {
            return GSON.toJson(new DetailedError()
                    .type("hl: unprocessable field")
                    .title("The following fields in the given parameters do not conform to the specified format.")
                    .invalidParams(invalidParams));
        }

        // TODO move test for existence into SubjectMatriculation.addSemestersItem
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

        stub.delState(matriculationData.getMatriculationId());
        stub.putStringState(matriculationData.getMatriculationId(), GSON.toJson(matriculationData));
        return "";
    }

    /**
     * Returns a list of errors describing everything wrong with the given matriculationData
     * @param matriculationData matriculationData to return errors for
     * @return a list of all errors found for the given matriculationData
     */
    private ArrayList<InvalidParameter> getErrorForMatriculationData(MatriculationData matriculationData) {

        ArrayList<InvalidParameter> list = new ArrayList<>();

        if(matriculationData.getMatriculationId() == null || matriculationData.getMatriculationId().equals("")) {
            addAbsent(list, new InvalidParameter()
                    .name("matriculationId")
                    .reason("ID must not be empty"));
        }

        if (matriculationData.getFirstName() == null || matriculationData.getFirstName().equals("")) {
            addAbsent(list, new InvalidParameter()
                    .name("firstName")
                    .reason("First name must not be empty"));
        }

        if (matriculationData.getLastName() == null || matriculationData.getLastName().equals("")) {
            addAbsent(list, new InvalidParameter()
                    .name("lastName")
                    .reason("Last name must not be empty"));
        }

        if (matriculationData.getBirthDate() == null) {
            addAbsent(list, new InvalidParameter()
                    .name("birthDate")
                    .reason("Birth date must be the following format \"yyyy-mm-dd\""));
        }

        List<SubjectMatriculation> matriculationStatus = matriculationData.getMatriculationStatus();
        list.addAll(getErrorForSubjectMatriculationList(
                matriculationStatus,
                matriculationData,
                "matriculationStatus"));
        return list;
    }

    private ArrayList<InvalidParameter> getErrorForSubjectMatriculationList(
            List<SubjectMatriculation> matriculationStatus) {
        return getErrorForSubjectMatriculationList(matriculationStatus, null, "");
    }

    private ArrayList<InvalidParameter> getErrorForSubjectMatriculationList(
            List<SubjectMatriculation> matriculationStatus,
            MatriculationData matriculationData,
            String prefix) { // TODO prefix param

        ArrayList<InvalidParameter> list = new ArrayList<>();

        if (matriculationStatus == null || matriculationStatus.isEmpty()) {
            addAbsent(list, new InvalidParameter()
                    .name(prefix)
                    .reason("Matriculation status must not be empty"));
        } else {

            ArrayList<SubjectMatriculation.FieldOfStudyEnum> existingFields = new ArrayList<>();

            for (int subMatIndex=0; subMatIndex<matriculationStatus.size(); subMatIndex++) {

                SubjectMatriculation subMat = matriculationStatus.get(subMatIndex);

                if (subMat.getFieldOfStudy() == null) {
                    addAbsent(list, new InvalidParameter()
                            .name(prefix+"["+subMatIndex+"].fieldOfStudy")
                            .reason("Field of study must be one of the specified values."));
                } else {
                    if (existingFields.contains(subMat.getFieldOfStudy())) {
                        addAbsent(list, new InvalidParameter()
                                .name(prefix+"["+subMatIndex+"].fieldOfStudy")
                                .reason("Each field of study must only appear in one matriculationStatus."));
                    } else
                        existingFields.add(subMat.getFieldOfStudy());
                }

                List<String> semesters = subMat.getSemesters();
                if (semesters == null || semesters.isEmpty()) {
                    addAbsent(list, new InvalidParameter()
                            .name(prefix+"["+subMatIndex+"].semesters")
                            .reason("Semesters must not be empty."));
                }

                ArrayList<String> existingSemesters = new ArrayList<>();
                for (int semesterIndex=0; semesterIndex<semesters.size(); semesterIndex++) {

                    String semester = semesters.get(semesterIndex);

                    if (semesterFormatValid(semester) && matriculationData.getBirthDate() != null) {

                        int semesterYear = Integer.parseInt(semester.substring(2, 6));
                        if (semesterYear < matriculationData.getBirthDate().getYear()) {
                            addAbsent(list, new InvalidParameter()
                                    .name(prefix+"["+subMatIndex+"].semesters["+semesterIndex+"]")
                                    .reason("Semester must not be earlier than birth date."));
                        }

                        if (existingSemesters.contains(semester)) {
                            addAbsent(list, new InvalidParameter()
                                    .name(prefix+"["+subMatIndex+"].semesters["+semesterIndex+"]")
                                    .reason("Each semester must only appear once in matriculationStatus.semesters."));
                        } else
                            existingSemesters.add(semester);
                    }

                    if (!semesterFormatValid(semester)) {
                        addAbsent(list, new InvalidParameter()
                                .name(prefix+"["+subMatIndex+"].semesters["+semesterIndex+"]")
                                .reason("Semester must be the following format \"(WS\\d{4}/\\d{2}|SS\\d{4})\", e.g. \"WS2020/21\""));
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
            if (year2 != (year1 + 1) % 100)
                return false;
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
