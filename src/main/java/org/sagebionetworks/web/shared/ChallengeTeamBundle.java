package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.ChallengeTeam;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ChallengeTeamBundle implements IsSerializable{
	
	private ChallengeTeam challengeTeam;
	private boolean isAdmin;
	public ChallengeTeamBundle(ChallengeTeam challengeTeam, boolean isAdmin) {
		super();
		this.challengeTeam = challengeTeam;
		this.isAdmin = isAdmin;
	}
	public ChallengeTeam getChallengeTeam() {
		return challengeTeam;
	}
	public void setChallengeTeam(ChallengeTeam challengeTeam) {
		this.challengeTeam = challengeTeam;
	}
	public boolean isAdmin() {
		return isAdmin;
	}
	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	
	
}
