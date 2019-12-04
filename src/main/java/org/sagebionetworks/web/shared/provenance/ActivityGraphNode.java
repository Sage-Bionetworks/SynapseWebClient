package org.sagebionetworks.web.shared.provenance;

import java.util.Date;
import com.google.gwt.user.client.rpc.IsSerializable;

public class ActivityGraphNode extends ProvGraphNode implements IsSerializable {

	private String id;
	private String activityId;
	private String activityName;
	private ActivityType type;
	private String modifiedBy;
	private Date modifiedOn;

	public ActivityGraphNode() {}

	public ActivityGraphNode(String id, String activityId, String activityName, ActivityType type, String modifiedBy, Date modifiedOn, Boolean startingNode) {
		super();
		this.id = id;
		this.activityId = activityId;
		this.activityName = activityName;
		this.type = type;
		this.modifiedBy = modifiedBy;
		this.modifiedOn = modifiedOn;
		this.setStartingNode(startingNode);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public ActivityType getType() {
		return type;
	}

	public void setType(ActivityType type) {
		this.type = type;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((activityId == null) ? 0 : activityId.hashCode());
		result = prime * result + ((activityName == null) ? 0 : activityName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((modifiedBy == null) ? 0 : modifiedBy.hashCode());
		result = prime * result + ((modifiedOn == null) ? 0 : modifiedOn.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActivityGraphNode other = (ActivityGraphNode) obj;
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
		if (modifiedBy == null) {
			if (other.modifiedBy != null)
				return false;
		} else if (!modifiedBy.equals(other.modifiedBy))
			return false;
		if (modifiedOn == null) {
			if (other.modifiedOn != null)
				return false;
		} else if (!modifiedOn.equals(other.modifiedOn))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActivityGraphNode [id=" + id + ", activityId=" + activityId + ", activityName=" + activityName + ", type=" + type + ", modifiedBy=" + modifiedBy + ", modifiedOn=" + modifiedOn + "]";
	}

}
