package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidgetView;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityListWidgetTest {
		
	EntityListWidget widget;
	EntityListWidgetView mockView;
	SynapseClientAsync mockSynapseClient;
	NodeModelCreator mockNodeModelCreator;
	SynapseJSNIUtils mockSynapseJSNIUtils;
	AuthenticationController mockAuthenticationController;

	Map<String, String> descriptor;
	Data syn456;
	EntityGroupRecord record456; 
	
	@Before
	public void setup() throws Exception{		
		mockView = mock(EntityListWidgetView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapseJSNIUtils = mock(SynapseJSNIUtils.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);

		// create gettable entity
		syn456 = new Data();
		syn456.setId("syn456");
		syn456.setName(syn456.getId());
		EntityBundle bundle = new EntityBundle(syn456, null, null, null, null, null, null);
		EntityBundleTransport transport = new EntityBundleTransport();
		AsyncMockStubber.callSuccessWith(transport).when(mockSynapseClient).getEntityBundle(eq(syn456.getId()), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(transport).when(mockSynapseClient).getEntityBundleForVersion(eq(syn456.getId()), eq(1L), anyInt(), any(AsyncCallback.class));
		when(mockNodeModelCreator.createEntityBundle(transport)).thenReturn(bundle);

		// create an entity group record for syn456
		record456 = new EntityGroupRecord();
		Reference ref = new Reference();
		ref.setTargetId(syn456.getId());
		ref.setTargetVersionNumber(1L);
		record456.setEntityReference(ref);
		
		// create empty descriptor
		descriptor = new HashMap<String, String>();		
				
		widget = new EntityListWidget(mockView, mockSynapseClient, mockNodeModelCreator, mockSynapseJSNIUtils, mockAuthenticationController);
	}
	
	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
	
	@Test
	public void testConfigure() {		
		List<EntityGroupRecord> records = new ArrayList<EntityGroupRecord>();
		records.add(record456);			
		String encoded = EntityListUtil.recordsToString(records);
		descriptor.put(WidgetConstants.ENTITYLIST_WIDGET_LIST_KEY, encoded);
				
		widget.configure(null, descriptor, null);
		
		verify(mockView).configure();	
		verify(mockView).setEntityGroupRecordDisplay(eq(0), any(EntityGroupRecordDisplay.class), eq(true));

	}
}
