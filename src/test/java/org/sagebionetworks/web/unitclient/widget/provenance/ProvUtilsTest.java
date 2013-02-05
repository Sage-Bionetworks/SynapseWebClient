package org.sagebionetworks.web.unitclient.widget.provenance;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

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
import org.mockito.internal.verification.Times;
import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Data;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONEntity;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.LayoutServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.provenance.ProvUtils;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidgetView;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.provenance.ActivityTreeNode;
import org.sagebionetworks.web.shared.provenance.EntityTreeNode;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.dev.util.collect.HashSet;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
	public void testBuildProvTree() throws Exception {
		Data entity = new Data();
		entity.setId("syn123");
		entity.setVersionNumber(1L);
		Data entity2 = new Data();
		entity2.setId("syn456");
		entity2.setVersionNumber(1L);
		Activity act = new Activity();
		act.setId("789");
		Reference ref = new Reference();
		ref.setTargetId(entity2.getId());
		ref.setTargetVersionNumber(entity2.getVersionNumber());
		EntityHeader header = new EntityHeader();
		header.setId(ref.getTargetId());
		header.setVersionNumber(ref.getTargetVersionNumber());
		UsedEntity ue = new UsedEntity();
		ue.setReference(ref);
		Set<UsedEntity> used = new HashSet<UsedEntity>();
		used.add(ue);
		act.setUsed(used);

		List<Activity> activities = new ArrayList<Activity>();
		activities.add(act);
		Map<String, ProvTreeNode> idToNode = new HashMap<String, ProvTreeNode>();
		Map<Reference, EntityHeader> refToHeader = new HashMap<Reference, EntityHeader>();
		refToHeader.put(ref, header);		
		
		ProvTreeNode root = ProvUtils.buildProvTree(activities, entity, idToNode, refToHeader, false, synapseJsniUtils);		
		
		assertEquals(root, idToNode.get(root.getId()));
		assertEquals(entity.getId(), ((EntityTreeNode)root).getEntityId());
		assertTrue(root.iterator().hasNext());

		ActivityTreeNode actNode = (ActivityTreeNode)root.iterator().next();
		assertEquals(actNode, idToNode.get(actNode.getId()));
		assertEquals(act.getId(), actNode.getActivityId());
		assertTrue(actNode.iterator().hasNext());
		
		EntityTreeNode entity2Node = (EntityTreeNode)actNode.iterator().next();
		assertEquals(entity2Node, idToNode.get(entity2Node.getId()));
		assertEquals(entity2.getId(), entity2Node.getEntityId());
		assertFalse(entity2Node.iterator().hasNext());		
	}

	public void testCreateUniqueNodeId() throws Exception {		
		Map<String, ProvTreeNode> idToNode = new HashMap<String, ProvTreeNode>();
		for(int i=0; i<10; i++) {
			String next = ProvUtils.createUniqueNodeId(idToNode, synapseJsniUtils);
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
		};
	}

	
}












