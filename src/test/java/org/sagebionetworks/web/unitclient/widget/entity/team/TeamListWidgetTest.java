package org.sagebionetworks.web.unitclient.widget.entity.team;

import static org.mockito.Mockito.verify;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.widget.team.TeamListWidget;
import org.sagebionetworks.web.client.widget.team.TeamListWidgetView;

public class TeamListWidgetTest {

	@Mock
	TeamListWidgetView mockView;
	@Mock
	Team mockTeam;
	TeamListWidget widget;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		widget = new TeamListWidget(mockView);
	}

	@Test
	public void testAddTeam() {
		widget.addTeam(mockTeam);
		verify(mockView).addTeam(mockTeam);
	}

	@Test
	public void testSetNotificationValue() {
		String teamId = "111";
		Long count = 12L;
		widget.setNotificationValue(teamId, count);
		verify(mockView).setNotificationValue(teamId, count);
	}
}
