package org.sagebionetworks.web.shared;


import org.sagebionetworks.repo.model.JoinTeamSignedToken;
import org.sagebionetworks.repo.model.MembershipInvtnSignedToken;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.auth.NewUserSignedToken;
import org.sagebionetworks.repo.model.message.NotificationSettingsSignedToken;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;

public enum NotificationTokenType  {
	EmailValidation(EmailValidationSignedToken.class),
	NewUser(NewUserSignedToken.class),
	// If any of the following values are renamed, please modify the corresponding constants in
	// org.sagebionetworks.repo.model.ServiceConstants (PLFM)
	JoinTeam(JoinTeamSignedToken.class),
	Settings(NotificationSettingsSignedToken.class),
	EmailInvitation(MembershipInvtnSignedToken.class);

	public final Class<? extends SignedTokenInterface> classType;
	NotificationTokenType(Class<? extends SignedTokenInterface> classType) {
		this.classType = classType;
	}
}
