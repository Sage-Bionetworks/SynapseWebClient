package org.sagebionetworks.web.shared;

import java.io.Serializable;

import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.table.TableBundle;

/**
 * The transport object for a bundle of entity data.
 * Each object is transported as a string for simple RPC calls.
 * @author John
 *
 */
public class EntityBundleTransport implements Serializable {
	
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
	private UserEntityPermissions permissions;
	private EntityPath entityPath;
	private String entityReferencedByJson;
	private Boolean hasChildren;
	private AccessControlList acl;
	private String accessRequirementsJson;
	private String unmetDownloadAccessRequirementsJson;
	private String fileHandlesJson;
	private TableBundle tableData;
	
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
	public UserEntityPermissions getPermissions() {
		return permissions;
	}
	public void setPermissions(UserEntityPermissions permissions) {
		this.permissions = permissions;
	}
	public EntityPath getEntityPath() {
		return entityPath;
	}
	public void setEntityPath(EntityPath entityPath) {
		this.entityPath = entityPath;
	}	
	public String getEntityReferencedByJson() {
		return entityReferencedByJson;
	}
	public void setEntityReferencedByJson(String entityReferencedByJson) {
		this.entityReferencedByJson = entityReferencedByJson;
	}	
	public AccessControlList getAcl() {
		return acl;
	}
	public void setAcl(AccessControlList acl) {
		this.acl = acl;
	}	
	public String getAccessRequirementsJson() {
		return accessRequirementsJson;
	}
	public void setAccessRequirementsJson(String accessRequirementsJson) {
		this.accessRequirementsJson = accessRequirementsJson;
	}
	public String getUnmetDownloadAccessRequirementsJson() {
		return unmetDownloadAccessRequirementsJson;
	}
	public void setUnmetDownloadAccessRequirementsJson(String unmetDownloadAccessRequirementsJson) {
		this.unmetDownloadAccessRequirementsJson = unmetDownloadAccessRequirementsJson;
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
	
	public TableBundle getTableData() {
		return tableData;
	}
	public void setTableData(TableBundle tableData) {
		this.tableData = tableData;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessRequirementsJson == null) ? 0 : accessRequirementsJson.hashCode());
		result = prime * result + ((acl == null) ? 0 : acl.hashCode());
		result = prime * result + ((annotationsJson == null) ? 0 : annotationsJson.hashCode());
		result = prime * result + ((entityJson == null) ? 0 : entityJson.hashCode());
		result = prime * result + ((entityPath == null) ? 0 : entityPath.hashCode());
		result = prime * result + ((entityReferencedByJson == null) ? 0 : entityReferencedByJson.hashCode());
		result = prime * result + ((fileHandlesJson == null) ? 0 : fileHandlesJson.hashCode());
		result = prime * result + ((hasChildren == null) ? 0 : hasChildren.hashCode());
		result = prime * result + ((isWikiBasedEntity == null) ? 0 : isWikiBasedEntity.hashCode());
		result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
		result = prime * result + ((tableData == null) ? 0 : tableData.hashCode());
		result = prime * result + ((unmetDownloadAccessRequirementsJson == null) ? 0 : unmetDownloadAccessRequirementsJson.hashCode());
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
		if (acl == null) {
			if (other.acl != null)
				return false;
		} else if (!acl.equals(other.acl))
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
		if (entityPath == null) {
			if (other.entityPath != null)
				return false;
		} else if (!entityPath.equals(other.entityPath))
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
		if (permissions == null) {
			if (other.permissions != null)
				return false;
		} else if (!permissions.equals(other.permissions))
			return false;
		if (tableData == null) {
			if (other.tableData != null)
				return false;
		} else if (!tableData.equals(other.tableData))
			return false;
		if (unmetDownloadAccessRequirementsJson == null) {
			if (other.unmetDownloadAccessRequirementsJson != null)
				return false;
		} else if (!unmetDownloadAccessRequirementsJson.equals(other.unmetDownloadAccessRequirementsJson))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "EntityBundleTransport [entityJson=" + entityJson + ", annotationsJson=" + annotationsJson + ", permissions=" + permissions + ", entityPath=" + entityPath + ", entityReferencedByJson=" + entityReferencedByJson + ", hasChildren=" + hasChildren + ", acl=" + acl + ", accessRequirementsJson=" + accessRequirementsJson + ", unmetAccessRequirementsJson=" + unmetDownloadAccessRequirementsJson + ", fileHandlesJson=" + fileHandlesJson + ", tableData=" + tableData + ", isWikiBasedEntity=" + isWikiBasedEntity + "]";
	}

	
	
}
