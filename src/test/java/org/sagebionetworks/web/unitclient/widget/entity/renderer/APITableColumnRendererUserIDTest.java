package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererUserId;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableInitializedColumnRenderer;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class APITableColumnRendererUserIDTest {

	@Mock
	GWTWrapper mockGWT;
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
		MockitoAnnotations.initMocks(this);
		
		List<UserProfile> listProfiles = new ArrayList<UserProfile>();
		UserProfile profile = new UserProfile();
		profile.setOwnerId(inputValue);
		profile.setFirstName(firstName);
		profile.setLastName(lastName);
		listProfiles.add(profile);
		
		renderer = new APITableColumnRendererUserId(mockGWT);
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
}
