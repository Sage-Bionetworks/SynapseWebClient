package org.sagebionetworks.web.unitclient.widget.provenance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.AccessControlList;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.auth.UserEntityPermissions;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.Used;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.callback.MD5Callback;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.LayoutServiceAsync;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.unitclient.widget.provenance.nchart.JsoProviderTestImpl;

import com.google.gwt.dev.util.collect.HashSet;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class ProvenanceWidgetTest {
		
	ProvenanceWidget provenanceWidget;
	ProvenanceWidgetView mockView;
	AuthenticationController mockAuthController;
	NodeModelCreator mockNodeModelCreator;
	AdapterFactory adapterFactory;
	SynapseClientAsync mockSynapseClient;
	LayoutServiceAsync mockLayoutService;
	SynapseJSNIUtils synapseJsniUtils = implJSNIUtils();	
	
	Data outputEntity;
	String entity456Id = "syn456";
	BatchResults<EntityHeader> referenceHeaders;
	String activityJSON;
	String referenceListJSON;
	String referenceHeadersJSON;
	Exception someException = new Exception();
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	JsoProvider jsoProvider;
	Map<String, String> descriptor;
	Activity act;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws Exception {		
		mockView = mock(ProvenanceWidgetView.class);
		mockAuthController = mock(AuthenticationController.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockLayoutService = mock(LayoutServiceAsync.class);
		adapterFactory = new AdapterFactoryImpl();
		jsoProvider = new JsoProviderTestImpl();

		provenanceWidget = new ProvenanceWidget(mockView, mockSynapseClient, mockNodeModelCreator, mockAuthController, mockLayoutService, adapterFactory, synapseJsniUtils, jsoProvider);
		verify(mockView).setPresenter(provenanceWidget);
		
		outputEntity = new Data();
		outputEntity.setId("syn123");
		outputEntity.setVersionNumber(1L);
		act = new Activity();
		act.setId("789");
		Reference ref123 = new Reference();
		ref123.setTargetId(outputEntity.getId());
		ref123.setTargetVersionNumber(outputEntity.getVersionNumber());
		Reference ref456 = new Reference();
		ref456.setTargetId(entity456Id);
		ref456.setTargetVersionNumber(1L);
		EntityHeader header123 = new EntityHeader();
		header123.setId(outputEntity.getId());
		header123.setVersionNumber(outputEntity.getVersionNumber());
		EntityHeader header456 = new EntityHeader();
		header456.setId(ref456.getTargetId());
		header456.setVersionNumber(ref456.getTargetVersionNumber());
		UsedEntity ue = new UsedEntity();
		ue.setReference(ref456);
		Set<Used> used = new HashSet<Used>();
		used.add(ue);
		act.setUsed(used);
		ReferenceList referenceList = new ReferenceList();
		referenceList.setReferences(new ArrayList<Reference>(Arrays.asList(new Reference[] { ref123, ref456 })));
		referenceHeaders = new BatchResults<EntityHeader>();
		referenceHeaders.setResults(new ArrayList<EntityHeader>(Arrays.asList(new EntityHeader[] { header456, header123 })));
		
		String generatedResult = "generatedPaginatedResults";
		PaginatedResults<Reference> generatedBy = new PaginatedResults<Reference>();
		generatedBy.setResults(Arrays.asList(new Reference[] { ref123 }));		
						
		EntityWrapper ew = new EntityWrapper(outputEntity.writeToJSONObject(adapterFactory.createNew()).toJSONString(), Data.class.getName());		
		activityJSON = act.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		referenceListJSON = referenceList.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		referenceHeadersJSON = referenceHeaders.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		
		AsyncMockStubber.callSuccessWith(ew).when(mockSynapseClient).getEntity(eq(outputEntity.getId()), any(AsyncCallback.class));
		when(mockNodeModelCreator.createEntity(ew)).thenReturn(outputEntity);
		AsyncMockStubber.callSuccessWith(activityJSON).when(mockSynapseClient).getActivityForEntityVersion(eq(outputEntity.getId()), eq(outputEntity.getVersionNumber()), any(AsyncCallback.class));
		AsyncMockStubber.callSuccessWith(referenceHeadersJSON).when(mockSynapseClient).getEntityHeaderBatch(anyString(), any(AsyncCallback.class));		
		Mockito.<BatchResults<?>>when(mockNodeModelCreator.createBatchResults(anyString(), eq(EntityHeader.class))).thenReturn((BatchResults<EntityHeader>)referenceHeaders);
		AsyncMockStubber.callSuccessWith(generatedResult).when(mockSynapseClient).getEntitiesGeneratedBy(eq(act.getId()), anyInt(), anyInt(), any(AsyncCallback.class));
		Mockito.<PaginatedResults<?>>when(mockNodeModelCreator.createPaginatedResults(generatedResult, Reference.class)).thenReturn(generatedBy);
		
		
		descriptor = new HashMap<String, String>();
		String depth = "1";
		String showExpand = "true";
		String displayHeight = "500";
		descriptor.put(WidgetConstants.PROV_WIDGET_DEPTH_KEY, depth);
		descriptor.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, outputEntity.getId());
		descriptor.put(WidgetConstants.PROV_WIDGET_EXPAND_KEY, showExpand);
		descriptor.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, displayHeight);		
		descriptor.put(WidgetConstants.PROV_WIDGET_UNDEFINED_KEY, Boolean.toString(true));
	}
	
	@Test
	public void testAsWidget(){
		provenanceWidget.asWidget();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildGraphSuccess() throws Exception {				
		provenanceWidget.configure(null, descriptor, null);	
		ProvGraph graph = verifyBuildGraphCalls();					
		verifySuccessGraphStructure(graph);
	}

	private void verifySuccessGraphStructure(ProvGraph graph) {
		assertNotNull(graph);
		ProvGraphNode node123 = null;
		ProvGraphNode node456 = null;
		ProvGraphNode nodeAct = null;
		for(ProvGraphNode node : graph.getNodes()) {
			if(node instanceof EntityGraphNode) {
				if(((EntityGraphNode) node).getEntityId().equals(outputEntity.getId())) 
					node123 = node;
				else if(((EntityGraphNode) node).getEntityId().equals(entity456Id))
					node456 = node;
			} else if(node instanceof ActivityGraphNode) {
				if(((ActivityGraphNode) node).getActivityId().equals(act.getId()))
					nodeAct = node;
			}
		}
		assertNotNull(node123);
		assertNotNull(node456);
		assertNotNull(nodeAct);
		
		assertTrue(graph.getEdges().contains(new ProvGraphEdge(node123, nodeAct)));
		assertTrue(graph.getEdges().contains(new ProvGraphEdge(nodeAct, node456)));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildTreeFailGetActivity() throws Exception {
		AsyncMockStubber.callFailureWith(someException).when(mockSynapseClient).getActivityForEntityVersion(eq(outputEntity.getId()), eq(outputEntity.getVersionNumber()), any(AsyncCallback.class));
		
		provenanceWidget.configure(null, descriptor, null);	
		verify(mockSynapseClient).getActivityForEntityVersion(eq(outputEntity.getId()), eq(outputEntity.getVersionNumber()), any(AsyncCallback.class));		
		ProvGraph graph = captureGraph();
		
		assertNotNull(graph);
		assertEquals(1, graph.getEdges().size());		
		assertEquals(3, graph.getNodes().size());
		ProvGraphNode node123 = null;
		ActivityGraphNode nodeAct = null;
		for(ProvGraphNode node : graph.getNodes()) {
			if(node instanceof EntityGraphNode) {
				if(((EntityGraphNode) node).getEntityId().equals(outputEntity.getId())) 
					node123 = node;
			} else if(node instanceof ActivityGraphNode) {				
					nodeAct = (ActivityGraphNode) node;
			}
		}
		assertNotNull(node123);
		assertNotNull(nodeAct);
		assertEquals(DisplayConstants.ERROR_PROVENANCE_RELOAD, nodeAct.getActivityName());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBuildTreeFailGetActivity404() throws Exception {
		AsyncMockStubber.callFailureWith(new NotFoundException()).when(mockSynapseClient).getActivityForEntityVersion(eq(outputEntity.getId()), eq(outputEntity.getVersionNumber()), any(AsyncCallback.class));
		
		provenanceWidget.configure(null, descriptor, null);	
		verify(mockSynapseClient).getActivityForEntityVersion(eq(outputEntity.getId()), eq(outputEntity.getVersionNumber()), any(AsyncCallback.class));
		verify(mockSynapseClient).getEntityHeaderBatch(anyString(), any(AsyncCallback.class));
		ProvGraph graph = captureGraph();
		
		assertNotNull(graph);
		assertEquals(1, graph.getEdges().size());		
		assertEquals(3, graph.getNodes().size());
		ProvGraphNode node123 = null;
		ActivityGraphNode nodeAct = null;
		for(ProvGraphNode node : graph.getNodes()) {
			if(node instanceof EntityGraphNode) {
				if(((EntityGraphNode) node).getEntityId().equals(outputEntity.getId())) 
					node123 = node;
			} else if(node instanceof ActivityGraphNode) {				
					nodeAct = (ActivityGraphNode) node;
			}
		}
		assertNotNull(node123);
		assertNotNull(nodeAct);
		assertTrue(nodeAct.getActivityName().contains(DisplayConstants.NOT_FOUND));
	}	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildTreeFailHeaderBatch() throws Exception {
		AsyncMockStubber.callFailureWith(someException).when(mockSynapseClient).getEntityHeaderBatch(anyString(), any(AsyncCallback.class));
		
		provenanceWidget.configure(null, descriptor, null);	
		verify(mockSynapseClient).getActivityForEntityVersion(eq(outputEntity.getId()), eq(outputEntity.getVersionNumber()), any(AsyncCallback.class));
		verify(mockSynapseClient).getEntityHeaderBatch(eq(referenceListJSON), any(AsyncCallback.class));
		ProvGraph graph = captureGraph();

		verifySuccessGraphStructure(graph);
	}
		
	@Test
	public void testFindOldVersions() throws Exception {
		// create graph
		provenanceWidget.configure(null, descriptor, null);	
		ProvGraph graph = verifyBuildGraphCalls();					
		
		reset(mockSynapseClient);
		reset(mockNodeModelCreator);
		
		// current version of each reference
		EntityHeader header123 = new EntityHeader();
		header123.setId(outputEntity.getId());
		header123.setVersionNumber(outputEntity.getVersionNumber());
		EntityHeader header456 = new EntityHeader();
		header456.setId(entity456Id);
		header456.setVersionNumber(2L); // v2 is newer than in Before method
		BatchResults<EntityHeader> currentVersionBatch = new BatchResults<EntityHeader>();
		currentVersionBatch.setResults(new ArrayList<EntityHeader>(Arrays.asList(new EntityHeader[] { header456, header123 })));		
		String currentVersionsBatchJSON = referenceHeaders.writeToJSONObject(adapterFactory.createNew()).toJSONString();
		
		// new mocks for batch call
		AsyncMockStubber.callSuccessWith(currentVersionsBatchJSON).when(mockSynapseClient).getEntityHeaderBatch(anyString(), any(AsyncCallback.class));		
		Mockito.<BatchResults<?>>when(mockNodeModelCreator.createBatchResults(anyString(), eq(EntityHeader.class))).thenReturn((BatchResults<EntityHeader>)currentVersionBatch);

		provenanceWidget.findOldVersions();
		
		@SuppressWarnings("rawtypes")
		ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);		
		verify(mockView).markOldVersions(argument.capture());		
		@SuppressWarnings("unchecked")
		List<String> oldVersions = (List<String>)argument.getValue();
		
		assertEquals(1, oldVersions.size());		
	}


	/*
	 * Private Methods
	 */
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

	private ProvGraph verifyBuildGraphCalls() throws Exception {
		verify(mockSynapseClient).getActivityForEntityVersion(eq(outputEntity.getId()), eq(outputEntity.getVersionNumber()), any(AsyncCallback.class));
		verify(mockSynapseClient).getEntityHeaderBatch(eq(referenceListJSON), any(AsyncCallback.class));		
		return captureGraph();
	}

	private ProvGraph captureGraph() {
		ArgumentCaptor<ProvGraph> argument = ArgumentCaptor.forClass(ProvGraph.class);		
		verify(mockView).setGraph(argument.capture());
		ProvGraph graph = argument.getValue();
		return graph;
	}

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
				return "";
			}

			@Override
			public LayoutResult nChartlayout(NChartLayersArray layers,
					NChartCharacters characters) {
				return jsoProvider.newLayoutResult();
			}
			@Override
			public void setPageDescription(String newDescription) {
				
			}
			
			@Override
			public void setPageTitle(String newTitle) {};
			
			@Override
			public void tablesorter(String id) {}
			@Override
			public boolean isDirectUploadSupported() {
				return false;
			}
			@Override
			public String getContentType(String fileFieldId) {
				// TODO Auto-generated method stub
				return null;
			}
			@Override
			public void uploadFileChunk(String contentType, String fileFieldId,
					int startByte, int endByte, String url, XMLHttpRequest xhr,
					ProgressCallback callback) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public double getFileSize(String fileFieldId) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public void uploadUrlToGenomeSpace(String url) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void uploadUrlToGenomeSpace(String url, String filename) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void getFileMd5(String fileFieldId, MD5Callback callback) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void processWithMathJax(Element element) {
				// TODO Auto-generated method stub
				
			}
		};
	}

}












