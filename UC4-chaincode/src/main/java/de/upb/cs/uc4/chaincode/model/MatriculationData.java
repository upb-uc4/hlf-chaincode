package de.upb.cs.uc4.chaincode.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MatriculationData {
    @SerializedName("enrollmentId")
    private String enrollmentId = null;

    @SerializedName("matriculationStatus")
    private List<SubjectMatriculation> matriculationStatus = null;

    public MatriculationData enrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
        return this;
    }

    /**
     * Get matriculationId
     *
     * @return matriculationId
     **/
    @ApiModelProperty(value = "")
    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public MatriculationData matriculationStatus(List<SubjectMatriculation> matriculationStatus) {
        this.matriculationStatus = matriculationStatus;
        return this;
    }

    public MatriculationData addMatriculationStatusItem(SubjectMatriculation matriculationStatusItem) {
        if (this.matriculationStatus == null) {
            this.matriculationStatus = new ArrayList<SubjectMatriculation>();
        }
        this.matriculationStatus.add(matriculationStatusItem);
        return this;
    }

    /**
     * Get matriculationStatus
     *
     * @return matriculationStatus
     **/
    @ApiModelProperty(value = "")
    public List<SubjectMatriculation> getMatriculationStatus() {
        return matriculationStatus;
    }

    public void setMatriculationStatus(List<SubjectMatriculation> matriculationStatus) {
        this.matriculationStatus = matriculationStatus;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MatriculationData matriculationData = (MatriculationData) o;
        return Objects.equals(this.enrollmentId, matriculationData.enrollmentId) &&
                Objects.equals(this.matriculationStatus, matriculationData.matriculationStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId, matriculationStatus);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MatriculationData {\n");
        sb.append("    enrollmentId: ").append(toIndentedString(enrollmentId)).append("\n");
        sb.append("    matriculationStatus: ").append(toIndentedString(matriculationStatus)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    public void addAbsent(ArrayList<SubjectMatriculation> matriculationStatus) {
        for (SubjectMatriculation newItem : matriculationStatus) {
            boolean exists = false;
            for (SubjectMatriculation item : this.getMatriculationStatus()) {
                if (item.getFieldOfStudy().equals(newItem.getFieldOfStudy())) {
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
                this.getMatriculationStatus().add(item);
                for (String newSemester : newItem.getSemesters()) {
                    item.addsemestersItem(newSemester);
                }
            }
        }
    }

}
