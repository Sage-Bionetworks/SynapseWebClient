package org.sagebionetworks.web.unitclient.widget.entity.browse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.entity.browse.EntityBrowserUtils;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityBrowserUtilsTest {

	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testLoadFavorites() throws Exception {
		int total = 2;
		ArrayList<EntityHeader> results = new ArrayList<EntityHeader>();
		EntityHeader h1 = new EntityHeader();
		h1.setId("1");
		EntityHeader h2 = new EntityHeader();
		h2.setId("2");
		results.add(h1);
		results.add(h2);

		AsyncMockStubber.callSuccessWith(results).when(mockSynapseJavascriptClient).getFavorites(any(AsyncCallback.class));
		AsyncCallback<List<EntityHeader>> callback = mock(AsyncCallback.class);
		EntityBrowserUtils.loadFavorites(mockSynapseJavascriptClient, mockGlobalApplicationState, callback);
		ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
		verify(callback).onSuccess(argument.capture());
		List<EntityHeader> returned = argument.getValue();
		assertEquals(2, returned.size());
		assertTrue(returned.contains(h1));
		assertTrue(returned.contains(h2));

	}

	@Test
	public void testSortEntityHeadersByName() {
		EntityHeader header1 = new EntityHeader();
		EntityHeader header2 = new EntityHeader();
		EntityHeader header3 = new EntityHeader();
		header1.setName("Abra");
		header2.setName("marill");
		header3.setName("Zubat");

		List<EntityHeader> list = new ArrayList<EntityHeader>();
		list.add(header3);
		list.add(header2);
		list.add(header1);

		// [Zubat, marill, Abra] --> [Abra, marill, Zubat]
		EntityBrowserUtils.sortEntityHeadersByName(list);

		assertEquals(list.get(0), header1);
		assertEquals(list.get(1), header2);
		assertEquals(list.get(2), header3);
	}
}
