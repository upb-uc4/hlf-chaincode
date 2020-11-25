package de.upb.cs.uc4.chaincode.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DetailedError
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-07-26T19:00:46.792+02:00")



public class SemanticError {
  @SerializedName("type")
  private String type = null;

  @SerializedName("title")
  private String title = null;

  @SerializedName("validationRules")
  private List<ValidationRule> validationRules = null;

  /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(value = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public SemanticError type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get title
   * @return title
  **/
  @ApiModelProperty(value = "")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public SemanticError title(String title) {
    this.title = title;
    return this;
  }

   /**
   * Get invalidParams
   * @return invalidParams
  **/
  @ApiModelProperty(value = "")
  public List<ValidationRule> getValidationRules() {
    return validationRules;
  }

  public void setValidationRules(List<ValidationRule> validationRules) {
    this.validationRules = validationRules;
  }

  public SemanticError validationRules(List<ValidationRule> validationRules) {
    this.validationRules = validationRules;
    return this;
  }

  public SemanticError addValidationRuleItem(ValidationRule validationRuleItem) {
    if (this.validationRules == null) {
      this.validationRules = new ArrayList<ValidationRule>();
    }
    this.validationRules.add(validationRuleItem);
    return this;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SemanticError detailedError = (SemanticError) o;
    return Objects.equals(this.type, detailedError.type) &&
        Objects.equals(this.title, detailedError.title) &&
        Objects.equals(this.validationRules, detailedError.validationRules);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, title, validationRules);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DetailedError {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    invalidParams: ").append(toIndentedString(validationRules)).append("\n");
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

