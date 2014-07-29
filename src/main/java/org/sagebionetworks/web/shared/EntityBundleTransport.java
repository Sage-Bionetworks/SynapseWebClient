package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.EntityBundle;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The transport object for a bundle of entity data.
 * Each object is transported as a string for simple RPC calls.
 * @author John
 *
 */
public class EntityBundleTransport implements IsSerializable {
	
	/**
	 * Masks for requesting what should be included in the bundle.s
	 */
	public static int ENTITY 		      		= EntityBundle.ENTITY;
	public static int ANNOTATIONS	      		= EntityBundle.ANNOTATIONS;
	public static int PERMISSIONS	     		= EntityBundle.PERMISSIONS;
	public static int ENTITY_PATH	      		= EntityBundle.ENTITY_PATH;
	public static int ENTITY_REFERENCEDBY 		= EntityBundle.ENTITY_REFERENCEDBY;
	public static int HAS_CHILDREN				= EntityBundle.HAS_CHILDREN;
	public static int ACL						= EntityBundle.ACL;
	public static int ACCESS_REQUIREMENTS		= EntityBundle.ACCESS_REQUIREMENTS;
	public static int UNMET_ACCESS_REQUIREMENTS	= EntityBundle.UNMET_ACCESS_REQUIREMENTS;
	public static int FILE_HANDLES				= EntityBundle.FILE_HANDLES;
	public static int TABLE_DATA				= EntityBundle.TABLE_DATA;
	
	public static String HELLO = ":)";

	private String entityJson;
	private String annotationsJson;
	private String permissionsJson;
	private String entityPathJson;
	private String entityReferencedByJson;
	private Boolean hasChildren;
	private String aclJson;
	private String accessRequirementsJson;
	private String unmetAccessRequirementsJson;
	private String fileHandlesJson;
	private String tableData;
	
	private Boolean isWikiBasedEntity;
	
	public Boolean getHasChildren() {
		return hasChildren;
	}
	public void setHashChildren(Boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
	public String getEntityJson() {
		return entityJson;
	}
	public void setEntityJson(String entityJson) {
		this.entityJson = entityJson;
	}
	public String getAnnotationsJson() {
		return annotationsJson;
	}
	public void setAnnotationsJson(String annotationsJson) {
		this.annotationsJson = annotationsJson;
	}
	public String getPermissionsJson() {
		return permissionsJson;
	}
	public void setPermissionsJson(String permissionsJson) {
		this.permissionsJson = permissionsJson;
	}
	public String getEntityPathJson() {
		return entityPathJson;
	}
	public void setEntityPathJson(String entityPathJson) {
		this.entityPathJson = entityPathJson;
	}	
	public String getEntityReferencedByJson() {
		return entityReferencedByJson;
	}
	public void setEntityReferencedByJson(String entityReferencedByJson) {
		this.entityReferencedByJson = entityReferencedByJson;
	}	
	public String getAclJson() {
		return aclJson;
	}
	public void setAclJson(String aclJson) {
		this.aclJson = aclJson;
	}	
	public String getAccessRequirementsJson() {
		return accessRequirementsJson;
	}
	public void setAccessRequirementsJson(String accessRequirementsJson) {
		this.accessRequirementsJson = accessRequirementsJson;
	}
	public String getUnmetAccessRequirementsJson() {
		return unmetAccessRequirementsJson;
	}
	public void setUnmetAccessRequirementsJson(String unmetAccessRequirementsJson) {
		this.unmetAccessRequirementsJson = unmetAccessRequirementsJson;
	}
	
	public String getFileHandlesJson() {
		return fileHandlesJson;
	}
	public void setFileHandlesJson(String fileHandlesJson) {
		this.fileHandlesJson = fileHandlesJson;
	}
	
	public Boolean getIsWikiBasedEntity() {
		return isWikiBasedEntity;
	}
	public void setIsWikiBasedEntity(Boolean isWikiBasedEntity) {
		this.isWikiBasedEntity = isWikiBasedEntity;
	}
	
	public String getTableData() {
		return tableData;
	}
	public void setTableData(String tableData) {
		this.tableData = tableData;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((accessRequirementsJson == null) ? 0
						: accessRequirementsJson.hashCode());
		result = prime * result + ((aclJson == null) ? 0 : aclJson.hashCode());
		result = prime * result
				+ ((annotationsJson == null) ? 0 : annotationsJson.hashCode());
		result = prime * result
				+ ((entityJson == null) ? 0 : entityJson.hashCode());
		result = prime * result
				+ ((entityPathJson == null) ? 0 : entityPathJson.hashCode());
		result = prime
				* result
				+ ((entityReferencedByJson == null) ? 0
						: entityReferencedByJson.hashCode());
		result = prime * result
				+ ((fileHandlesJson == null) ? 0 : fileHandlesJson.hashCode());
		result = prime * result
				+ ((hasChildren == null) ? 0 : hasChildren.hashCode());
		result = prime
				* result
				+ ((isWikiBasedEntity == null) ? 0 : isWikiBasedEntity
						.hashCode());
		result = prime * result
				+ ((permissionsJson == null) ? 0 : permissionsJson.hashCode());
		result = prime * result
				+ ((tableData == null) ? 0 : tableData.hashCode());
		result = prime
				* result
				+ ((unmetAccessRequirementsJson == null) ? 0
						: unmetAccessRequirementsJson.hashCode());
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
		EntityBundleTransport other = (EntityBundleTransport) obj;
		if (accessRequirementsJson == null) {
			if (other.accessRequirementsJson != null)
				return false;
		} else if (!accessRequirementsJson.equals(other.accessRequirementsJson))
			return false;
		if (aclJson == null) {
			if (other.aclJson != null)
				return false;
		} else if (!aclJson.equals(other.aclJson))
			return false;
		if (annotationsJson == null) {
			if (other.annotationsJson != null)
				return false;
		} else if (!annotationsJson.equals(other.annotationsJson))
			return false;
		if (entityJson == null) {
			if (other.entityJson != null)
				return false;
		} else if (!entityJson.equals(other.entityJson))
			return false;
		if (entityPathJson == null) {
			if (other.entityPathJson != null)
				return false;
		} else if (!entityPathJson.equals(other.entityPathJson))
			return false;
		if (entityReferencedByJson == null) {
			if (other.entityReferencedByJson != null)
				return false;
		} else if (!entityReferencedByJson.equals(other.entityReferencedByJson))
			return false;
		if (fileHandlesJson == null) {
			if (other.fileHandlesJson != null)
				return false;
		} else if (!fileHandlesJson.equals(other.fileHandlesJson))
			return false;
		if (hasChildren == null) {
			if (other.hasChildren != null)
				return false;
		} else if (!hasChildren.equals(other.hasChildren))
			return false;
		if (isWikiBasedEntity == null) {
			if (other.isWikiBasedEntity != null)
				return false;
		} else if (!isWikiBasedEntity.equals(other.isWikiBasedEntity))
			return false;
		if (permissionsJson == null) {
			if (other.permissionsJson != null)
				return false;
		} else if (!permissionsJson.equals(other.permissionsJson))
			return false;
		if (tableData == null) {
			if (other.tableData != null)
				return false;
		} else if (!tableData.equals(other.tableData))
			return false;
		if (unmetAccessRequirementsJson == null) {
			if (other.unmetAccessRequirementsJson != null)
				return false;
		} else if (!unmetAccessRequirementsJson
				.equals(other.unmetAccessRequirementsJson))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "EntityBundleTransport [entityJson=" + entityJson
				+ ", annotationsJson=" + annotationsJson + ", permissionsJson="
				+ permissionsJson + ", entityPathJson=" + entityPathJson
				+ ", entityReferencedByJson=" + entityReferencedByJson
				+ ", hasChildren=" + hasChildren + ", aclJson=" + aclJson
				+ ", accessRequirementsJson=" + accessRequirementsJson
				+ ", unmetAccessRequirementsJson="
				+ unmetAccessRequirementsJson + ", fileHandlesJson="
				+ fileHandlesJson + ", tableData=" + tableData
				+ ", isWikiBasedEntity=" + isWikiBasedEntity + "]";
	}
	
	
}
