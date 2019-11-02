package org.sagebionetworks.web.shared;

import org.sagebionetworks.repo.model.ChallengeTeam;
import com.google.gwt.user.client.rpc.IsSerializable;

public class ChallengeTeamBundle implements IsSerializable {

	private ChallengeTeam challengeTeam;
	private boolean isAdmin;

	public ChallengeTeamBundle() {}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((challengeTeam == null) ? 0 : challengeTeam.hashCode());
		result = prime * result + (isAdmin ? 1231 : 1237);
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
		ChallengeTeamBundle other = (ChallengeTeamBundle) obj;
		if (challengeTeam == null) {
			if (other.challengeTeam != null)
				return false;
		} else if (!challengeTeam.equals(other.challengeTeam))
			return false;
		if (isAdmin != other.isAdmin)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ChallengeTeamBundle [challengeTeam=" + challengeTeam + ", isAdmin=" + isAdmin + "]";
	}
}
