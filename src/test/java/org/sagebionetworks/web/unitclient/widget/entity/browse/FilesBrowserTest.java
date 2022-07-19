package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityChildrenResponse;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.clienthelp.ContainerClientsHelp;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserView;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadListV2;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class FilesBrowserTest {

	FilesBrowserView mockView;
	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	FilesBrowser filesBrowser;
	@Mock
	CookieProvider mockCookies;
	String configuredEntityId = "syn123";
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	ContainerClientsHelp mockContainerClientsHelp;
	@Mock
	AddToDownloadListV2 mockAddToDownloadListV2;
	@Mock
	SynapseJavascriptClient mockJsClient;
	@Mock
	EntityChildrenResponse mockEntityChildrenResponse;
	List<EntityHeader> children;
	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockView = mock(FilesBrowserView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		filesBrowser = new FilesBrowser(mockView, mockGlobalApplicationState, mockAuthenticationController, mockContainerClientsHelp, mockAddToDownloadListV2, mockCookies, mockJsClient);
		AsyncMockStubber.callSuccessWith(mockEntityChildrenResponse).when(mockJsClient).getEntityChildren(any(), any());
		children = new ArrayList<>();
		when(mockEntityChildrenResponse.getPage()).thenReturn(children);
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(filesBrowser);
	}

	@Test
	public void testConfigure() {
		String entityId = "syn123";
		filesBrowser.configure(entityId);
		verify(mockView).configure(entityId);

		filesBrowser.onProgrammaticDownloadOptions();
		verify(mockContainerClientsHelp).configureAndShow(entityId);
	}

	@Test
	public void testHasNoFileChildren() {
		filesBrowser.configure("syn121");
		
		verify(mockView).setHasFile(false);
	}

	@Test
	public void testHasFileChildren() {
		children.add(new EntityHeader());
		
		filesBrowser.configure("syn121");
		
		verify(mockView).setHasFile(true);
	}

	@Test
	public void testHasNoFileChildrenError() {
		String errorMessage = "unable to get the children for some reason";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockJsClient).getEntityChildren(any(), any());
		
		filesBrowser.configure("syn121");
		
		verify(mockView).showErrorMessage(errorMessage);
	}

	@Test
	public void testAddToDownloadListV2() {
		when(mockCookies.getCookie(eq(DisplayUtils.SYNAPSE_TEST_WEBSITE_COOKIE_KEY))).thenReturn("true");
		String entityId = "syn123";
		filesBrowser.configure(entityId);
		filesBrowser.onAddToDownloadList();
		verify(mockAddToDownloadListV2).configure(entityId);

	}
}

