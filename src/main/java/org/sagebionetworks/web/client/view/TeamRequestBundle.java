package org.sagebionetworks.web.client.view;

import java.io.Serializable;

import org.sagebionetworks.repo.model.Team;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TeamRequestBundle implements IsSerializable, Serializable {

	private Team team;
	private Long requestCount;
	
	public TeamRequestBundle() {
	}
	
	public TeamRequestBundle(Team team, Long requestCount) {
		this.team = team;
		this.requestCount = requestCount;
	}
	
	public Team getTeam() {
		return team;
	}
	
	public Long getRequestCount() {
		return requestCount;
	}
	
}
