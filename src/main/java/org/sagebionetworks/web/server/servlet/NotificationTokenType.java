package org.sagebionetworks.web.server.servlet;


import org.sagebionetworks.repo.model.JoinTeamSignedToken;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.auth.NewUserSignedToken;
import org.sagebionetworks.repo.model.message.NotificationSettingsSignedToken;
import org.sagebionetworks.repo.model.principal.AccountCreationToken;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.web.client.place.SignedToken;

public enum NotificationTokenType  {
	JoinTeam(JoinTeamSignedToken.class),
	NewUser(NewUserSignedToken.class),
	Settings(NotificationSettingsSignedToken.class);

	public final Class<? extends SignedTokenInterface> classType;
	NotificationTokenType(Class<? extends SignedTokenInterface> classType) {
		this.classType = classType;
	}
}
