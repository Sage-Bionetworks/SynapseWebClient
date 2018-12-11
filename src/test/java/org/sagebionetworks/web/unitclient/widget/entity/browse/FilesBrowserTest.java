package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.Annotations;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.clienthelp.ContainerClientsHelp;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.FilesBrowserView;
import org.sagebionetworks.web.client.widget.entity.file.AddToDownloadList;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public class FilesBrowserTest {

	FilesBrowserView mockView;
	SynapseClientAsync mockSynapseClient;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	FilesBrowser filesBrowser;
	CookieProvider mockCookies;
	String configuredEntityId = "syn123";
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	ContainerClientsHelp mockContainerClientsHelp;
	@Mock
	AddToDownloadList mockAddToDownloadList;
	@Before
	public void before() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		mockView = mock(FilesBrowserView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		mockCookies = mock(CookieProvider.class);
		filesBrowser = new FilesBrowser(mockView,
				mockGlobalApplicationState, 
				mockAuthenticationController,
				mockContainerClientsHelp,
				mockAddToDownloadList);
	}

	@Test
	public void testConstructor() {		
		verify(mockView).setAddToDownloadList(any(IsWidget.class));
		verify(mockView).setPresenter(filesBrowser);
	}

	@Test
	public void testConfigure() {		
		String entityId = "syn123";
		filesBrowser.configure(entityId);
		verify(mockView).configure(entityId);
		
		filesBrowser.onProgrammaticDownloadOptions();
		verify(mockContainerClientsHelp).configureAndShow(entityId);
		
		filesBrowser.onAddToDownloadList();
		verify(mockAddToDownloadList).addToDownloadList(entityId);
	}
}

