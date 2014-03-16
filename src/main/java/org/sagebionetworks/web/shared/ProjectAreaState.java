package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.EntityHeader;

public class ProjectAreaState {

	private String projectId;
	private String lastWikiSubToken;
	private EntityHeader lastFileAreaEntity;
	private EntityHeader lastTableAreaEntity;

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getLastWikiSubToken() {
		return lastWikiSubToken;
	}

	public void setLastWikiSubToken(String lastWikiSubToken) {
		this.lastWikiSubToken = lastWikiSubToken;
	}

	public EntityHeader getLastFileAreaEntity() {
		return lastFileAreaEntity;
	}

	public void setLastFileAreaEntity(EntityHeader lastFileEntity) {
		this.lastFileAreaEntity = lastFileEntity;
	}

	public EntityHeader getLastTableAreaEntity() {
		return lastTableAreaEntity;
	}

	public void setLastTableAreaEntity(EntityHeader lastTableAreaEntity) {
		this.lastTableAreaEntity = lastTableAreaEntity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((lastFileAreaEntity == null) ? 0 : lastFileAreaEntity
						.hashCode());
		result = prime
				* result
				+ ((lastTableAreaEntity == null) ? 0 : lastTableAreaEntity
						.hashCode());
		result = prime
				* result
				+ ((lastWikiSubToken == null) ? 0 : lastWikiSubToken.hashCode());
		result = prime * result
				+ ((projectId == null) ? 0 : projectId.hashCode());
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
		ProjectAreaState other = (ProjectAreaState) obj;
		if (lastFileAreaEntity == null) {
			if (other.lastFileAreaEntity != null)
				return false;
		} else if (!lastFileAreaEntity.equals(other.lastFileAreaEntity))
			return false;
		if (lastTableAreaEntity == null) {
			if (other.lastTableAreaEntity != null)
				return false;
		} else if (!lastTableAreaEntity.equals(other.lastTableAreaEntity))
			return false;
		if (lastWikiSubToken == null) {
			if (other.lastWikiSubToken != null)
				return false;
		} else if (!lastWikiSubToken.equals(other.lastWikiSubToken))
			return false;
		if (projectId == null) {
			if (other.projectId != null)
				return false;
		} else if (!projectId.equals(other.projectId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProjectAreaState [projectId=" + projectId
				+ ", lastWikiSubToken=" + lastWikiSubToken
				+ ", lastFileAreaEntity=" + lastFileAreaEntity
				+ ", lastTableAreaEntity=" + lastTableAreaEntity + "]";
	}
	
}
