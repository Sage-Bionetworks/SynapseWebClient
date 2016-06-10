package org.sagebionetworks.web.unitclient.widget.docker;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoWidgetView;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.user.client.ui.Widget;

public class DockerRepoWidgetTest {
	@Mock
	private PreflightController mockPreflightController;
	@Mock
	private DockerRepoWidgetView mockView;
	@Mock
	private SynapseAlert mockSynAlert;
	@Mock
	private WikiPageWidget mockWikiPageWidget;

	DockerRepoWidget dockerRepoWidget;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		dockerRepoWidget = new DockerRepoWidget(mockPreflightController,
				mockView, mockSynAlert, mockWikiPageWidget);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(dockerRepoWidget);
		verify(mockView).setSynapseAlert(any(Widget.class));
		verify(mockView).setWikiPage(any(Widget.class));
	}

}
