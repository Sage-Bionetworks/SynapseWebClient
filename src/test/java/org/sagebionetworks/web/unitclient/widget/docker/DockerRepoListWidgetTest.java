package org.sagebionetworks.web.unitclient.widget.docker;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerRepoListWidgetView;
import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;

import com.google.gwt.user.client.ui.Widget;

public class DockerRepoListWidgetTest {
	@Mock
	private PreflightController mockPreflightController;
	@Mock
	private DockerRepoListWidgetView mockView;
	@Mock
	private PaginationWidget mockPaginationWidget;
	@Mock
	private AddExternalRepoModal mockAddExternalRepoModal;

	DockerRepoListWidget dockerRepoListWidget;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		dockerRepoListWidget = new DockerRepoListWidget(mockPreflightController,
				mockView, mockPaginationWidget, mockAddExternalRepoModal);
	}

	@Test
	public void testConstruction() {
		verify(mockView).setPresenter(dockerRepoListWidget);
		verify(mockView).addExternalRepoModal(any(Widget.class));
		verify(mockView).addPaginationWidget(any(PaginationWidget.class));
	}

}
