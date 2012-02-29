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
	public static int ENTITY 		= 0x1;
	public static int ANNOTATIONS	= 0x2;
	public static int PERMISSIONS	= 0x4;
	public static int ENTITY_PATH	= 0x8;

	private String entityJson;
	private String annotaionsJson;
	private String permissionsJson;
	private String entityPathJson;
	
	public String getEntityJson() {
		return entityJson;
	}
	public void setEntityJson(String entityJson) {
		this.entityJson = entityJson;
	}
	public String getAnnotaionsJson() {
		return annotaionsJson;
	}
	public void setAnnotaionsJson(String annotaionsJson) {
		this.annotaionsJson = annotaionsJson;
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
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((annotaionsJson == null) ? 0 : annotaionsJson.hashCode());
		result = prime * result
				+ ((entityJson == null) ? 0 : entityJson.hashCode());
		result = prime * result
				+ ((entityPathJson == null) ? 0 : entityPathJson.hashCode());
		result = prime * result
				+ ((permissionsJson == null) ? 0 : permissionsJson.hashCode());
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
		if (annotaionsJson == null) {
			if (other.annotaionsJson != null)
				return false;
		} else if (!annotaionsJson.equals(other.annotaionsJson))
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
		if (permissionsJson == null) {
			if (other.permissionsJson != null)
				return false;
		} else if (!permissionsJson.equals(other.permissionsJson))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "EntityBundleTransport [entityJson=" + entityJson
				+ ", annotaionsJson=" + annotaionsJson + ", permissionsJson="
				+ permissionsJson + ", entityPathJson=" + entityPathJson + "]";
	}
	
	
}
