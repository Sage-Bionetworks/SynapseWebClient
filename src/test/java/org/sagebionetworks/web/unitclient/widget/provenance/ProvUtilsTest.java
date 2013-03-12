package org.sagebionetworks.web.unitclient.widget.provenance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.LayoutServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.provenance.ProvUtils;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

public class ProvUtilsTest {
		
	ProvenanceWidget provenanceWidget;
	ProvenanceWidgetView mockView;
	AuthenticationController mockAuthController;
	NodeModelCreator mockNodeModelCreator;
	AdapterFactory adapterFactory;
	SynapseClientAsync mockSynapseClient;
	LayoutServiceAsync mockLayoutService;
	SynapseJSNIUtils synapseJsniUtils = implJSNIUtils();	
	
	@Before
	public void setup(){		
		mockView = mock(ProvenanceWidgetView.class);
		mockAuthController = mock(AuthenticationController.class);
		mockNodeModelCreator = mock(NodeModelCreator.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockLayoutService = mock(LayoutServiceAsync.class);		
		adapterFactory = new AdapterFactoryImpl();				
	}
		
	@SuppressWarnings("unchecked")
	@Test
	public void testBuildProvGraph() throws Exception {
		Data entity1 = new Data();
		entity1.setId("syn123");
		entity1.setVersionNumber(1L);
		Data entity2 = new Data();
		entity2.setId("syn456");
		entity2.setVersionNumber(1L);
		Activity act = new Activity();
		act.setId("789");

		Reference ref1 = new Reference();
		ref1.setTargetId(entity1.getId());
		ref1.setTargetVersionNumber(entity1.getVersionNumber());
		EntityHeader header1 = new EntityHeader();
		header1.setId(ref1.getTargetId());
		header1.setVersionNumber(ref1.getTargetVersionNumber());
		
		Reference ref2 = new Reference();
		ref2.setTargetId(entity2.getId());
		ref2.setTargetVersionNumber(entity2.getVersionNumber());
		EntityHeader header2 = new EntityHeader();
		header2.setId(ref2.getTargetId());
		header2.setVersionNumber(ref2.getTargetVersionNumber());
		
		UsedEntity ue = new UsedEntity();
		ue.setReference(ref2);
		Set<UsedEntity> used = new HashSet<UsedEntity>();
		used.add(ue);
		act.setUsed(used);

		List<Activity> activities = new ArrayList<Activity>();
		activities.add(act);
		Map<String, ProvGraphNode> idToNode = new HashMap<String, ProvGraphNode>();
		Map<Reference, EntityHeader> refToHeader = new HashMap<Reference, EntityHeader>();
		refToHeader.put(ref1, header1);
		refToHeader.put(ref2, header2);
		
		Map<Reference, String> generatedByActivityId = new HashMap<Reference, String>();
		generatedByActivityId.put(ref1, act.getId());

		Map<String, Activity> processedActivities = new HashMap<String, Activity>();
		processedActivities.put(act.getId(), act);		
		
		Set<Reference> startRefs = new HashSet<Reference>();
		startRefs.add(ref1);
		
		Set<Reference> noExpandNodes = new HashSet<Reference>();
		
		ProvGraph graph = ProvUtils.buildProvGraph(generatedByActivityId, processedActivities, idToNode, refToHeader, false, startRefs, noExpandNodes);		
		
		assertNotNull(graph.getNodes());
		assertNotNull(graph.getEdges());		
		Set<ProvGraphNode> nodes = graph.getNodes();
		Set<ProvGraphEdge> edges = graph.getEdges();
		
		// verify all nodes created
		
		EntityGraphNode entity1Node = null;
		EntityGraphNode entity2Node = null;
		ActivityGraphNode actNode = null;
		for(ProvGraphNode node : nodes) {			
			if(node instanceof EntityGraphNode) {
				if(((EntityGraphNode)node).getEntityId().equals(entity1.getId())) entity1Node = (EntityGraphNode) node;
				if(((EntityGraphNode)node).getEntityId().equals(entity2.getId())) entity2Node = (EntityGraphNode) node;
			} else if(node instanceof ActivityGraphNode) {
				if(((ActivityGraphNode)node).getActivityId().equals(act.getId())) actNode = (ActivityGraphNode) node;
			}
		}
		assertNotNull(entity1Node);
		assertNotNull(entity2Node);
		assertNotNull(actNode);
		
		// verify all edges created
		ProvGraphEdge generatedByEdge = new ProvGraphEdge(entity1Node, actNode);
		assertTrue(edges.contains(generatedByEdge));
		ProvGraphEdge usedEdge = new ProvGraphEdge(actNode, entity2Node);
		assertTrue(edges.contains(usedEdge));
	}

	public void testCreateUniqueNodeId() throws Exception {
		Integer sequence = 0;
		Map<String, ProvGraphNode> idToNode = new HashMap<String, ProvGraphNode>();
		for(int i=0; i<10; i++) {
			String next = ProvUtils.createUniqueNodeId();
			assertFalse(idToNode.containsKey(next));
			idToNode.put(next, null);
		}		
	}
	
	public void testExtractReferences() {
		String entId1 = "syn123";
		String entId2 = "syn456";
		Activity act = new Activity();
		act.setId("789");
		Reference ref = new Reference();
		ref.setTargetId(entId1);
		Reference ref2 = new Reference();
		ref2.setTargetId(entId2);

		Set<UsedEntity> used = new HashSet<UsedEntity>();
		UsedEntity ue;
		ue = new UsedEntity();
		ue.setReference(ref);
		used.add(ue);
		ue = new UsedEntity();
		ue.setReference(ref2);
		used.add(ue);		
		
		act.setUsed(used);

		List<Activity> activities = new ArrayList<Activity>();
		activities.add(act);
		
		List<Reference> refs = ProvUtils.extractReferences(activities);
		
		assertTrue(refs.contains(ref));
		assertTrue(refs.contains(ref2));
	}
	
	public void testMapReferencesToHeaders() throws Exception {
		Reference ref = new Reference();
		ref.setTargetId("syn456");
		ref.setTargetVersionNumber(1L);
		EntityHeader header = new EntityHeader();
		header.setId(ref.getTargetId());
		header.setVersionNumber(ref.getTargetVersionNumber());
		UsedEntity ue = new UsedEntity();
		ue.setReference(ref);
		BatchResults<EntityHeader> referenceHeaders = new BatchResults<EntityHeader>();
		referenceHeaders.setResults(new ArrayList<EntityHeader>(Arrays.asList(new EntityHeader[] { header })));
				
		Map<Reference, EntityHeader> refToHeader = ProvUtils.mapReferencesToHeaders(referenceHeaders);
		assertTrue(refToHeader.containsKey(ref));
		assertEquals(header, refToHeader.get(ref));
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
				return "";
			}
			
			@Override
			public LayoutResult nChartlayout(NChartLayersArray layers, NChartCharacters characters) {
				return null;
			}
			@Override
			public void setPageDescription(String newDescription) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void setPageTitle(String newTitle) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	
}












