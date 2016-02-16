package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousJobTracker;
import org.sagebionetworks.web.client.widget.asynch.UpdatingAsynchProgressHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.renderer.SynapseTableFormWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.SynapseTableFormWidgetView;
import org.sagebionetworks.web.client.widget.table.v2.results.QueryResultEditorWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.RowFormEditorWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class SynapseTableFormWidgetTest {
		
	SynapseTableFormWidget widget;
	
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	@Mock
	SynapseTableFormWidgetView mockView;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	RowFormEditorWidget mockRowFormWidget;
	@Mock
	AsynchronousJobTracker mockAsynchronousJobTracker;
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	Row mockRow;
	@Mock
	AsynchronousResponseBody mockResponse;
	
	private static final String TABLE_ID = "syn7777777";
	private static final String SUCCESS_MESSAGE = "Custom success message";
	private List<ColumnModel> columnModels;
	private Map<String, String> descriptor;
	@Before
	public void setup() throws RequestException{
		MockitoAnnotations.initMocks(this);
		columnModels = new ArrayList<ColumnModel>();
		widget = new SynapseTableFormWidget(mockView, mockSynAlert, mockRowFormWidget, mockAsynchronousJobTracker, mockSynapseClient);
		AsyncMockStubber.callSuccessWith(columnModels).when(mockSynapseClient).getColumnModelsForTableEntity(anyString(), any(AsyncCallback.class));
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.TABLE_ID_KEY, TABLE_ID);
		descriptor.put(WidgetConstants.SUCCESS_MESSAGE, SUCCESS_MESSAGE);
		when(mockSynAlert.isUserLoggedIn()).thenReturn(true);
		when(mockRowFormWidget.isValid()).thenReturn(true);
		when(mockRowFormWidget.getRow()).thenReturn(mockRow);
	}
	
	@Test
	public void testAsWidget() {
		//test construction
		verify(mockView).setRowFormWidget(any(Widget.class));
		verify(mockView).setSynAlertWidget(any(Widget.class));
		verify(mockView).setPresenter(widget);
		
		//and asWidget
		widget.asWidget();
		verify(mockView).asWidget();
	}
	

	@Test
	public void testConfigureAnonymous() {
		when(mockSynAlert.isUserLoggedIn()).thenReturn(false);
		widget.configure(wikiKey, descriptor, null, null);
		
		//verify all is cleared
		verify(mockSynAlert).clear();
		verify(mockRowFormWidget).clear();
		verify(mockView).setFormUIVisible(false);
		verify(mockView).setSuccessMessageVisible(false);
		
		verify(mockSynAlert).showMustLogin();
	}
	
	@Test
	public void testConfigure() {
		widget.configure(wikiKey, descriptor, null, null);
		
		//verify all is cleared
		verify(mockSynAlert).clear();
		verify(mockRowFormWidget).clear();
		verify(mockView).setFormUIVisible(false);
		verify(mockView).setSuccessMessageVisible(false);
		
		verify(mockView).setSuccessMessage(SUCCESS_MESSAGE);
		verify(mockSynapseClient).getColumnModelsForTableEntity(eq(TABLE_ID), any(AsyncCallback.class));
		verify(mockRowFormWidget).configure(TABLE_ID, columnModels);
		verify(mockView).setFormUIVisible(true);
	}
	
	@Test
	public void testConfigureFailure() throws RequestException {
		Exception e = new Exception("Could not retrieve column models");
		AsyncMockStubber.callFailureWith(e).when(mockSynapseClient).getColumnModelsForTableEntity(anyString(), any(AsyncCallback.class));
		widget.configure(wikiKey, descriptor, null, null);
		
		verify(mockSynapseClient).getColumnModelsForTableEntity(eq(TABLE_ID), any(AsyncCallback.class));
		verify(mockSynAlert).handleException(e);
	}
	
	@Test
	public void testOnSubmitInvalid() throws RequestException {
		when(mockRowFormWidget.isValid()).thenReturn(false);
		widget.onSubmit();
		verify(mockSynAlert).clear();
		verify(mockSynAlert).showError(QueryResultEditorWidget.SEE_THE_ERRORS_ABOVE);
		verify(mockAsynchronousJobTracker, never()).startAndTrack(any(AsynchType.class), any(AsynchronousRequestBody.class), anyInt(), any(UpdatingAsynchProgressHandler.class));
	}
	
	@Test
	public void testOnSubmitOnFailure() throws RequestException {
		widget.configure(wikiKey, descriptor, null, null);
		
		widget.onSubmit();
		
		ArgumentCaptor<UpdatingAsynchProgressHandler> captor = ArgumentCaptor.forClass(UpdatingAsynchProgressHandler.class);
		verify(mockAsynchronousJobTracker).startAndTrack(any(AsynchType.class), any(AsynchronousRequestBody.class), anyInt(), captor.capture());
		UpdatingAsynchProgressHandler handler = captor.getValue();
		
		assertTrue(handler.isAttached());
		Exception ex = new Exception("error occurred during table update");
		handler.onFailure(ex);
		verify(mockSynAlert).handleException(ex);
	}
	
	@Test
	public void testOnSubmitOnCancel() throws RequestException {
		widget.configure(wikiKey, descriptor, null, null);
		widget.onSubmit();
		ArgumentCaptor<UpdatingAsynchProgressHandler> captor = ArgumentCaptor.forClass(UpdatingAsynchProgressHandler.class);
		verify(mockAsynchronousJobTracker).startAndTrack(any(AsynchType.class), any(AsynchronousRequestBody.class), anyInt(), captor.capture());
		UpdatingAsynchProgressHandler handler = captor.getValue();
		
		reset(mockView);
		handler.onCancel();
		verify(mockView).setSubmitButtonLoading(false);
	}
	
	@Test
	public void testOnSubmitOnComplete() throws RequestException {
		widget.configure(wikiKey, descriptor, null, null);
		widget.onSubmit();
		ArgumentCaptor<UpdatingAsynchProgressHandler> captor = ArgumentCaptor.forClass(UpdatingAsynchProgressHandler.class);
		verify(mockAsynchronousJobTracker).startAndTrack(any(AsynchType.class), any(AsynchronousRequestBody.class), anyInt(), captor.capture());
		UpdatingAsynchProgressHandler handler = captor.getValue();
		
		reset(mockView, mockSynAlert, mockRowFormWidget);
		handler.onComplete(mockResponse);
		verify(mockSynAlert).clear();
		verify(mockRowFormWidget).clear();
		verify(mockView).setFormUIVisible(false);
		verify(mockView).setSuccessMessageVisible(true);
	}

}
