package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.Folder;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.repo.model.table.TableEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils.SelectedHandler;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFilter;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderArea;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class EntityFinderTest {
	@Mock
	EntityFinderView mockView;
	AdapterFactory adapterFactory;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	AuthenticationController mockAuthenticationController;
	EntityFinder entityFinder;
	@Mock
	ClientCache mockClientCache;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	EntityHeader mockHeader;
	@Mock
	EntityHeader mockHeader2;
	@Mock
	SynapseJavascriptClient mockJsClient;
	ArrayList<EntityHeader> entityHeaderResults;

	@Before
	public void before() throws JSONObjectAdapterException {
		adapterFactory = new AdapterFactoryImpl();
		entityFinder = new EntityFinder(mockView, mockGlobalApplicationState, mockAuthenticationController, mockClientCache, mockSynAlert, mockJsClient);
		verify(mockView).setPresenter(entityFinder);
		reset(mockView);
		when(mockView.isShowing()).thenReturn(false);
		entityHeaderResults = new ArrayList<EntityHeader>();
		entityHeaderResults.add(mockHeader);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadEntity() throws Exception {
		SelectedHandler mockHandler = mock(SelectedHandler.class);
		entityFinder.configure(true, mockHandler);
		verify(mockView).setMultiVisible(false);

		String name = "name";
		String id = "syn456";

		when(mockHeader.getId()).thenReturn(id);
		when(mockHeader.getName()).thenReturn(name);
		when(mockHeader.getType()).thenReturn(Folder.class.getName());

		CallbackP<List<EntityHeader>> mockCallback = mock(CallbackP.class);
		AsyncMockStubber.callSuccessWith(entityHeaderResults).when(mockJsClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		entityFinder.lookupEntity(id, mockCallback);

		verify(mockCallback).invoke(entityHeaderResults);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadMultiEntityComma() throws Exception {
		SelectedHandler mockHandler = mock(SelectedHandler.class);
		entityFinder.configureMulti(EntityFilter.ALL, true, mockHandler);
		verify(mockView).setMultiVisible(true);

		String name = "name";
		String id = "syn456";
		String searchId = id + ", " + id;

		when(mockHeader.getId()).thenReturn(id);
		when(mockHeader.getName()).thenReturn(name);
		when(mockHeader.getType()).thenReturn(Folder.class.getName());

		when(mockHeader2.getId()).thenReturn(id);
		when(mockHeader2.getName()).thenReturn(name);
		when(mockHeader2.getType()).thenReturn(Folder.class.getName());

		entityHeaderResults.add(mockHeader2);

		CallbackP<List<EntityHeader>> mockCallback = mock(CallbackP.class);
		AsyncMockStubber.callSuccessWith(entityHeaderResults).when(mockJsClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		entityFinder.lookupEntity(searchId, mockCallback);

		verify(mockCallback).invoke(entityHeaderResults);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadMultiEntitySpace() throws Exception {
		SelectedHandler mockHandler = mock(SelectedHandler.class);
		entityFinder.configureMulti(EntityFilter.ALL, true, mockHandler);

		String name = "name";
		String id = "syn456";
		String searchId = id + " " + id;

		when(mockHeader.getId()).thenReturn(id);
		when(mockHeader.getName()).thenReturn(name);
		when(mockHeader.getType()).thenReturn(Folder.class.getName());

		when(mockHeader2.getId()).thenReturn(id);
		when(mockHeader2.getName()).thenReturn(name);
		when(mockHeader2.getType()).thenReturn(Folder.class.getName());

		entityHeaderResults.add(mockHeader2);

		CallbackP<List<EntityHeader>> mockCallback = mock(CallbackP.class);
		AsyncMockStubber.callSuccessWith(entityHeaderResults).when(mockJsClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		entityFinder.lookupEntity(searchId, mockCallback);

		verify(mockCallback).invoke(entityHeaderResults);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadEntityFail() throws Exception {
		SelectedHandler mockHandler = mock(SelectedHandler.class);
		entityFinder.configure(true, mockHandler);

		String name = "name";
		String id = "syn456";

		when(mockHeader.getId()).thenReturn(id);
		when(mockHeader.getName()).thenReturn(name);
		when(mockHeader.getType()).thenReturn(Folder.class.getName());

		CallbackP<List<EntityHeader>> mockCallback = mock(CallbackP.class);
		Exception ex = new NotFoundException();
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		entityFinder.lookupEntity(id, mockCallback);
		verify(mockCallback, never()).invoke(any());
		verify(mockSynAlert).handleException(ex);
	}

	public void testLookupEntityEmptyResults() throws Exception {
		entityFinder.configure(true, mock(SelectedHandler.class));
		AsyncMockStubber.callSuccessWith(new ArrayList<>()).when(mockJsClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		CallbackP<List<EntityHeader>> mockCallback = mock(CallbackP.class);

		entityFinder.lookupEntity("syn293824", mockCallback);

		verify(mockCallback, never()).invoke(any());
		verify(mockSynAlert).handleException(any(NotFoundException.class));
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testLoadVersions() throws Exception {
		String id = "syn456";
		List<VersionInfo> results = new ArrayList<VersionInfo>();
		AsyncMockStubber.callSuccessWith(results).when(mockJsClient).getEntityVersions(eq(id), anyInt(), anyInt(), any(AsyncCallback.class));

		entityFinder.loadVersions(id);
		verify(mockJsClient).getEntityVersions(eq(id), eq(WebConstants.ZERO_OFFSET.intValue()), anyInt(), any(AsyncCallback.class));
		verify(mockView).setVersions(results);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoadVersionsFail() throws Exception {
		String id = "syn456";
		PaginatedResults<VersionInfo> paginated = new PaginatedResults<VersionInfo>();
		List<VersionInfo> results = new ArrayList<VersionInfo>();
		paginated.setResults(results);
		Exception ex = new Exception();
		AsyncMockStubber.callFailureWith(ex).when(mockJsClient).getEntityVersions(eq(id), anyInt(), anyInt(), any(AsyncCallback.class));

		entityFinder.loadVersions(id);

		verify(mockSynAlert).handleException(ex);
	}

	@Test
	public void testSelectionHandlerAnyType() throws Exception {
		SelectedHandler mockHandler = mock(SelectedHandler.class);
		entityFinder.configure(true, mockHandler);

		// then the view calls okClicked if the user has clicked ok in the entity finder

		// no selection
		entityFinder.okClicked();
		// verify the error
		verify(mockSynAlert).showError(DisplayConstants.PLEASE_MAKE_SELECTION);

		// now with selection
		Reference mockReference = mock(Reference.class);
		when(mockReference.getTargetId()).thenReturn("syn99");
		when(mockHeader.getType()).thenReturn(Folder.class.getName());
		AsyncMockStubber.callSuccessWith(entityHeaderResults).when(mockJsClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		// the view usually sets the selected entity in the presenter
		entityFinder.setSelectedEntity(mockReference);
		entityFinder.okClicked();
		verify(mockHandler).onSelected(mockReference);
	}

	private void verifyWrongEntityTypeSelected(ArrayList<EntityHeader> entitySelected, SelectedHandler mockSelectionHandler) {
		reset(mockSynAlert, mockSelectionHandler);
		AsyncMockStubber.callSuccessWith(entitySelected).when(mockJsClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		entityFinder.okClicked();
		verify(mockSynAlert).showError(anyString());
		verify(mockSelectionHandler, never()).onSelected(any(Reference.class));
	}

	private void verifyCorrectEntityTypeSelected(ArrayList<EntityHeader> results, SelectedHandler mockSelectionHandler) {
		reset(mockSynAlert, mockSelectionHandler);
		AsyncMockStubber.callSuccessWith(results).when(mockJsClient).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		entityFinder.okClicked();
		verify(mockSynAlert, never()).showError(anyString());
		verify(mockSelectionHandler).onSelected(any(Reference.class));
	}

	private void verifySelectedWithoutTypeCheck(SelectedHandler mockSelectionHandler) {
		reset(mockSynAlert, mockSelectionHandler);
		entityFinder.okClicked();
		verify(mockJsClient, never()).getEntityHeaderBatchFromReferences(anyList(), any(AsyncCallback.class));
		verify(mockSynAlert, never()).showError(anyString());
		verify(mockSelectionHandler).onSelected(any(Reference.class));
	}

	@Test
	public void testAllFilter() throws Exception {
		SelectedHandler mockHandler = mock(SelectedHandler.class);
		entityFinder.configure(EntityFilter.ALL, true, mockHandler);
		Reference mockReference = mock(Reference.class);
		when(mockReference.getTargetId()).thenReturn("syn99");
		entityFinder.setSelectedEntity(mockReference);

		when(mockHeader.getType()).thenReturn(Folder.class.getName());
		verifySelectedWithoutTypeCheck(mockHandler);
		when(mockHeader.getType()).thenReturn(FileEntity.class.getName());
		verifySelectedWithoutTypeCheck(mockHandler);
		when(mockHeader.getType()).thenReturn(Project.class.getName());
		verifySelectedWithoutTypeCheck(mockHandler);
		when(mockHeader.getType()).thenReturn(TableEntity.class.getName());
		verifySelectedWithoutTypeCheck(mockHandler);
	}

	@Test
	public void testProjectFilter() throws Exception {
		SelectedHandler mockHandler = mock(SelectedHandler.class);
		entityFinder.configure(EntityFilter.PROJECT, true, mockHandler);
		Reference mockReference = mock(Reference.class);
		when(mockReference.getTargetId()).thenReturn("syn99");
		entityFinder.setSelectedEntity(mockReference);

		when(mockHeader.getType()).thenReturn(Folder.class.getName());
		verifyWrongEntityTypeSelected(entityHeaderResults, mockHandler);
		when(mockHeader.getType()).thenReturn(FileEntity.class.getName());
		verifyWrongEntityTypeSelected(entityHeaderResults, mockHandler);
		when(mockHeader.getType()).thenReturn(Project.class.getName());
		verifyCorrectEntityTypeSelected(entityHeaderResults, mockHandler);
	}

	@Test
	public void testFileFilter() throws Exception {
		SelectedHandler mockHandler = mock(SelectedHandler.class);
		entityFinder.configure(EntityFilter.FILE, true, mockHandler);
		Reference mockReference = mock(Reference.class);
		when(mockReference.getTargetId()).thenReturn("syn99");
		entityFinder.setSelectedEntity(mockReference);

		when(mockHeader.getType()).thenReturn(Folder.class.getName());
		verifyWrongEntityTypeSelected(entityHeaderResults, mockHandler);
		when(mockHeader.getType()).thenReturn(Project.class.getName());
		verifyWrongEntityTypeSelected(entityHeaderResults, mockHandler);
		when(mockHeader.getType()).thenReturn(FileEntity.class.getName());
		verifyCorrectEntityTypeSelected(entityHeaderResults, mockHandler);
	}

	@Test
	public void testContainerFilter() throws Exception {
		SelectedHandler mockHandler = mock(SelectedHandler.class);
		entityFinder.configure(EntityFilter.CONTAINER, true, mockHandler);
		Reference mockReference = mock(Reference.class);
		when(mockReference.getTargetId()).thenReturn("syn99");
		entityFinder.setSelectedEntity(mockReference);

		when(mockHeader.getType()).thenReturn(FileEntity.class.getName());
		verifyWrongEntityTypeSelected(entityHeaderResults, mockHandler);
		when(mockHeader.getType()).thenReturn(Folder.class.getName());
		verifyCorrectEntityTypeSelected(entityHeaderResults, mockHandler);
		when(mockHeader.getType()).thenReturn(Project.class.getName());
		verifyCorrectEntityTypeSelected(entityHeaderResults, mockHandler);
	}

	@Test
	public void testShowDefaultArea() {
		entityFinder.show();
		verify(mockView).clear();
		verify(mockView).setBrowseAreaVisible();
		verify(mockView).show();
	}

	@Test
	public void testShowBrowseArea() {
		when(mockClientCache.get(EntityFinder.ENTITY_FINDER_AREA_KEY)).thenReturn(EntityFinderArea.BROWSE.toString());
		entityFinder.show();
		verify(mockView).clear();
		verify(mockView).setBrowseAreaVisible();
		verify(mockView).show();
	}

	@Test
	public void testShowSearchArea() {
		when(mockClientCache.get(EntityFinder.ENTITY_FINDER_AREA_KEY)).thenReturn(EntityFinderArea.SEARCH.toString());
		entityFinder.show();
		verify(mockView).clear();
		verify(mockView).setSearchAreaVisible();
		verify(mockView).show();
	}

	@Test
	public void testShowSynIdArea() {
		when(mockClientCache.get(EntityFinder.ENTITY_FINDER_AREA_KEY)).thenReturn(EntityFinderArea.SYNAPSE_ID.toString());
		entityFinder.show();
		verify(mockView).clear();
		verify(mockView).setSynapseIdAreaVisible();
		verify(mockView).show();
	}

	@Test
	public void testShowMultiSynIdArea() {
		when(mockClientCache.get(EntityFinder.ENTITY_FINDER_AREA_KEY)).thenReturn(EntityFinderArea.SYNAPSE_MULTI_ID.toString());
		entityFinder.show();
		verify(mockView).clear();
		verify(mockView).setSynapseMultiIdAreaVisible();
		verify(mockView).show();
	}

	@Test
	public void testHideNoArea() {
		when(mockView.getCurrentArea()).thenReturn(null);
		entityFinder.hide();
		verify(mockClientCache, never()).put(anyString(), anyString());
	}

	@Test
	public void testHideSearchArea() {
		when(mockView.getCurrentArea()).thenReturn(EntityFinderArea.SEARCH);
		entityFinder.hide();
		verify(mockClientCache).put(EntityFinder.ENTITY_FINDER_AREA_KEY, EntityFinderArea.SEARCH.toString());
	}

	@Test
	public void testClearBeforeConfigure() {
		// should not result in an exception
		entityFinder.clearSelectedEntities();
		assertTrue(entityFinder.getSelectedEntity().isEmpty());
	}
}


