package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static junit.framework.Assert.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererUserId;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableInitializedColumnRenderer;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class APITableColumnRendererUserIDTest {
		
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	SynapseJSNIUtils mockJsniUtils;
	APITableColumnRendererUserId renderer;
	Map<String, List<String>> columnData;
	APITableColumnConfig config;
	AsyncCallback<APITableInitializedColumnRenderer> mockCallback;
	String inputColumnName = "name";
	String inputValue = "8888888";
	String firstName = "Philip";
	String lastName = "Fry";
	
	@Before
	public void setup() throws JSONObjectAdapterException{
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockJsniUtils = mock(SynapseJSNIUtils.class);
		
		UserGroupHeaderResponsePage responsePage = new UserGroupHeaderResponsePage();
		List<UserGroupHeader> userGroupHeaders = new ArrayList<UserGroupHeader>();
		UserGroupHeader ugh = new UserGroupHeader();
		ugh.setOwnerId(inputValue);
		ugh.setFirstName(firstName);
		ugh.setLastName(lastName);
		userGroupHeaders.add(ugh);
		responsePage.setChildren(userGroupHeaders);
		
		AsyncMockStubber.callSuccessWith(responsePage).when(mockSynapseClient).getUserGroupHeadersById(any(ArrayList.class), any(AsyncCallback.class));
		
		renderer = new APITableColumnRendererUserId(mockSynapseClient, mockNodeModelCreator, mockJsniUtils);
		columnData = new HashMap<String, List<String>>();
		config = new APITableColumnConfig();
		HashSet<String> inputColumnNames = new HashSet<String>();
		inputColumnNames.add(inputColumnName);
		config.setInputColumnNames(inputColumnNames);
		mockCallback = mock(AsyncCallback.class);
		APITableTestUtils.setInputValue(inputValue, inputColumnName, columnData);
	}
	
	@Test
	public void testInitHappy() {
		renderer.init(columnData, config, mockCallback);
		APITableInitializedColumnRenderer initializedRenderer = APITableTestUtils.getInitializedRenderer(mockCallback);
		assertTrue(initializedRenderer.getColumnData().containsKey(inputColumnName));
		//rendered value should contain the first and last name (obtained from the synapse client service for the input user id)
		String userHtml = initializedRenderer.getColumnData().get(inputColumnName).get(0);
		assertTrue(userHtml.contains(firstName));
		assertTrue(userHtml.contains(lastName));
	}
	
	@Test
	public void testInitNull() {
		APITableTestUtils.setInputValue(null, inputColumnName, columnData);
		renderer.init(columnData, config, mockCallback);
		APITableInitializedColumnRenderer initializedRenderer = APITableTestUtils.getInitializedRenderer(mockCallback);
		//null value should be rendered as an empty string
		assertEquals("", initializedRenderer.getColumnData().get(inputColumnName).get(0));
	}
	
	@Test
	public void testInitNotFoundUserId() {
		String unavailableUserProfileId = "11111";
		APITableTestUtils.setInputValue(unavailableUserProfileId, inputColumnName, columnData);
		renderer.init(columnData, config, mockCallback);
		APITableInitializedColumnRenderer initializedRenderer = APITableTestUtils.getInitializedRenderer(mockCallback);
		String userHtml = initializedRenderer.getColumnData().get(inputColumnName).get(0);
		//no first or last name, but it should give us back the ID we used as input
		assertTrue(userHtml.contains(unavailableUserProfileId));
	}
	
	@Test
	public void testGetUserGroupHeadersFailure() {
		Exception thrownException = new Exception("unhandled");
		AsyncMockStubber.callFailureWith(thrownException).when(mockSynapseClient).getUserGroupHeadersById(any(ArrayList.class), any(AsyncCallback.class));
		renderer.init(columnData, config, mockCallback);
		verify(mockCallback).onFailure(eq(thrownException));
	}
	//Callback.onFailure is never called.  An empty column is shown if the data are unavailable for the specified column.
}
