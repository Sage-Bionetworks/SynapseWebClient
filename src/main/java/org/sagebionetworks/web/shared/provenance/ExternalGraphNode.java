package org.sagebionetworks.web.shared.provenance;

import com.google.gwt.user.client.rpc.IsSerializable;


public class ExternalGraphNode extends ProvGraphNode implements IsSerializable {

	private String id;
	private String name;
	private String url;
	private Boolean wasExecuted;

	public ExternalGraphNode() {}

	public ExternalGraphNode(String id, String name, String url, Boolean wasExecuted) {
		super();
		this.id = id;
		this.name = name;
		this.url = url;
		this.wasExecuted = wasExecuted;
	}

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

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getWasExecuted() {
		return wasExecuted;
	}

	public void setWasExecuted(Boolean wasExecuted) {
		this.wasExecuted = wasExecuted;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		result = prime * result + ((wasExecuted == null) ? 0 : wasExecuted.hashCode());
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
		ExternalGraphNode other = (ExternalGraphNode) obj;
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
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		if (wasExecuted == null) {
			if (other.wasExecuted != null)
				return false;
		} else if (!wasExecuted.equals(other.wasExecuted))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExternalGraphNode [id=" + id + ", name=" + name + ", url=" + url + ", wasExecuted=" + wasExecuted + "]";
	}

}
