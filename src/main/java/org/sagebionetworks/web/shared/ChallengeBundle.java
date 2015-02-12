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
}
