package org.sagebionetworks.web.shared.provenance;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ExpandGraphNode extends ProvGraphNode implements IsSerializable {
	private String id;
	private String entityId;
	private Long versionNumber;

	public ExpandGraphNode() {}

	public ExpandGraphNode(String id, String entityId, Long versionNumber) {
		super();
		this.id = id;
		this.entityId = entityId;
		this.versionNumber = versionNumber;
	}

	@Override
	public String getId() {
		return id;
	}

	public String getEntityId() {
		return entityId;
	}

	public Long getVersionNumber() {
		return versionNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((versionNumber == null) ? 0 : versionNumber.hashCode());
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
		ExpandGraphNode other = (ExpandGraphNode) obj;
		if (entityId == null) {
			if (other.entityId != null)
				return false;
		} else if (!entityId.equals(other.entityId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
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
		return "ExpandTreeNode [id=" + id + ", entityId=" + entityId + ", versionNumber=" + versionNumber + "]";
	}



}
