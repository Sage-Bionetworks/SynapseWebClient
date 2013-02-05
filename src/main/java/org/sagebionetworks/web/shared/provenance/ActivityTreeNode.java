package org.sagebionetworks.web.shared.provenance;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ActivityTreeNode extends ProvTreeNode implements IsSerializable {

	private String id;
	private String activityId;
	private String activityName;
	private ActivityType type;
	private String subEntityId;
	private String subName;
	private String subVersionLabel;
	private Long subVersionNumber;
	private String subEntityType;

	public ActivityTreeNode() {	}
	
	public ActivityTreeNode(String id, String activityId, String activityName, ActivityType type) {
		super();
		this.id = id;
		this.activityId = activityId;
		this.activityName = activityName;
		this.type = type;
	}

	public ActivityTreeNode(String id, String activityId, String activityName, ActivityType type, String subEntityId, String subName,
			String subVersionLabel, Long subVersionNumber, String subEntityType) {
		super();
		this.id = id;
		this.activityId = activityId;
		this.activityName = activityName;
		this.type = type;
		this.subEntityId = subEntityId;
		this.subName = subName;
		this.subVersionLabel = subVersionLabel;
		this.subVersionNumber = subVersionNumber;
		this.subEntityType = subEntityType;
	}

	@Override
	public String getId() {
		return id;
	}
	
	public String getActivityId() {
		return activityId;
	}

	public String getActivityName() {
		return activityName;
	}
	
	public ActivityType getType() {
		return type;
	}

	public String getSubEntityId() {
		return subEntityId;
	}
	
	public String getSubName() {
		return subName;
	}

	public String getSubVersionLabel() {
		return subVersionLabel;
	}

	public Long getSubVersionNumber() {
		return subVersionNumber;
	}

	public String getSubEntityType() {
		return subEntityType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((activityId == null) ? 0 : activityId.hashCode());
		result = prime * result
				+ ((activityName == null) ? 0 : activityName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((subEntityId == null) ? 0 : subEntityId.hashCode());
		result = prime * result
				+ ((subEntityType == null) ? 0 : subEntityType.hashCode());
		result = prime * result + ((subName == null) ? 0 : subName.hashCode());
		result = prime * result
				+ ((subVersionLabel == null) ? 0 : subVersionLabel.hashCode());
		result = prime
				* result
				+ ((subVersionNumber == null) ? 0 : subVersionNumber.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActivityTreeNode other = (ActivityTreeNode) obj;
		if (activityId == null) {
			if (other.activityId != null)
				return false;
		} else if (!activityId.equals(other.activityId))
			return false;
		if (activityName == null) {
			if (other.activityName != null)
				return false;
		} else if (!activityName.equals(other.activityName))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (subEntityId == null) {
			if (other.subEntityId != null)
				return false;
		} else if (!subEntityId.equals(other.subEntityId))
			return false;
		if (subEntityType == null) {
			if (other.subEntityType != null)
				return false;
		} else if (!subEntityType.equals(other.subEntityType))
			return false;
		if (subName == null) {
			if (other.subName != null)
				return false;
		} else if (!subName.equals(other.subName))
			return false;
		if (subVersionLabel == null) {
			if (other.subVersionLabel != null)
				return false;
		} else if (!subVersionLabel.equals(other.subVersionLabel))
			return false;
		if (subVersionNumber == null) {
			if (other.subVersionNumber != null)
				return false;
		} else if (!subVersionNumber.equals(other.subVersionNumber))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActivityTreeNode [id=" + id + ", activityId=" + activityId
				+ ", activityName=" + activityName + ", type=" + type
				+ ", subEntityId=" + subEntityId + ", subName=" + subName
				+ ", subVersionLabel=" + subVersionLabel
				+ ", subVersionNumber=" + subVersionNumber + ", subEntityType="
				+ subEntityType + "]";
	}


}
