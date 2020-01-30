package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberRowWidget.SYNAPSE_ORG;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberRowWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberRowWidgetView;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public class TeamMemberRowWidgetTest {

	@Mock
	TeamMemberRowWidgetView mockView;
	@Mock
	UserBadge mockUserBadge;

	TeamMemberRowWidget widget;
	@Mock
	UserProfile mockProfile;

	public static final String USERNAME = "a_user";
	public static final String COMPANY = "WWU";

	@Before
	public void before() throws RestServiceException, JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		widget = new TeamMemberRowWidget(mockView, mockUserBadge);
		when(mockProfile.getCompany()).thenReturn(COMPANY);
		when(mockProfile.getUserName()).thenReturn(USERNAME);
	}

	@Test
	public void testConfigure() throws Exception {
		widget.configure(mockProfile);
		verify(mockUserBadge).configure(mockProfile);
		verify(mockView).setInstitution(COMPANY);
		verify(mockView).setEmail(USERNAME + SYNAPSE_ORG);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}


