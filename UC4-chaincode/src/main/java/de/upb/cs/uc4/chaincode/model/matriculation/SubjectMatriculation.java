package de.upb.cs.uc4.chaincode.model.matriculation;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubjectMatriculation {

    @SerializedName("fieldOfStudy")
    private String fieldOfStudy = null;
    @SerializedName("semesters")
    private List<String> semesters = null;

    public SubjectMatriculation fieldOfStudy(String fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
        return this;
    }

    /**
     * Get fieldOfStudy
     *
     * @return fieldOfStudy
     **/
    public String getFieldOfStudy() {
        return fieldOfStudy;
    }

    public void setFieldOfStudy(String fieldOfStudy) {
        this.fieldOfStudy = fieldOfStudy;
    }

    public SubjectMatriculation semesters(List<String> semesters) {
        this.semesters = semesters;
        return this;
    }

    public SubjectMatriculation addsemestersItem(String semestersItem) {
        if (this.semesters == null) {
            this.semesters = new ArrayList<String>();
        }
        this.semesters.add(semestersItem);
        return this;
    }

    /**
     * Get semesters
     *
     * @return semesters
     **/
    public List<String> getSemesters() {
        return semesters;
    }

    public void setSemesters(List<String> semesters) {
        this.semesters = semesters;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubjectMatriculation subjectImmatriculationInterval = (SubjectMatriculation) o;
        return Objects.equals(this.fieldOfStudy, subjectImmatriculationInterval.fieldOfStudy) &&
                Objects.equals(this.semesters, subjectImmatriculationInterval.semesters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SubjectImmatriculationInterval {\n");
        sb.append("    fieldOfStudy: ").append(fieldOfStudy).append("\n");
        sb.append("    semesters: ").append(semesters).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
