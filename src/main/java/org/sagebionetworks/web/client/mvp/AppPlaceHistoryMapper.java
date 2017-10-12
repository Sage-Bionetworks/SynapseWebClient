package org.sagebionetworks.web.client.mvp;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import org.sagebionetworks.web.client.place.*;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.place.users.RegisterAccount;

/**
 * PlaceHistoryMapper interface is used to attach all places which the
 * PlaceHistoryHandler should be aware of. This is done via the @WithTokenizers
 * annotation or by extending PlaceHistoryMapperWithFactory and creating a
 * separate TokenizerFactory.
 */
@WithTokenizers({ Home.Tokenizer.class, LoginPlace.Tokenizer.class,
		PasswordReset.Tokenizer.class, RegisterAccount.Tokenizer.class,
		ProjectsHome.Tokenizer.class, Profile.Tokenizer.class,
		ComingSoon.Tokenizer.class, Synapse.Tokenizer.class, Wiki.Tokenizer.class,
		Search.Tokenizer.class,
		Challenges.Tokenizer.class, Help.Tokenizer.class, Governance.Tokenizer.class,
		Down.Tokenizer.class, Team.Tokenizer.class, MapPlace.Tokenizer.class, 
		TeamSearch.Tokenizer.class, Quiz.Tokenizer.class, Account.Tokenizer.class, Certificate.Tokenizer.class, 
		NewAccount.Tokenizer.class,
		ChangeUsername.Tokenizer.class, Trash.Tokenizer.class, PeopleSearch.Tokenizer.class,
		StandaloneWiki.Tokenizer.class,
		SignedToken.Tokenizer.class, ErrorPlace.Tokenizer.class, ACTPlace.Tokenizer.class, 
		SynapseForumPlace.Tokenizer.class, SubscriptionPlace.Tokenizer.class,
		AccessRequirementsPlace.Tokenizer.class, ACTDataAccessSubmissionsPlace.Tokenizer.class,
		ACTDataAccessSubmissionDashboardPlace.Tokenizer.class, ACTAccessApprovalsPlace.Tokenizer.class,
		EmailInvitation.Tokenizer.class})
public interface AppPlaceHistoryMapper extends PlaceHistoryMapper {
}
