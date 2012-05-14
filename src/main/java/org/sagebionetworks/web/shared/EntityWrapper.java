package org.sagebionetworks.web.shared;

import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.IsSerializable;

public class EntityWrapper implements IsSerializable {

	private String entityJson;
	private RestServiceException restServiceException;
	private String entityClassName;

	public String getEntityJson() {
		return entityJson;
	}

	public RestServiceException getRestServiceException() {
		return restServiceException;
	}

	public String getEntityClassName() {
		return entityClassName;
	}

	/**
	 * This should only be used for RPC
	 */
	public EntityWrapper(){
		
	}
	public EntityWrapper(String entityJson,	String entityClassName, RestServiceException restServiceException) {
		super();
		if(restServiceException == null){
			// When the exception is null, then both the json and class name cannot be null
			if(entityJson == null) throw new IllegalArgumentException("Json string cannot be null");
			if(entityClassName == null) throw new IllegalArgumentException("EntityClassName cannot be null");
		}
		this.entityJson = entityJson;
		this.restServiceException = restServiceException;
		this.entityClassName = entityClassName;
	}

	public void setEntityJson(String entityJson) {
		this.entityJson = entityJson;
	}

	public void setRestServiceException(RestServiceException restServiceException) {
		this.restServiceException = restServiceException;
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
		result = prime
				* result
				+ ((restServiceException == null) ? 0 : restServiceException
						.hashCode());
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
		if (restServiceException == null) {
			if (other.restServiceException != null)
				return false;
		} else if (!restServiceException.equals(other.restServiceException))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EntityWrapper [entityJson=" + entityJson
				+ ", restServiceException=" + restServiceException
				+ ", entityClassName=" + entityClassName + "]";
	}

}
