package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.LogEntry;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.ErrorPlace;
import org.sagebionetworks.web.client.presenter.ErrorPresenter;
import org.sagebionetworks.web.client.view.ErrorView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class ErrorPresenterTest {
	
	ErrorPresenter presenter;
	ErrorView mockView;
	SynapseClientAsync mockSynapse;
	SynapseAlert mockSynAlert;
	SynapseJSNIUtils mockJsniUtils;
	LogEntry mockLogEntry;
	
	@Before
	public void setup() throws JSONObjectAdapterException{
		mockView = mock(ErrorView.class);
		mockJsniUtils = mock(SynapseJSNIUtils.class);
		mockSynapse = mock(SynapseClientAsync.class);
		mockSynAlert = mock(SynapseAlert.class);
		mockLogEntry = mock(LogEntry.class);
		presenter = new ErrorPresenter(mockView, mockSynapse, mockSynAlert, mockJsniUtils);
		when(mockSynAlert.isUserLoggedIn()).thenReturn(false);
		AsyncMockStubber.callSuccessWith(mockLogEntry).when(mockSynapse).hexDecodeLogEntry(
				anyString(), any(AsyncCallback.class));
		
		verify(mockView).setPresenter(presenter);
		verify(mockView).setSynAlertWidget(any(Widget.class));
	}	
	
	@Test
	public void testSetPlace() {
		ErrorPlace place = Mockito.mock(ErrorPlace.class);
		presenter.setPlace(place);
	}
	
	@Test
	public void testShowLogEntryAnonymous() throws RestServiceException {
		presenter.showLogEntry("");
		verify(mockView).clear();
		verify(mockView, times(2)).setPresenter(presenter);
		verify(mockSynAlert).clear();
		verify(mockView).setEntry(mockLogEntry);
		verify(mockJsniUtils, times(3)).consoleError(anyString());
		verify(mockSynAlert).showMustLogin();
	}
	@Test
	public void testShowLogEntryLoggedIn() throws RestServiceException {
		when(mockSynAlert.isUserLoggedIn()).thenReturn(true);
		presenter.showLogEntry("");
		verify(mockView).clear();
		verify(mockView, times(2)).setPresenter(presenter);
		verify(mockSynAlert).clear();
		verify(mockView).setEntry(mockLogEntry);
		verify(mockJsniUtils, times(3)).consoleError(anyString());
		verify(mockSynAlert, never()).showMustLogin();
	}
	
	@Test
	public void testShowLogEntryFailure() throws RestServiceException {
		Exception caught = new Exception("unhandled exception");
		AsyncMockStubber.callFailureWith(caught).when(mockSynapse).hexDecodeLogEntry(
				anyString(), any(AsyncCallback.class));
		presenter.showLogEntry("");
		verify(mockSynAlert).handleException(caught);
	}
}