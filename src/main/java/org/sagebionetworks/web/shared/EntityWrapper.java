package org.sagebionetworks.web.shared;

import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.IsSerializable;

class EntityWrapper implements IsSerializable {

	private String entityJson;
	private String entityClassName;

	public String getEntityJson() {
		return entityJson;
	}
	
	public String getEntityClassName() {
		return entityClassName;
	}

	/**
	 * This should only be used for RPC
	 */
	public EntityWrapper(){
		
	}
	public EntityWrapper(String entityJson,	String entityClassName) {
		super();
		if(entityJson == null) throw new IllegalArgumentException("Json string cannot be null");
		if(entityClassName == null) throw new IllegalArgumentException("EntityClassName cannot be null");
		this.entityJson = entityJson;
		this.entityClassName = entityClassName;
	}

	public void setEntityJson(String entityJson) {
		this.entityJson = entityJson;
	}

	public void setEntityClassName(String entityClassName) {
		this.entityClassName = entityClassName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((entityClassName == null) ? 0 : entityClassName.hashCode());
		result = prime * result
				+ ((entityJson == null) ? 0 : entityJson.hashCode());
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
		EntityWrapper other = (EntityWrapper) obj;
		if (entityClassName == null) {
			if (other.entityClassName != null)
				return false;
		} else if (!entityClassName.equals(other.entityClassName))
			return false;
		if (entityJson == null) {
			if (other.entityJson != null)
				return false;
		} else if (!entityJson.equals(other.entityJson))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EntityWrapper [entityJson=" + entityJson + ", entityClassName="
				+ entityClassName + "]";
	}


}
