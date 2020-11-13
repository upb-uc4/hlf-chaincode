package de.upb.cs.uc4.chaincode.model;

import java.util.Objects;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * SubjectImmatriculationInterval
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-07-26T19:00:46.792+02:00")



public class SubjectMatriculation {

  @SerializedName("fieldOfStudy")
  private String fieldOfStudy = null;

  public SubjectMatriculation fieldOfStudy(String fieldOfStudy) {
    this.fieldOfStudy = fieldOfStudy;
    return this;
  }

  @SerializedName("semesters")
  private List<String> semesters = null;

   /**
   * Get fieldOfStudy
   * @return fieldOfStudy
  **/
  /**
   * Get name
   * @return name
   **/
  @ApiModelProperty(value = "")
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
   * @return semesters
  **/
  @ApiModelProperty(value = "")
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
  public int hashCode() {
    return Objects.hash(fieldOfStudy, semesters);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubjectImmatriculationInterval {\n");
    
    sb.append("    fieldOfStudy: ").append(toIndentedString(fieldOfStudy)).append("\n");
    sb.append("    semesters: ").append(toIndentedString(semesters)).append("\n");
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

}

