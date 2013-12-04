package org.sagebionetworks.web.shared;

import java.util.List;

public class TableObject {

	String id;
	String name;
	String createdByPrincipalId;
	List<String> columnIds;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCreatedByPrincipalId() {
		return createdByPrincipalId;
	}
	public void setCreatedByPrincipalId(String createdByPrincipalId) {
		this.createdByPrincipalId = createdByPrincipalId;
	}
	public List<String> getColumnIds() {
		return columnIds;
	}
	public void setColumnIds(List<String> columnIds) {
		this.columnIds = columnIds;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((columnIds == null) ? 0 : columnIds.hashCode());
		result = prime
				* result
				+ ((createdByPrincipalId == null) ? 0 : createdByPrincipalId
						.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		TableObject other = (TableObject) obj;
		if (columnIds == null) {
			if (other.columnIds != null)
				return false;
		} else if (!columnIds.equals(other.columnIds))
			return false;
		if (createdByPrincipalId == null) {
			if (other.createdByPrincipalId != null)
				return false;
		} else if (!createdByPrincipalId.equals(other.createdByPrincipalId))
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
		return true;
	}
	@Override
	public String toString() {
		return "TableObject [id=" + id + ", name=" + name
				+ ", createdByPrincipalId=" + createdByPrincipalId
				+ ", columnIds=" + columnIds + "]";
	}
	
}
