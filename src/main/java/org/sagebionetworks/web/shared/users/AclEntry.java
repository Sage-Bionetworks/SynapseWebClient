package org.sagebionetworks.web.shared.users;

import java.util.Set;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.UserProfile;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AclEntry implements IsSerializable {

	private Team team;
	private UserProfile profile;
	private Set<ACCESS_TYPE> accessTypes;
	private boolean isOwner, isIndividual;
	private String ownerId, displayName;
	
	public AclEntry() {}

	/**
	 * Create a new ACLEntry.  If no Team or UserProfile is available, at the very least provide a display name to use (used for groups that have no Teams, for example)
	 * @param ownerId
	 * @param accessTypes
	 * @param isOwner
	 * @param displayName
	 */
	public AclEntry(String ownerId, Set<ACCESS_TYPE> accessTypes, boolean isOwner, String displayName, boolean isIndividual) {
		this(ownerId, accessTypes, isOwner, null, null, displayName, isIndividual);
	}

	/**
	 * Create a new ACLEntry.  Give a Team to help rendering of the principal
	 * @param ownerId
	 * @param accessTypes
	 * @param isOwner
	 * @param team
	 */
	public AclEntry(String ownerId, Set<ACCESS_TYPE> accessTypes, boolean isOwner, Team team) {
		this(ownerId, accessTypes, isOwner, null, team, null, false);
	}

	/**
	 * Create a new ACLEntry.  Give a UserProfile to help rendering of the principal
	 * @param ownerId
	 * @param accessTypes
	 * @param isOwner
	 * @param profile
	 */
	public AclEntry(String ownerId, Set<ACCESS_TYPE> accessTypes, boolean isOwner, UserProfile profile) {
		this(ownerId, accessTypes, isOwner, profile, null, null, true);
	}

	private AclEntry(String ownerId, Set<ACCESS_TYPE> accessTypes, boolean isOwner, UserProfile profile, Team team, String displayName, boolean isIndividual) {
		super();
		this.ownerId = ownerId;
		this.team = team;
		this.profile = profile;
		this.accessTypes = accessTypes;
		this.isOwner = isOwner;
		this.displayName = displayName;
		this.isIndividual = isIndividual;
	}

	public UserProfile getProfile() {
		return profile;
	}

	public void setProfile(UserProfile profile) {
		this.profile = profile;
	}
	
	public Team getTeam() {
		return team;
	}
	
	public void setTeam(Team team) {
		this.team = team;
	}

	public Set<ACCESS_TYPE> getAccessTypes() {
		return accessTypes;
	}
	
	public boolean isIndividual() {
		return isIndividual;
	}

	public void setAccessTypes(Set<ACCESS_TYPE> accessTypes) {
		this.accessTypes = accessTypes;
	}

	public boolean isOwner() {
		return isOwner;
	}

	public void setOwner(boolean isOwner) {
		this.isOwner = isOwner;
	}
	
	public String getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accessTypes == null) ? 0 : accessTypes.hashCode());
		result = prime * result + (isOwner ? 1231 : 1237);
		result = prime * result + ((profile == null) ? 0 : profile.hashCode());
		result = prime * result + ((team == null) ? 0 : team.hashCode());
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
		AclEntry other = (AclEntry) obj;
		if (accessTypes == null) {
			if (other.accessTypes != null)
				return false;
		} else if (!accessTypes.equals(other.accessTypes))
			return false;
		if (isOwner != other.isOwner)
			return false;
		if (profile == null) {
			if (other.profile != null)
				return false;
		} else if (!profile.equals(other.profile))
			return false;
		if (team == null) {
			if (other.team != null)
				return false;
		} else if (!team.equals(other.team))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AclEntry [team=" + team + ", profile=" + profile
				+ ", accessTypes=" + accessTypes + ", isOwner=" + isOwner + "]";
	}

	
	
}
