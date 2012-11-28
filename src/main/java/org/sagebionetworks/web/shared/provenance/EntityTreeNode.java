package org.sagebionetworks.web.shared.provenance;

import com.google.gwt.user.client.rpc.IsSerializable;


public class EntityTreeNode extends ProvTreeNode implements IsSerializable {

	private String id;
	private String entityId;
	private String name;
	private String versionLabel;
	private Long versionNumber;
	private String entityType;
	
	public EntityTreeNode() { }
	
	public EntityTreeNode(String id, String entityId, String name, String versionLabel,
			Long versionNumber, String entityType) {
		super();
		this.id = id;
		this.entityId = entityId;
		this.name = name;
		this.versionLabel = versionLabel;
		this.versionNumber = versionNumber;
		this.entityType = entityType;
	}
	
	@Override
	public String getId() {
		return id;
	}
	public String getEntityId() {
		return entityId;
	}
	public String getName() {
		return name;
	}
	public String getVersionLabel() {
		return versionLabel;
	}
	public Long getVersionNumber() {
		return versionNumber;
	}
	public String getEntityType() {
		return entityType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((entityId == null) ? 0 : entityId.hashCode());
		result = prime * result
				+ ((entityType == null) ? 0 : entityType.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((versionLabel == null) ? 0 : versionLabel.hashCode());
		result = prime * result
				+ ((versionNumber == null) ? 0 : versionNumber.hashCode());
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
		EntityTreeNode other = (EntityTreeNode) obj;
		if (entityId == null) {
			if (other.entityId != null)
				return false;
		} else if (!entityId.equals(other.entityId))
			return false;
		if (entityType == null) {
			if (other.entityType != null)
				return false;
		} else if (!entityType.equals(other.entityType))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (versionLabel == null) {
			if (other.versionLabel != null)
				return false;
		} else if (!versionLabel.equals(other.versionLabel))
			return false;
		if (versionNumber == null) {
			if (other.versionNumber != null)
				return false;
		} else if (!versionNumber.equals(other.versionNumber))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EntityTreeNode [id=" + id + ", entityId=" + entityId
				+ ", name=" + name + ", versionLabel=" + versionLabel
				+ ", versionNumber=" + versionNumber + ", entityType="
				+ entityType + "]";
	}
	
}
