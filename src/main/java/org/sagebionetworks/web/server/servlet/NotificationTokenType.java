package org.sagebionetworks.web.server.servlet;


import org.sagebionetworks.repo.model.JoinTeamSignedToken;
import org.sagebionetworks.repo.model.auth.NewUserSignedToken;
import org.sagebionetworks.repo.model.message.NotificationSettingsSignedToken;
import org.sagebionetworks.repo.model.principal.AccountCreationToken;
import org.sagebionetworks.schema.adapter.JSONEntity;

public enum NotificationTokenType  {
	JoinTeam(JoinTeamSignedToken.class),
	NewUser(NewUserSignedToken.class),
	Settings(NotificationSettingsSignedToken.class);

	public final Class<? extends JSONEntity> classType;
	NotificationTokenType(Class<? extends JSONEntity> classType) {
		this.classType = classType;
	}
}
