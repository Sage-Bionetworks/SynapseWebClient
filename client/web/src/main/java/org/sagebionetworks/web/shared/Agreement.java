package org.sagebionetworks.web.shared;

import java.util.Date;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This is a data transfer object that will be populated from REST JSON.
 * 
 */
public class Agreement implements IsSerializable {
	private String name;	
	private String annotations;
	private String id;
	private Date creationDate;
	private String etag;
	private String createdBy;
	private String uri;
	private String accessControlList;

	public Agreement() {		
	}
	
	/**
	 * Default constructor is required
	 * @param obj JSONObject of the Project object
	 */
	public Agreement(JSONObject object) {
		String key = null; 

		key = "name";
		if(object.containsKey(key)) 
			if(object.get(key).isString() != null)
				setName(object.get(key).isString().stringValue());		
				
		key = "annotations";
		if(object.containsKey(key)) 
			if(object.get(key).isString() != null)
				setAnnotations(object.get(key).isString().stringValue());		
		
		key = "id";
		if(object.containsKey(key)) 
			if(object.get(key).isString() != null)
				setId(object.get(key).isString().stringValue());		
				
		key = "creationDate";
		if(object.containsKey(key)) 
			if(object.get(key).isNumber() != null)
				setCreationDate(new Date(new Double(object.get(key).isNumber().doubleValue()).longValue()));

		key = "etag";
		if(object.containsKey(key)) 
			if(object.get(key).isString() != null)
				setEtag(object.get(key).isString().stringValue());		
		
		key = "createdBy";
		if(object.containsKey(key)) 
			if(object.get(key).isString() != null)
				setCreatedBy(object.get(key).isString().stringValue());		
		
		key = "uri";
		if(object.containsKey(key)) 
			if(object.get(key).isString() != null)
				setUri(object.get(key).isString().stringValue());

		key = "accessControlList";
		if(object.containsKey(key)) 
			if(object.get(key).isString() != null)
				setAccessControlList(object.get(key).isString().stringValue());					
	}

	public String toJson() {
		JSONObject object = new JSONObject();
		
		object.put("name", new JSONString(getName()));
		object.put("annotations", new JSONString(getAnnotations()));
		object.put("id", new JSONString(getId()));
		object.put("creationDate", new JSONNumber(getCreationDate().getTime()));
		object.put("etag", new JSONString(getEtag()));
		object.put("createdBy", new JSONString(getCreatedBy()));
		object.put("uri", new JSONString(getUri()));
		object.put("accessControlList", new JSONString(getAccessControlList()));
		
		return object.toString();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAnnotations() {
		return annotations;
	}

	public void setAnnotations(String annotations) {
		this.annotations = annotations;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getEtag() {
		return etag;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getAccessControlList() {
		return accessControlList;
	}

	public void setAccessControlList(String accessControlList) {
		this.accessControlList = accessControlList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((accessControlList == null) ? 0 : accessControlList
						.hashCode());
		result = prime * result
				+ ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result
				+ ((createdBy == null) ? 0 : createdBy.hashCode());
		result = prime * result
				+ ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((etag == null) ? 0 : etag.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		Agreement other = (Agreement) obj;
		if (accessControlList == null) {
			if (other.accessControlList != null)
				return false;
		} else if (!accessControlList.equals(other.accessControlList))
			return false;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		if (createdBy == null) {
			if (other.createdBy != null)
				return false;
		} else if (!createdBy.equals(other.createdBy))
			return false;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (etag == null) {
			if (other.etag != null)
				return false;
		} else if (!etag.equals(other.etag))
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
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Agreement [name=" + name + ", annotations=" + annotations
				+ ", id=" + id + ", creationDate=" + creationDate + ", etag="
				+ etag + ", createdBy=" + createdBy + ", uri=" + uri
				+ ", accessControlList=" + accessControlList + "]";
	}

}
