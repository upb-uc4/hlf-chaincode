package de.upb.cs.uc4.chaincode.model.operation;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ApprovalList {

    @SerializedName("users")
    private List<String> users = new ArrayList<>();
    @SerializedName("groups")
    private List<String> groups = new ArrayList<>();

    public ApprovalList users(List<String> users) {
        this.users = users;
        return this;
    }

    public ApprovalList addUsersItem(String usersItem) {
        if (this.users == null) {
            this.users = new ArrayList<String>();
        }
        if (!this.users.contains(usersItem)) {
            this.users.add(usersItem);
        }
        return this;
    }

    /**
     * Get matriculationStatus
     *
     * @return matriculationStatus
     **/
    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public ApprovalList groups(List<String> groups) {
        this.groups = groups;
        return this;
    }

    public ApprovalList addGroupsItem(String groupsItem) {
        if (this.groups == null) {
            this.groups = new ArrayList<String>();
        }
        if (!this.groups.contains(groupsItem)) {
            this.groups.add(groupsItem);
        }
        return this;
    }

    public ApprovalList addGroupsItems(List<String> groupsItems){
        for (String groupsItem : groupsItems){
            this.addGroupsItem(groupsItem);
        }
        return this;
    }

    /**
     * Get matriculationStatus
     *
     * @return matriculationStatus
     **/
    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApprovalList approvalList = (ApprovalList) o;
        return Objects.equals(this.users, approvalList.users) &&
                Objects.equals(this.groups, approvalList.groups);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MatriculationData {\n");
        sb.append("    users: ").append(users).append("\n");
        sb.append("    groups: ").append(groups).append("\n");
        sb.append("}");
        return sb.toString();
    }

    public boolean isEmpty() {
        return this.users.isEmpty() && this.groups.isEmpty();
    }
}
