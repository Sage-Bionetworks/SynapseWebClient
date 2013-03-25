package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Set;

/**
 * APITableColumnConfig Package information relating to a column in the API
 * Table (supertable)
 */
public class APITableColumnConfig {
	
	/**
	 * Display Column Name
	 * 
	 */
	private String displayColumnName;
	
	/**
	 * Input Column Names
	 * 
	 */
	private Set<String> inputColumnNames;
	
	/**
	 * Renderer Name
	 * 
	 */
	private String rendererName;

	public APITableColumnConfig() {
	}

	/**
	 * Display Column Name
	 * 
	 * Display name for this column (if applicable)
	 * 
	 * @return displayColumnName
	 */
	public String getDisplayColumnName() {
		return displayColumnName;
	}

	/**
	 * Display Column Name
	 * 
	 * Display name for this column (if applicable)
	 * 
	 * @param displayColumnName
	 */
	public void setDisplayColumnName(String displayColumnName) {
		this.displayColumnName = displayColumnName;
	}

	/**
	 * Input Column Names
	 * 
	 * The list columns needed as input to produce this set of output columns.
	 * 
	 * 
	 * 
	 * @return inputColumnNames
	 */
	public Set<String> getInputColumnNames() {
		return inputColumnNames;
	}

	/**
	 * Input Column Names
	 * 
	 * The list columns needed as input to produce this set of output columns.
	 * 
	 * 
	 * 
	 * @param inputColumnNames
	 */
	public void setInputColumnNames(Set<String> inputColumnNames) {
		this.inputColumnNames = inputColumnNames;
	}

	/**
	 * Renderer Name
	 * 
	 * Renderer that should be used to generate the output columns.
	 * 
	 * 
	 * 
	 * @return rendererName
	 */
	public String getRendererName() {
		return rendererName;
	}

	/**
	 * Renderer Name
	 * 
	 * Renderer that should be used to generate the output columns.
	 * 
	 * 
	 * 
	 * @param rendererName
	 */
	public void setRendererName(String rendererName) {
		this.rendererName = rendererName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = ((prime * result) + ((displayColumnName == null) ? 0
				: displayColumnName.hashCode()));
		result = ((prime * result) + ((inputColumnNames == null) ? 0
				: inputColumnNames.hashCode()));
		result = ((prime * result) + ((rendererName == null) ? 0 : rendererName
				.hashCode()));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		APITableColumnConfig other = ((APITableColumnConfig) obj);
		if (displayColumnName == null) {
			if (other.displayColumnName != null) {
				return false;
			}
		} else {
			if (!displayColumnName.equals(other.displayColumnName)) {
				return false;
			}
		}
		if (inputColumnNames == null) {
			if (other.inputColumnNames != null) {
				return false;
			}
		} else {
			if (!inputColumnNames.equals(other.inputColumnNames)) {
				return false;
			}
		}
		if (rendererName == null) {
			if (other.rendererName != null) {
				return false;
			}
		} else {
			if (!rendererName.equals(other.rendererName)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder result;
		result = new StringBuilder();
		result.append("");
		result.append("org.sagebionetworks.repo.model.widget.APITableColumnConfig");
		result.append(" [");
		result.append("displayColumnName=");
		result.append(displayColumnName);
		result.append(" ");
		result.append("inputColumnNames=");
		result.append(inputColumnNames);
		result.append(" ");
		result.append("rendererName=");
		result.append(rendererName);
		result.append(" ");
		result.append("]");
		return result.toString();
	}

}
