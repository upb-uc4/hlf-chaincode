package de.upb.cs.uc4.chaincode.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * MatriculationData
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-07-26T19:00:46.792+02:00")



public class Approval {
  @SerializedName("id")
  private String id = null;

  public Approval id(String id) {
    this.id = id;
    return this;
  }

  @ApiModelProperty(value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @SerializedName("type")
  private String type = null;

  public Approval type(String type) {
    this.type = type;
    return this;
  }

  @ApiModelProperty(value = "")
  public String getType() {
    return type;
  }

  public void setIType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Approval matriculationData = (Approval) o;
    return Objects.equals(this.id, matriculationData.id) &&
        Objects.equals(this.type, matriculationData.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MatriculationData {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

