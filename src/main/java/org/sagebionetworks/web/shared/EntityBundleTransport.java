package org.sagebionetworks.web.shared;

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
	public static int ENTITY 		      	= 0x1;
	public static int ANNOTATIONS	      	= 0x2;
	public static int PERMISSIONS	     	= 0x4;
	public static int ENTITY_PATH	      	= 0x8;
	public static int ENTITY_REFERENCEDBY 	= 0x10;
	public static int CHILD_COUNT			= 0x20;
	public static int ACL					= 0x40;
	public static int USERS					= 0x80;
	public static int GROUPS				= 0x100;
	
	public static String HELLO = ":)";

	private String entityJson;
	private String annotationsJson;
	private String permissionsJson;
	private String entityPathJson;
	private String entityReferencedByJson;
	private Long childCount;
	private String aclJson;
	private String usersJson;
	private String groupsJson;
	
	public Long getChildCount() {
		return childCount;
	}
	public void setChildCount(Long childCount) {
		this.childCount = childCount;
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
	public String getUsersJson() {
		return usersJson;
	}
	public void setUsersJson(String usersJson) {
		this.usersJson = usersJson;
	}
	public String getGroupsJson() {
		return groupsJson;
	}
	public void setGroupsJson(String groupsJson) {
		this.groupsJson = groupsJson;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((groupsJson == null) ? 0 : groupsJson.hashCode());
		result = prime * result + ((aclJson == null) ? 0 : aclJson.hashCode());
		result = prime * result
				+ ((annotationsJson == null) ? 0 : annotationsJson.hashCode());
		result = prime * result
				+ ((childCount == null) ? 0 : childCount.hashCode());
		result = prime * result
				+ ((entityJson == null) ? 0 : entityJson.hashCode());
		result = prime * result
				+ ((entityPathJson == null) ? 0 : entityPathJson.hashCode());
		result = prime
				* result
				+ ((entityReferencedByJson == null) ? 0
						: entityReferencedByJson.hashCode());
		result = prime * result
				+ ((permissionsJson == null) ? 0 : permissionsJson.hashCode());
		result = prime * result
				+ ((usersJson == null) ? 0 : usersJson.hashCode());
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
		if (groupsJson == null) {
			if (other.groupsJson != null)
				return false;
		} else if (!groupsJson.equals(other.groupsJson))
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
		if (childCount == null) {
			if (other.childCount != null)
				return false;
		} else if (!childCount.equals(other.childCount))
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
		if (permissionsJson == null) {
			if (other.permissionsJson != null)
				return false;
		} else if (!permissionsJson.equals(other.permissionsJson))
			return false;
		if (usersJson == null) {
			if (other.usersJson != null)
				return false;
		} else if (!usersJson.equals(other.usersJson))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "EntityBundleTransport [entityJson=" + entityJson
				+ ", annotationsJson=" + annotationsJson + ", permissionsJson="
				+ permissionsJson + ", entityPathJson=" + entityPathJson
				+ ", entityReferencedByJson=" + entityReferencedByJson
				+ ", childCount=" + childCount + ", aclJson=" + aclJson
				+ ", usersJson=" + usersJson + ", GroupsJson=" + groupsJson
				+ "]";
	}
	
	
}
