package org.sagebionetworks.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class FormParams implements IsSerializable {

	private String containerSynId, jsonSchemaSynId, uiSchemaSynId;

	public FormParams(String containerSynId, String jsonSchemaSynId, String uiSchemaSynId) {
		super();
		this.containerSynId = containerSynId;
		this.jsonSchemaSynId = jsonSchemaSynId;
		this.uiSchemaSynId = uiSchemaSynId;
	}

	public String getContainerSynId() {
		return containerSynId;
	}

	public String getJsonSchemaSynId() {
		return jsonSchemaSynId;
	}

	public String getUiSchemaSynId() {
		return uiSchemaSynId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((containerSynId == null) ? 0 : containerSynId.hashCode());
		result = prime * result + ((jsonSchemaSynId == null) ? 0 : jsonSchemaSynId.hashCode());
		result = prime * result + ((uiSchemaSynId == null) ? 0 : uiSchemaSynId.hashCode());
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
		FormParams other = (FormParams) obj;
		if (containerSynId == null) {
			if (other.containerSynId != null)
				return false;
		} else if (!containerSynId.equals(other.containerSynId))
			return false;
		if (jsonSchemaSynId == null) {
			if (other.jsonSchemaSynId != null)
				return false;
		} else if (!jsonSchemaSynId.equals(other.jsonSchemaSynId))
			return false;
		if (uiSchemaSynId == null) {
			if (other.uiSchemaSynId != null)
				return false;
		} else if (!uiSchemaSynId.equals(other.uiSchemaSynId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FormParams [containerSynId=" + containerSynId + ", jsonSchemaSynId=" + jsonSchemaSynId + ", uiSchemaSynId=" + uiSchemaSynId + "]";
	}

}
