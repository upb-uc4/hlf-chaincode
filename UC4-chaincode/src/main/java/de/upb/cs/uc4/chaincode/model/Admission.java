package de.upb.cs.uc4.chaincode.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

public class Admission {
  private static String DELIMITER = ":";

  @SerializedName("admissionId")
  private String admissionId;

  @SerializedName("enrollmentId")
  private String enrollmentId;

  @SerializedName("courseId")
  private String courseId;

  @SerializedName("moduleId")
  private String moduleId;

  @SerializedName("timestamp")
  private String timestamp;

  public Admission(String enrollmentId, String courseId, String moduleId, String timestamp) {
    this.enrollmentId = enrollmentId;
    this.courseId = courseId;
    this.moduleId = moduleId;
    this.timestamp = timestamp;
    this.admissionId = enrollmentId + Admission.DELIMITER + courseId;
  }

  /**
   * Get admissionId
   * @return admissionId
   **/
  @ApiModelProperty()
  public String getAdmissionId() {
    return this.admissionId;
  }

  public void setAdmissionId(String admissionId) {
    this.admissionId = admissionId;
  }

  /**
   * Get enrollmentId
   * @return enrollmentId
   **/
  @ApiModelProperty()
  public String getEnrollmentId() {
    return this.enrollmentId;
  }

  public void setEnrollmentId(String enrollmentId) {
    this.enrollmentId = enrollmentId;
  }

  /**
   * Get courseId
   * @return courseId
   **/
  @ApiModelProperty()
  public String getCourseId() {
    return this.courseId;
  }

  public void setCourseId(String courseId) {
    this.courseId = courseId;
  }

  /**
   * Get moduleId
   * @return moduleId
   **/
  @ApiModelProperty()
  public String getModuleId() {
    return this.moduleId;
  }

  public void setModuleId(String moduleId) {
    this.moduleId = moduleId;
  }

  /**
   * Get timestamp
   * @return timestamp
   **/
  @ApiModelProperty()
  public String getTimestamp() {
    return this.timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Admission other = (Admission) o;
    return Objects.equals(this.hashCode(), other.hashCode());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.enrollmentId, this.courseId, this.moduleId, this.timestamp);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Admission {\n");
    sb.append("    enrollmentId: ").append(toIndentedString(this.enrollmentId)).append("\n");
    sb.append("    courseId: ").append(toIndentedString(this.courseId)).append("\n");
    sb.append("    moduleId: ").append(toIndentedString(this.moduleId)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(this.timestamp)).append("\n");
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

