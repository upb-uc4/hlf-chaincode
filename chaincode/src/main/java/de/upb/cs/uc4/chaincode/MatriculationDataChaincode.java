package de.upb.cs.uc4.chaincode;

import de.upb.cs.uc4.chaincode.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Contract(
        name="UC4.MatriculationData"
)
@Default
public class MatriculationDataChaincode implements ContractInterface {

    private static Log _logger = LogFactory.getLog(MatriculationDataChaincode.class);
    // setup gson (de-)serializer capable of (de-)serializing dates
    private static final GsonWrapper gson = new GsonWrapper();

    @Transaction()
    public void initLedger(final Context ctx) {

    }

    /**
     * Adds MatriculationData to the ledger.
     * @param ctx
     * @param jsonMatriculationData json-representation of a MatriculationData to be added
     * @return Empty string on success, serialized error on failure
     */
    @Transaction()
    public String addMatriculationData(final Context ctx, final String jsonMatriculationData) {
        _logger.info("immatriculateMatriculationData");

        ChaincodeStub stub = ctx.getStub();
        MatriculationData matriculationData;

        try {
            matriculationData = gson.fromJson(jsonMatriculationData, MatriculationData.class);
        } catch(Exception e) {
            return gson.toJson(new GenericError()
                    .type("hl: unprocessable entity")
                    .title("The given parameters do not conform to the specified format."));
        }

        String result = stub.getStringState(matriculationData.getMatriculationId());
        if (result != null && !result.equals("")) {
            return gson.toJson(new GenericError()
                    .type("hl: conflict")
                    .title("There is already a MatriculationData for the given matriculationId."));
        }

        ArrayList<InvalidParameter> invalidParams = getErrorForMatriculationData(matriculationData);

        if(!invalidParams.isEmpty()){
            return gson.toJson(new DetailedError()
                    .type("hl: unprocessable entity")
                    .title("The given parameters do not conform to the specified format.")
                    .invalidParams(invalidParams));
        }

        stub.putStringState(matriculationData.getMatriculationId(),gson.toJson(matriculationData));
        return "";
    }

    @Transaction()
    public String updateMatriculationData(final Context ctx, final String jsonMatriculationData) {

        ChaincodeStub stub = ctx.getStub();

        MatriculationData updatedMatriculationData;
        try {
            updatedMatriculationData = gson.fromJson(jsonMatriculationData, MatriculationData.class);
        } catch(Exception e) {
        return gson.toJson(new GenericError()
                .type("hl: unprocessable entity")
                .title("The given parameters do not conform to the specified format."));
        }

        ArrayList<InvalidParameter> invalidParams = getErrorForMatriculationData(updatedMatriculationData);

        if (!invalidParams.isEmpty())
            return gson.toJson(new DetailedError()
                    .type("hl: unprocessable entity")
                    .title("The given parameters do not conform to the specified format.")
                    .invalidParams(invalidParams));

        String MatriculationDataOnLedger = stub.getStringState(updatedMatriculationData.getMatriculationId());

        if(MatriculationDataOnLedger == null || MatriculationDataOnLedger.equals(""))
            return gson.toJson(new GenericError()
                    .type("hl: not found")
                    .title("There is no MatriculationData for the given matriculationId."));

        stub.delState(updatedMatriculationData.getMatriculationId());
        stub.putStringState(updatedMatriculationData.getMatriculationId(), gson.toJson(updatedMatriculationData));
        return "";
    }

    @Transaction()
    public String getMatriculationData(final Context ctx, final String matriculationId) {

        ChaincodeStub stub = ctx.getStub();
        MatriculationData matriculationData;

        try {
            matriculationData = gson.fromJson(stub.getStringState(matriculationId), MatriculationData.class);
        } catch(Exception e) {
            return gson.toJson(new GenericError()
                    .type("hl: unprocessable ledger state")
                    .title("The state on the ledger does not conform to the specified format."));
        }

        if(matriculationData == null || matriculationData.equals(""))
            return gson.toJson(new DetailedError()
                    .type("hl: not found")
                    .title("There is no MatriculationData for the given matriculationId."));
        return gson.toJson(matriculationData);
    }

    @Transaction()
    public String addEntryToMatriculationData (
            final Context ctx,
            final String matriculationId,
            final String fieldOfStudy,
            final String semester) {

        ArrayList<InvalidParameter> invalidParams = new ArrayList<>();
        SubjectMatriculation.FieldOfStudyEnum fieldOfStudyValue = SubjectMatriculation.FieldOfStudyEnum.fromValue(fieldOfStudy);

        if (fieldOfStudyValue == null)
            invalidParams.add(new InvalidParameter()
                    .name("fieldOfStudy")
                    .reason("The given value is not accepted."));

        if (!semesterFormatValid(semester))
            invalidParams.add(new InvalidParameter()
                    .name("semester")
                    .reason("First semester must be the following format \"(WS\\d{4}/\\d{2}|SS\\d{4})\", e.g. \"WS2020/21\""));

        if (!invalidParams.isEmpty())
            return gson.toJson(new DetailedError()
                    .type("hl: unprocessable entity")
                    .title("The given parameters do not conform to the specified format.")
                    .invalidParams(invalidParams));

        ChaincodeStub stub = ctx.getStub();

        String jsonMatriculationData = stub.getStringState(matriculationId);

        if(jsonMatriculationData == null || jsonMatriculationData.equals(""))
            return gson.toJson(new GenericError()
                    .type("hl: not found")
                    .title("There is no MatriculationData for the given matriculationId."));

        MatriculationData matriculationData;

        try {
            matriculationData = gson.fromJson(jsonMatriculationData, MatriculationData.class);
        } catch(Exception e) {
            return gson.toJson(new GenericError()
                    .type("hl: unprocessable ledger state")
                    .title("The state on the ledger does not conform to the specified format."));
        }

        for (SubjectMatriculation item: matriculationData.getMatriculationStatus()) {
            if (item.getFieldOfStudy() == fieldOfStudyValue) {
                for (String existingSemester: item.getSemesters()) {
                    if (existingSemester.equals(semester))
                        return "";
                }
                item.addsemestersItem(semester);
                return "";
            }
        }

        matriculationData.addMatriculationStatusItem(new SubjectMatriculation()
                .fieldOfStudy(fieldOfStudyValue)
                .semesters(new ArrayList<String>()
                {{add(semester);}})
        );

        return "";
    }

    private ArrayList<InvalidParameter> getErrorForMatriculationData(MatriculationData matriculationData) {

        ArrayList<InvalidParameter> list = new ArrayList<>();

        if(matriculationData.getMatriculationId() == null || matriculationData.getMatriculationId().equals(""))
            list.add(new InvalidParameter()
                    .name("matriculationID")
                    .reason("ID is empty"));

        if (matriculationData.getFirstName() == null || matriculationData.getFirstName().equals(""))
            list.add(new InvalidParameter()
                    .name("firstName")
                    .reason("First name must not be empty"));

        if (matriculationData.getLastName() == null || matriculationData.getLastName().equals(""))
            list.add(new InvalidParameter()
                    .name("lastName")
                    .reason("Last name must not be empty"));

        if (matriculationData.getBirthDate() == null)
            list.add(new InvalidParameter()
                    .name("birthDate")
                    .reason("Birth date must be the following format \"yyyy-mm-dd\""));

        List<SubjectMatriculation> immatriculationStatus = matriculationData.getMatriculationStatus();

        if (immatriculationStatus == null || immatriculationStatus.size() == 0)
            list.add(new InvalidParameter()
                    .name("matriculationStatus")
                    .reason("Matriculation status must not be empty"));
        else {

            ArrayList<SubjectMatriculation.FieldOfStudyEnum> existingFields = new ArrayList<>();

            for (SubjectMatriculation subMat: immatriculationStatus) {

                if (subMat.getFieldOfStudy() == null || subMat.getFieldOfStudy().equals(""))
                    list.add(new InvalidParameter()
                            .name("SubjectMatriculation.fieldOfStudy")
                            .reason("Field of study must not be empty."));
                else
                    if (existingFields.contains(subMat.getFieldOfStudy()))
                        list.add(new InvalidParameter()
                                .name("SubjectMatriculation.fieldOfStudy")
                                .reason("Each field of study should only appear in one SubjectMatriculation."));
                    else
                        existingFields.add(subMat.getFieldOfStudy());

                if (subMat.getSemesters() == null || subMat.getSemesters().size() == 0)
                    list.add(new InvalidParameter()
                            .name("SubjectMatriculation.semesters")
                            .reason("Semesters must not be empty."));

                ArrayList<String> existingSemesters = new ArrayList<>();

                for (String semester: subMat.getSemesters()) {
                    if (semester == null || semester.equals(""))
                        list.add(new InvalidParameter()
                                .name("matriculationStatus.semesters")
                                .reason("A semester must not be empty."));

                    if (semesterFormatValid(semester) && matriculationData.getBirthDate() != null) {

                        int semesterYear = Integer.parseInt(semester.substring(2, 6));
                        if (semesterYear < matriculationData.getBirthDate().getYear()) {
                            list.add(new InvalidParameter()
                                    .name("matriculationStatus.semesters")
                                    .reason("First semester must not be earlier than birth date."));
                        }

                        if (existingSemesters.contains(semester))
                            list.add(new InvalidParameter()
                                    .name("SubjectMatriculation.semesters")
                                    .reason("Each semester should only appear once in SubjectMatriculation.semesters."));
                        else
                            existingSemesters.add(semester);
                    }

                    if (!semesterFormatValid(semester))
                        list.add(new InvalidParameter()
                                .name("matriculationStatus.semesters")
                                .reason("Semester must be the following format \"(WS\\d{4}/\\d{2}|SS\\d{4})\", e.g. \"WS2020/21\""));
                }
            }
        }

        return list;
    }

    public boolean semesterFormatValid(String semester) {
        Pattern pattern = Pattern.compile("^(WS\\d{4}/\\d{2}|SS\\d{4})");
        Matcher matcher = pattern.matcher(semester);
        return matcher.matches();
    }
}
