package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.Challenge;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ChallengeBundle implements IsSerializable{
	
	private Challenge challenge;
	private String projectName;
	
	public ChallengeBundle() {
	}
	
	public ChallengeBundle(Challenge challenge, String projectName) {
		super();
		this.challenge = challenge;
		this.projectName = projectName;
	}
	public Challenge getChallenge() {
		return challenge;
	}
	public void setChallenge(Challenge challenge) {
		this.challenge = challenge;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((challenge == null) ? 0 : challenge.hashCode());
		result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
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
		ChallengeBundle other = (ChallengeBundle) obj;
		if (challenge == null) {
			if (other.challenge != null)
				return false;
		} else if (!challenge.equals(other.challenge))
			return false;
		if (projectName == null) {
			if (other.projectName != null)
				return false;
		} else if (!projectName.equals(other.projectName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChallengeBundle [challenge=" + challenge + ", projectName=" + projectName + "]";
	}
}
