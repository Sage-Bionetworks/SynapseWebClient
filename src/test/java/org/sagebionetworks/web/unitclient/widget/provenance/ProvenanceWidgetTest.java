package org.sagebionetworks.web.unitclient.widget.provenance;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.LayoutServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.provenance.EntityTreeNode;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.dev.util.collect.HashSet;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProvenanceWidgetTest {
		
	ProvenanceWidget provenanceWidget;
	ProvenanceWidgetView mockView;
	AuthenticationController mockAuthController;
	NodeModelCreator mockNodeModelCreator;
	AdapterFactory adapterFactory;
	SynapseClientAsync mockSynapseClient;
	LayoutServiceAsync mockLayoutService;
	SynapseJSNIUtils synapseJsniUtils = implJSNIUtils();	
	
	Data entity;
	ProvTreeNode root;
	BatchResults<EntityHeader> referenceHeaders;
	String activityJSON;
	String referenceListJSON;
	String referenceHeadersJSON;
	Exception someException = new Exception();
	WikiPageKey wikiKey = new WikiPageKey("", WidgetConstants.WIKI_OWNER_ID_ENTITY, null);
	
	@Before
	public void setup() throws Exception {		
		mockView = mock(ProvenanceWidgetView.class);
		mockAuthController = mock(AuthenticationController.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockLayoutService = mock(LayoutServiceAsync.class);
		adapterFactory = new AdapterFactoryImpl();

		provenanceWidget = new ProvenanceWidget(mockView, mockSynapseClient, mockNodeModelCreator, mockAuthController, mockLayoutService, adapterFactory, synapseJsniUtils);
		verify(mockView).setPresenter(provenanceWidget);
		
		entity = new Data();
		entity.setId("syn123");
		entity.setVersionNumber(1L);
		Activity act = new Activity();
		act.setId("789");
		Reference ref = new Reference();
		ref.setTargetId("syn456");
		ref.setTargetVersionNumber(1L);
		EntityHeader header = new EntityHeader();
		header.setId(ref.getTargetId());
		header.setVersionNumber(ref.getTargetVersionNumber());
		UsedEntity ue = new UsedEntity();
		ue.setReference(ref);
		Set<UsedEntity> used = new HashSet<UsedEntity>();
		used.add(ue);
		act.setUsed(used);
		ReferenceList referenceList = new ReferenceList();
		referenceList.setReferences(new ArrayList<Reference>(Arrays.asList(new Reference[] { ref })));
		referenceHeaders = new BatchResults<EntityHeader>();
		referenceHeaders.setResults(new ArrayList<EntityHeader>(Arrays.asList(new EntityHeader[] { header })));
		
		root = new EntityTreeNode("someid", null, null, null, null, null);
		
		activityJSON = act.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		referenceListJSON = referenceList.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		referenceHeadersJSON = referenceHeaders.writeToJSONObject(adapterFactory.createNew()).toJSONString();

		AsyncMockStubber.callSuccessWith(activityJSON).when(mockSynapseClient).getActivityForEntityVersion(eq(entity.getId()), eq(entity.getVersionNumber()), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(referenceHeadersJSON).when(mockSynapseClient).getEntityHeaderBatch(anyString(), any(AsyncCallback.class));		
		Mockito.<BatchResults<?>>when(mockNodeModelCreator.createBatchResults(anyString(), eq(EntityHeader.class))).thenReturn((BatchResults<EntityHeader>)referenceHeaders);
		AsyncMockStubber.callSuccessWith(root).when(mockLayoutService).layoutProvTree(any(ProvTreeNode.class), any(AsyncCallback.class));

	}
	
	@Test
	public void testAsWidget(){
		provenanceWidget.asWidget();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildTreeSuccess() throws Exception {		
		provenanceWidget.buildTree(entity, 1, false);	
		verifyBuildTreeCalls();
	}

	private void verifyBuildTreeCalls() throws Exception {
		verify(mockSynapseClient).getActivityForEntityVersion(eq(entity.getId()), eq(entity.getVersionNumber()), any(AsyncCallback.class));
		verify(mockSynapseClient).getEntityHeaderBatch(eq(referenceListJSON), any(AsyncCallback.class));
		verify(mockLayoutService).layoutProvTree(any(ProvTreeNode.class), any(AsyncCallback.class));
		verify(mockView).setTree(root);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildTreeFailGetActivity() throws Exception {
		AsyncMockStubber.callFailureWith(someException).when(mockSynapseClient).getActivityForEntityVersion(eq(entity.getId()), eq(entity.getVersionNumber()), any(AsyncCallback.class));
		
		provenanceWidget.buildTree(entity, 1, false);	
		verify(mockSynapseClient).getActivityForEntityVersion(eq(entity.getId()), eq(entity.getVersionNumber()), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_PROVENANCE);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBuildTreeFailGetActivity404() throws Exception {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getActivityForEntityVersion(eq(entity.getId()), eq(entity.getVersionNumber()), any(AsyncCallback.class));
		
		provenanceWidget.buildTree(entity, 1, false);	
		verify(mockSynapseClient).getActivityForEntityVersion(eq(entity.getId()), eq(entity.getVersionNumber()), any(AsyncCallback.class));
		verify(mockSynapseClient).getEntityHeaderBatch(anyString(), any(AsyncCallback.class));
		verify(mockLayoutService).layoutProvTree(any(ProvTreeNode.class), any(AsyncCallback.class));
		verify(mockView).setTree(root);
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildTreeFailHeaderBatch() throws Exception {
		AsyncMockStubber.callFailureWith(someException).when(mockSynapseClient).getEntityHeaderBatch(anyString(), any(AsyncCallback.class));
		
		provenanceWidget.buildTree(entity, 1, false);	
		verify(mockSynapseClient).getActivityForEntityVersion(eq(entity.getId()), eq(entity.getVersionNumber()), any(AsyncCallback.class));
		verify(mockSynapseClient).getEntityHeaderBatch(eq(referenceListJSON), any(AsyncCallback.class));
		verify(mockLayoutService).layoutProvTree(any(ProvTreeNode.class), any(AsyncCallback.class));
		verify(mockView).setTree(root);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBuildTreeFailLayout() throws Exception {
		AsyncMockStubber.callFailureWith(someException).when(mockLayoutService).layoutProvTree(any(ProvTreeNode.class), any(AsyncCallback.class));
		
		provenanceWidget.buildTree(entity, 1, false);	
		verify(mockSynapseClient).getActivityForEntityVersion(eq(entity.getId()), eq(entity.getVersionNumber()), any(AsyncCallback.class));
		verify(mockSynapseClient).getEntityHeaderBatch(eq(referenceListJSON), any(AsyncCallback.class));
		verify(mockLayoutService).layoutProvTree(any(ProvTreeNode.class), any(AsyncCallback.class));
		verify(mockView).showErrorMessage(DisplayConstants.ERROR_LAYOUT);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testConfigure() throws Exception {
		//verify that calling configure eventually does the same thing as buildTree
		EntityBundleTransport ebt = new EntityBundleTransport();
		when(mockNodeModelCreator.createEntityBundle(ebt)).thenReturn(new EntityBundle(entity, null, null, null, null, null, null, null));
		
		AsyncMockStubber.callSuccessWith(ebt).when(mockSynapseClient).getEntityBundle(anyString(), anyInt(), any(AsyncCallback.class));
		Map<String, String> widgetDescriptor = new HashMap<String, String>();
		widgetDescriptor.put(WidgetConstants.PROV_WIDGET_DEPTH_KEY, "42");
		widgetDescriptor.put(WidgetConstants.PROV_WIDGET_ENTITY_ID_KEY, entity.getId());
		widgetDescriptor.put(WidgetConstants.PROV_WIDGET_EXPAND_KEY, "true");
		provenanceWidget.configure(wikiKey,  widgetDescriptor);
		verifyBuildTreeCalls();
	}
	
	private static EntityBundleTransport createEBT(JSONEntity entity, AccessControlList acl, UserEntityPermissions uep) {
		try {
			EntityBundleTransport ebt = new EntityBundleTransport();
			ebt.setEntityJson(EntityFactory.createJSONStringForEntity(entity));
			ebt.setAclJson(EntityFactory.createJSONStringForEntity(acl));
			ebt.setPermissionsJson(EntityFactory.createJSONStringForEntity(uep));
			return ebt;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	
	/*
	 * Private Methods
	 */
	private SynapseJSNIUtils implJSNIUtils() {
		return new SynapseJSNIUtils() {
			Random rand = new Random();
			
			@Override
			public void recordPageVisit(String token) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public int randomNextInt() {
				return rand.nextInt();
			}
			
			@Override
			public void highlightCodeBlocks() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void hideBootstrapTooltip(String id) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public String getCurrentHistoryToken() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String getBaseProfileAttachmentUrl() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public String convertDateToSmallString(Date toFormat) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void bindBootstrapTooltip(String id) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void bindBootstrapPopover(String id) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public String getLocationPath() {
				return "/Portal.html";
			}

			@Override
			public String getLocationQueryString() {
				return "?foo=bar";
			}
			
			@Override
			public String getBaseFileHandleUrl() {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}

}












