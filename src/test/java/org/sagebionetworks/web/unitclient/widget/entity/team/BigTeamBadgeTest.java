package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.renderer.TeamMemberCountWidget;
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.BigTeamBadgeView;

@RunWith(MockitoJUnitRunner.class)
public class BigTeamBadgeTest {
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	BigTeamBadgeView mockView;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	TeamMemberCountWidget mockTeamMemberCountWidget;

	BigTeamBadge presenter;

	@Before
	public void setUp() throws Exception {
		presenter = new BigTeamBadge(mockView, mockJsClient, mockAuthenticationController, mockTeamMemberCountWidget);
	}

	@Test
	public void testGetTeamEmail() {
		boolean canSendEmail = true;
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		assertEquals("basic@synapse.org", presenter.getTeamEmail("basic", canSendEmail));
		assertEquals("StandardCaseHere@synapse.org", presenter.getTeamEmail("Standard Case Here", canSendEmail));
		assertEquals("unlikelycase@synapse.org", presenter.getTeamEmail(" \n\r unlikely\t case ", canSendEmail));
		assertEquals("Another_UnlikelyCase@synapse.org", presenter.getTeamEmail(" %^$##* Another_Unlikely\t &*#$)(!!@~Case ", canSendEmail));

		canSendEmail = false;
		assertEquals("", presenter.getTeamEmail("basic", canSendEmail));

		canSendEmail = true;
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		assertEquals("", presenter.getTeamEmail("basic", canSendEmail));
	}

}
