package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.repo.model.file.FileResultFailureCode;
import org.sagebionetworks.repo.model.file.S3FileHandle;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SessionDetector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.DownloadSpeedTesterImpl;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.FileHandleWidgetView;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.PresignedAndFileHandleURLAsyncHandler;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.FileCellRenderer;
import org.sagebionetworks.web.shared.exceptions.UnauthorizedException;
import org.sagebionetworks.web.shared.table.CellAddress;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;

import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class SessionDetectorTest {

	SessionDetector sessionDetector;

	@Mock
	AuthenticationController mockAuthController;
	@Mock
	GlobalApplicationState mockGlobalAppState;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	ClientCache mockClientCache;
	@Captor
	ArgumentCaptor<Callback> callbackCaptor;
	
	@Before
	public void setUp() throws Exception {
		sessionDetector = new SessionDetector(mockAuthController, mockGlobalAppState, mockGWT, mockClientCache);
	}

	@Test
	public void testSessionChanges() {
		sessionDetector.start();
		
		verify(mockGWT).scheduleExecution(callbackCaptor.capture(), eq(SessionDetector.INTERVAL_MS));
		// SWC-4947
		verify(mockAuthController).checkForUserChange();
		
		Callback cb = callbackCaptor.getValue();

		// Was no session, still is no session.
		cb.invoke();
		verify(mockAuthController, times(1)).checkForUserChange();
		
		// Was no session, is now logged in.  Should check for user change.
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn("1");
		cb.invoke();
		verify(mockAuthController, times(2)).checkForUserChange();
		
		// Was a session, now is no session.  Should check for user change.
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn(null);
		when(mockClientCache.get(SessionDetector.SESSION_MARKER)).thenReturn("1");
		cb.invoke();
		verify(mockAuthController, times(3)).checkForUserChange();
		
		// Was a session, now has same session.  No need to check for user change.
		when(mockAuthController.getCurrentUserPrincipalId()).thenReturn("1");
		when(mockClientCache.get(SessionDetector.SESSION_MARKER)).thenReturn("1");
		cb.invoke();
		verify(mockAuthController, times(3)).checkForUserChange();
	}
	
}
