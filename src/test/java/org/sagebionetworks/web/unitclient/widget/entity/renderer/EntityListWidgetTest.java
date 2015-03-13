package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertEquals;
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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.EntityGroupRecord;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.EntityGroupRecordDisplay;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListUtil;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.EntityListWidgetView;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.safehtml.shared.SafeHtml;
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
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(syn456);
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseClient).getEntityBundle(eq(syn456.getId()), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseClient).getEntityBundleForVersion(eq(syn456.getId()), eq(1L), anyInt(), any(AsyncCallback.class));

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
				
		widget.configure(null, descriptor, null, null);
		
		verify(mockView).configure();	
		verify(mockView).setEntityGroupRecordDisplay(eq(0), any(EntityGroupRecordDisplay.class), eq(true));
	}
	
	@Test
	public void testUtilLoadIndividualRowDetailsDeprecatedDescriptionField() {
		List<EntityGroupRecord> records = new ArrayList<EntityGroupRecord>();
		records.add(record456);
		EntityListUtil.RowLoadedHandler handler = mock(EntityListUtil.RowLoadedHandler.class);
		
		EntityListUtil.loadIndividualRowDetails(mockSynapseClient, mockSynapseJSNIUtils,
					mockNodeModelCreator, mockAuthenticationController.isLoggedIn(),
					records, 0, handler);
		verify(handler).onLoaded(any(EntityGroupRecordDisplay.class));
		
		// Since syn456 is depracated (syn456 instanceof locationable), the call
		// on getPlainTextWikiPage should never have been made.
		verify(mockSynapseClient, Mockito.never()).getPlainTextWikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
	}
	
	@Test
	@Ignore	// Ignoring as we reverted to old behavior. Un-ignore when new behavoir is re-implemented.
	public void testUtilLoadIndividualRowDetailsWikiDescription() throws Exception {
		// create non-deprecated entity
		FileEntity syn789 = new FileEntity();
		syn789.setId("syn789");
		syn789.setName(syn789.getId());
		EntityBundle bundle = new EntityBundle();
		bundle.setEntity(syn789);
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseClient).getEntityBundle(eq(syn789.getId()), anyInt(), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(bundle).when(mockSynapseClient).getEntityBundleForVersion(eq(syn789.getId()), eq(1L), anyInt(), any(AsyncCallback.class));
		
		// create an entity group record for syn789
		List<EntityGroupRecord> records = new ArrayList<EntityGroupRecord>();
		EntityGroupRecord record789 = new EntityGroupRecord();
		Reference ref = new Reference();
		ref.setTargetId(syn789.getId());
		ref.setTargetVersionNumber(1L);
		record789.setEntityReference(ref);
		records.add(record789);
		
		EntityListUtil.RowLoadedHandler handler = mock(EntityListUtil.RowLoadedHandler.class);

		// Set up success for call to get wiki text.
		String resultDescription = "Description =)";
		AsyncMockStubber.callSuccessWith(resultDescription).when(mockSynapseClient).getPlainTextWikiPage(any(WikiPageKey.class), any(AsyncCallback.class));

		EntityListUtil.loadIndividualRowDetails(mockSynapseClient, mockSynapseJSNIUtils,
					mockNodeModelCreator, mockAuthenticationController.isLoggedIn(),
					records, 0, handler);

		// Since syn789 is non-deprecated, the call on
		// getPlainTextWikiPage should have been made once.
		verify(mockSynapseClient).getPlainTextWikiPage(any(WikiPageKey.class), any(AsyncCallback.class));
		
		// The wiki description was used.
		ArgumentCaptor<EntityGroupRecordDisplay> arg = ArgumentCaptor.forClass(EntityGroupRecordDisplay.class);
		verify(handler).onLoaded(arg.capture());
		
		// proper description was in fact sent to row details.
		assertEquals("syn789", arg.getValue().getEntityId());
		assertEquals(resultDescription, arg.getValue().getDescription().asString());
	}
}
