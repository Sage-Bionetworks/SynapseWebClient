package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AutoGenFactory;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SearchServiceAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.browse.PagesBrowser;
import org.sagebionetworks.web.client.widget.entity.browse.PagesBrowserView;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class PagesBrowserTest {

	PagesBrowserView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	AdapterFactory adapterFactory;
	AutoGenFactory autoGenFactory;
	SearchServiceAsync mockSearchServiceAsync;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	PagesBrowser pagesBrowser;
	
	String entityId = "syn123";
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(PagesBrowserView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		adapterFactory = new AdapterFactoryImpl();
		autoGenFactory = new AutoGenFactory();		
		mockSearchServiceAsync = mock(SearchServiceAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		pagesBrowser = new PagesBrowser(mockView, mockSynapseClient, mockNodeModelCreator, adapterFactory, autoGenFactory, mockSearchServiceAsync, mockGlobalApplicationState, mockAuthenticationController);
		verify(mockView).setPresenter(pagesBrowser);
		ArrayList<String> results = new ArrayList<String>();
		results.add("A Test Entity Header");
		AsyncMockStubber.callSuccessWith(results).when(mockSearchServiceAsync).searchEntities(anyString(),any(List.class), anyInt(), anyInt(), anyString(), anyBoolean(), any(AsyncCallback.class));
		
		AsyncMockStubber.callSuccessWith("entity id 1").when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		
		when(mockNodeModelCreator.createJSONEntity(anyString(), eq(EntityHeader.class))).thenReturn(new EntityHeader());
		reset(mockView);
	}
	
	@Test
	public void testConfigureProjectWithExistingWiki() throws Exception {
		//it should search for the wiki searchService.searchEntities with a single entity returned (see before for test setup)
		//create the returned entity header using mockNodeModelCreator.createJSONEntity (see before for test setup)
		//and then call searchService.searchEntities to refresh the children of the returned entity (see before for test setup).
		//let's kick it off:
		pagesBrowser.configure(entityId, DisplayConstants.PAGES, true, true);
		//after all this, it should call view.configure with the list of entityheaders
		verify(mockView).configure(any(List.class), anyBoolean());
	}
	
	@Test
	public void testConfigureProjectFailure() throws Exception {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSearchServiceAsync).searchEntities(anyString(),any(List.class), anyInt(), anyInt(), anyString(), anyBoolean(), any(AsyncCallback.class));
		pagesBrowser.configure(entityId, DisplayConstants.PAGES, true, true);

		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testConfigureProjectWithoutExistingWiki() {
		//it should search for the wiki searchService.searchEntities with no entities returned
		ArrayList<String> results = new ArrayList<String>();
		AsyncMockStubber.callSuccessWith(results).when(mockSearchServiceAsync).searchEntities(anyString(),any(List.class), anyInt(), anyInt(), anyString(), anyBoolean(), any(AsyncCallback.class));

		//it should try to create the wiki folder synapseClient.createOrUpdateEntity (see before for test setup)
		
		//and then call searchService.searchEntities to refresh the children of the returned entity (see before for test setup).
		//let's kick it off:
		pagesBrowser.configure(entityId, DisplayConstants.PAGES, true, true);
		//after all this, it should call view.configure with the list of entityheaders

		verify(mockView).configure(any(List.class), anyBoolean());
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testConfigurePage() {
		pagesBrowser.configure(entityId, DisplayConstants.PAGES, true, false);
		verify(mockView).configure(any(List.class), anyBoolean());
		verify(mockSynapseClient, Mockito.times(0)).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testAsWidget() {
		pagesBrowser.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testCreatePageSuccess() {
		pagesBrowser.createPage("test page");
		//needed to have created the page
		verify(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		//report that a page was created
		verify(mockView).showInfo(anyString(), anyString());
		//and refreshed the children
		verify(mockSearchServiceAsync).searchEntities(anyString(), any(List.class), anyInt(), anyInt(), anyString(), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testCreatePageFailure() {
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).createOrUpdateEntity(anyString(), anyString(), anyBoolean(), any(AsyncCallback.class));
		pagesBrowser.createPage("test page");
		verify(mockView).showErrorMessage(anyString());
	}
	
	@Test
	public void testRefreshChildrenFailure(){
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSearchServiceAsync).searchEntities(anyString(),any(List.class), anyInt(), anyInt(), anyString(), anyBoolean(), any(AsyncCallback.class));
		pagesBrowser.refreshChildren(entityId);

		verify(mockView).showErrorMessage(anyString());	
	}
}











