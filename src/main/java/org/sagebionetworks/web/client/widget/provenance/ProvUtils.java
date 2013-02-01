package org.sagebionetworks.web.client.widget.provenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.ActivityTypeUtil;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProvUtils {

	public static ProvGraph buildProvGraph(
			Map<Reference, String> generatedByActivityId,
			Map<String, Activity> processedActivities,
			Map<String, ProvGraphNode> idToNode,
			Map<Reference, EntityHeader> refToHeader, boolean showExpand,
			SynapseJSNIUtils synapseJSNIUtils) {
		ProvGraph graph = new ProvGraph();
		Map<Reference,EntityGraphNode> entityNodes = new HashMap<Reference,EntityGraphNode>();
		Map<Activity,ActivityGraphNode> activityNodes = new HashMap<Activity,ActivityGraphNode>();
		
		// create ProvGraphNodes for the entities
		for(Reference ref : refToHeader.keySet()) {
			EntityHeader header = refToHeader.get(ref);
			if(header == null) header = new EntityHeader();
			EntityGraphNode entityNode = new EntityGraphNode(createUniqueNodeId(
					idToNode, synapseJSNIUtils), ref.getTargetId(),
					header.getName(), header.getVersionLabel(),
					header.getVersionNumber(), header.getType(), false);
			idToNode.put(entityNode.getId(), entityNode);
			graph.addNode(entityNode);
		}
		
		// create ProvGraphNodes for the activities
		// add used edges
		// create Expand nodes and add edges to used nodes if requested
		for(Activity act : processedActivities.values()) {			
			ActivityType type = ActivityTypeUtil.get(act);
			ActivityGraphNode activityNode = new ActivityGraphNode(
						createUniqueNodeId(idToNode, synapseJSNIUtils), 
						act.getId(),
						act.getName(), 
						type);			
			idToNode.put(activityNode.getId(), activityNode);
			graph.addNode(activityNode);
			
			// add used edges
			if(act.getUsed() != null) {
				Iterator<UsedEntity> itr = act.getUsed().iterator();
				while(itr.hasNext()) {
					UsedEntity ue = itr.next();					
					Reference ref = ue.getReference();			
					EntityGraphNode entityNode = entityNodes.get(ref);
					graph.addEdge(new ProvGraphEdge(activityNode, entityNode));				
					
					if(showExpand) {
						ProvGraphNode expandNode = new ExpandGraphNode(createUniqueNodeId(idToNode, synapseJSNIUtils), ref.getTargetId(), ref.getTargetVersionNumber());
						idToNode.put(expandNode.getId(), expandNode);
						graph.addEdge(new ProvGraphEdge(entityNode, expandNode));
					}
				}
				
			}
		}
		
		// add generatedBy edges
		for(Reference ref : generatedByActivityId.keySet()) {			
			String activityId = generatedByActivityId.get(ref);
			Activity act = processedActivities.get(activityId);
			if(act != null) {
				graph.addEdge(new ProvGraphEdge(entityNodes.get(ref), activityNodes.get(act)));
			}
		}
				
		return graph;
	}

	
	/**
	 * Creates a random id that is not in use yet 
	 */
	public static String createUniqueNodeId(Map<String, ProvGraphNode> idToNode, SynapseJSNIUtils synapseJSNIUtils) {
		String id;
		do{
			id = "provNode" + String.valueOf(synapseJSNIUtils.randomNextInt());
		} while(idToNode.containsKey(id));
		return id;
	}

	public static List<Reference> extractReferences(List<Activity> activities) {
		List<Reference> allRefs = new ArrayList<Reference>();
		if(activities == null) return allRefs; 
		for(Activity act : activities) {
			if(act.getUsed() == null) continue;
		
			Iterator<UsedEntity> itr = act.getUsed().iterator();
			while(itr.hasNext()) {
				UsedEntity ue = itr.next();
				if(ue != null && ue.getReference() != null) {
					allRefs.add(ue.getReference());
				}
			}
		}
		return allRefs;
	}
	
	public static Map<Reference, EntityHeader> mapReferencesToHeaders(BatchResults<EntityHeader> headers) {
		Map<Reference, EntityHeader> refToHeader = new HashMap<Reference, EntityHeader>();
		for(EntityHeader header : headers.getResults()) {
			Reference equivalentRef = new Reference();
			equivalentRef.setTargetId(header.getId());
			equivalentRef.setTargetVersionNumber(header.getVersionNumber());
			refToHeader.put(equivalentRef, header);
		}
		return refToHeader;
	}

	public static KeyValueDisplay<String> entityToKeyValueDisplay(Entity entity) {
		Map<String,String> map = new HashMap<String, String>();
		List<String> order = new ArrayList<String>();
		
		order.add("Name");
		map.put("Name", entity.getName());		
		
		if(entity instanceof Versionable) {
			order.add("Version");
			map.put("Version", DisplayUtils.getVersionDisplay((Versionable)entity));
		}
		order.add("Modified By");
		map.put("Modified By", entity.getModifiedBy());
		
		order.add("Modified On");
		map.put("Modified On", DisplayUtils.converDataToPrettyString(entity.getModifiedOn()));
		
		order.add("Description");
		map.put("Description", entity.getDescription());		
		
		return new KeyValueDisplay<String>(map, order);
	}

	public static KeyValueDisplay<String> activityToKeyValueDisplay(Activity activity) {
		Map<String,String> map = new HashMap<String, String>();
		List<String> order = new ArrayList<String>();
			
		order.add("Name");
		map.put("Name", activity.getName());				

		UsedEntity executed = ActivityTypeUtil.getExecuted(activity);
		if(executed != null) {
			order.add("Executed Entity");
			map.put("Executed Entity", DisplayUtils.getVersionDisplay(executed.getReference()));
		}
		
		String used = null;
		Iterator<UsedEntity> itr = activity.getUsed().iterator();
		while(itr.hasNext()) {
			Reference ref = itr.next().getReference();
			if(used == null) {
				used = DisplayUtils.getVersionDisplay(ref);
			} else {
				used += ", " + DisplayUtils.getVersionDisplay(ref);
			}
		}
		order.add("Entities Used");
		map.put("Entities Used", used);
		
		order.add("Modified By");
		map.put("Modified By", activity.getModifiedBy());
		
		order.add("Modified On");
		map.put("Modified On", DisplayUtils.converDataToPrettyString(activity.getModifiedOn()));		
		
		order.add("Description");
		map.put("Description", activity.getDescription());				
		
		return new KeyValueDisplay<String>(map, order);
	}

	/**
	 * Returns a KeyValueDisplay to the callback for the given ProvGraphNode nodeId (NOTE: nodeId is not entity id!)
	 * @param nodeId ProvGraphNode id
	 * @param callback
	 * @param synapseClient
	 * @param nodeModelCreator
	 * @param idToNode mapping from id to ProvGraphNode
	 */
	public static void getInfo(String nodeId,			
			SynapseClientAsync synapseClient,
			final NodeModelCreator nodeModelCreator,
			Map<String, ProvGraphNode> idToNode,
			final AsyncCallback<KeyValueDisplay<String>> callback) {
		if(callback == null) return;
		if(nodeId == null) callback.onFailure(null);
				
		ProvGraphNode node = idToNode.get(nodeId);
		if(node == null) callback.onFailure(null);
		
		if(node instanceof EntityGraphNode) {
			getInfoEntityTreeNode(synapseClient, nodeModelCreator, callback, (EntityGraphNode)node);
		} else if(node instanceof ActivityGraphNode) { 
			getInfoActivityTreeNode(synapseClient, nodeModelCreator, callback, (ActivityGraphNode)node);
		}
	}
	
	
	/*
	 * Private Methods
	 */
	private static void getInfoActivityTreeNode(
			SynapseClientAsync synapseClient,
			final NodeModelCreator nodeModelCreator,
			final AsyncCallback<KeyValueDisplay<String>> callback,
			ActivityGraphNode atNode) {
		synapseClient.getActivity(atNode.getActivityId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					Activity activity = nodeModelCreator.createJSONEntity(result, Activity.class);
					callback.onSuccess(ProvUtils.activityToKeyValueDisplay(activity));			
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}		
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	private static void getInfoEntityTreeNode(SynapseClientAsync synapseClient,
			final NodeModelCreator nodeModelCreator,
			final AsyncCallback<KeyValueDisplay<String>> callback,
			EntityGraphNode etNode) {
		synapseClient.getEntityForVersion(etNode.getEntityId(), etNode.getVersionNumber(), new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				Entity entity;
				try {
					entity = nodeModelCreator.createEntity(result);
					callback.onSuccess(ProvUtils.entityToKeyValueDisplay(entity));
				} catch (JSONObjectAdapterException e) {
					onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
}





