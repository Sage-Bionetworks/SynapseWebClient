package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.browse.EntityBrowserUtils;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityBrowserUtilsTest {

	SynapseClientAsync mockSynapseClient;
	AdapterFactory adapterFactory = new AdapterFactoryImpl();
	GlobalApplicationState mockGlobalApplicationState;
	
	@Before
	public void before() {
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
	}
	
	@Test
	public void testLoadFavorites() throws Exception {
		int total = 2;
		ArrayList<String> results = new ArrayList<String>();
		EntityHeader h1 = new EntityHeader();
		h1.setId("1");
		EntityHeader h2 = new EntityHeader();
		h2.setId("2");
		results.add(h1.writeToJSONObject(adapterFactory.createNew()).toJSONString());
		results.add(h2.writeToJSONObject(adapterFactory.createNew()).toJSONString());
		
		AsyncMockStubber.callSuccessWith(results).when(mockSynapseClient).getFavoritesList(anyInt(), anyInt(), any(AsyncCallback.class));
		AsyncCallback<List<EntityHeader>> callback = mock(AsyncCallback.class);
		EntityBrowserUtils.loadFavorites(mockSynapseClient, adapterFactory, mockGlobalApplicationState, callback);
		ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
		verify(callback).onSuccess(argument.capture());
		List<EntityHeader> returned = argument.getValue();
		assertEquals(2, returned.size());
		assertTrue(returned.contains(h1));
		assertTrue(returned.contains(h2));
		
	}
}
