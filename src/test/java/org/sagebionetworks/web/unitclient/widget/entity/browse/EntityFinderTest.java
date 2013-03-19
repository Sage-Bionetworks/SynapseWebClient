package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.mockito.Matchers.any;
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
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.VersionInfo;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinder;
import org.sagebionetworks.web.client.widget.entity.browse.EntityFinderView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityFinderTest {

	EntityFinderView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	AdapterFactory adapterFactory;
	AutoGenFactory autoGenFactory;
	
	EntityFinder entityFinder;	
	
	@Before
	public void before() throws JSONObjectAdapterException {
		mockView = mock(EntityFinderView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		adapterFactory = new AdapterFactoryImpl();
		autoGenFactory = new AutoGenFactory();		
		
		entityFinder = new EntityFinder(mockView, mockNodeModelCreator, mockSynapseClient);
		verify(mockView).setPresenter(entityFinder);
		reset(mockView);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadEntity() throws Exception {
		String name = "name";
		String id = "syn456";
		Entity entity = new Data();
		entity.setId(id);
		entity.setName(name);
		EntityWrapper wrapper = new EntityWrapper();
		AsyncMockStubber.callSuccessWith(wrapper).when(mockSynapseClient).getEntity(eq(id), any(AsyncCallback.class));		
		AsyncCallback<Entity> mockCallback = mock(AsyncCallback.class);
		when(mockNodeModelCreator.createEntity(any(EntityWrapper.class))).thenReturn(entity);
		
		entityFinder.lookupEntity(id, mockCallback);
		
		verify(mockCallback).onSuccess(entity);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testLoadEntityFail() throws Exception {
		String name = "name";
		String id = "syn456";
		Entity entity = new Data();
		entity.setId(id);
		entity.setName(name);
		AsyncCallback<Entity> mockCallback = mock(AsyncCallback.class);

		// 404
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getEntity(eq(id), any(AsyncCallback.class));		
		when(mockNodeModelCreator.createEntity(any(EntityWrapper.class))).thenReturn(entity);		
		entityFinder.lookupEntity(id, mockCallback);		
		verify(mockCallback).onFailure(any(Throwable.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_NOT_FOUND);		
		reset(mockCallback);
		reset(mockView);
		
		// 403
		AsyncMockStubber.callFailureWith(new ForbiddenException()).when(mockSynapseClient).getEntity(eq(id), any(AsyncCallback.class));		
		when(mockNodeModelCreator.createEntity(any(EntityWrapper.class))).thenReturn(entity);		
		entityFinder.lookupEntity(id, mockCallback);
		verify(mockCallback).onFailure(any(Throwable.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_FAILURE_PRIVLEDGES);
		reset(mockCallback);
		reset(mockView);

		// other
		AsyncMockStubber.callFailureWith(new Exception()).when(mockSynapseClient).getEntity(eq(id), any(AsyncCallback.class));		
		when(mockNodeModelCreator.createEntity(any(EntityWrapper.class))).thenReturn(entity);		
		entityFinder.lookupEntity(id, mockCallback);
		verify(mockCallback).onFailure(any(Throwable.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_GENERIC);
		reset(mockCallback);
		reset(mockView);
	}
	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testLoadVersions() throws Exception {
//		String id = "syn456";
//		String versionsJson = "versions";
//		PaginatedResults<VersionInfo> paginated = new PaginatedResults<VersionInfo>();		
//		List<VersionInfo> results = new ArrayList<VersionInfo>();
//		paginated.setResults(results);
//		AsyncMockStubber.callSuccessWith(versionsJson).when(mockSynapseClient).getEntityVersions(eq(id), anyInt(), anyInt(), any(AsyncCallback.class));		
//		AsyncCallback<Entity> mockCallback = mock(AsyncCallback.class);	
//		Mockito.<PaginatedResults<?>>when(mockNodeModelCreator.createPaginatedResults(anyString(), VersionInfo.class)).thenReturn(paginated);
//		
//		entityFinder.loadVersions(id);
//		
//		verify(mockView).setVersions(results);
//	}	
	
}

