package org.sagebionetworks.web.client.widget.provenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.shared.EntityWrapper;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.provenance.ActivityTreeNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.ActivityTypeUtil;
import org.sagebionetworks.web.shared.provenance.EntityTreeNode;
import org.sagebionetworks.web.shared.provenance.ExpandTreeNode;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;

import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProvUtils {

	public static ProvTreeNode buildProvTree(List<Activity> activities, Entity rootEntity, Map<String, ProvTreeNode> idToNode, Map<Reference, EntityHeader> refToHeader, boolean showExpand) {
		String versionLabel = null;
		Long versionNumber = null;
		if(rootEntity instanceof Versionable) {
			versionLabel = ((Versionable)rootEntity).getVersionLabel();
			versionNumber = ((Versionable)rootEntity).getVersionNumber();
		}
		ProvTreeNode root = new EntityTreeNode(rootEntity.getId(), rootEntity.getId(), rootEntity.getName(), versionLabel, versionNumber, rootEntity.getEntityType());
		idToNode.put(root.getId(), root);		
		for(Activity act : activities) {			
			ProvTreeNode activityNode;
			UsedEntity firstExecuted = null;
			ActivityType type = ActivityTypeUtil.get(act);
			if(type == ActivityType.CODE_EXECUTION) {
				firstExecuted = ActivityTypeUtil.getExecuted(act);				
				EntityHeader header = firstExecuted != null ? refToHeader.get(firstExecuted.getReference()) : null;
				if(header == null) header = new EntityHeader();
				activityNode = new ActivityTreeNode(
						createUniqueNodeId(idToNode), 
						act.getId(),
						act.getName(), 
						type, 
						header.getId(), 
						header.getName(),
						header.getVersionLabel(), 
						header.getVersionNumber(),
						header.getType());
			} else {
				activityNode = new ActivityTreeNode(
						createUniqueNodeId(idToNode), 
						act.getId(),
						act.getName(), 
						type);
			}
			idToNode.put(activityNode.getId(), activityNode);
			// add activity to tree
			root.addChild(activityNode);
			
			if(act.getUsed() != null) {
				Iterator<UsedEntity> itr = act.getUsed().iterator();
				while(itr.hasNext()) {
					UsedEntity ue = itr.next();
					if(firstExecuted != null && firstExecuted.equals(ue)) continue; // don't show the first executed in the used list
					Reference ref = ue.getReference();
					EntityHeader header = refToHeader.get(ref);
					if(header == null) header = new EntityHeader();
					ProvTreeNode usedNode = new EntityTreeNode(createUniqueNodeId(idToNode), ref.getTargetId(), header.getName(), header.getVersionLabel(), header.getVersionNumber(), header.getType());
					idToNode.put(usedNode.getId(), usedNode);
					// add used to activity in tree
					activityNode.addChild(usedNode);
					
					if(showExpand) {
						ProvTreeNode expandNode = new ExpandTreeNode(createUniqueNodeId(idToNode), ref.getTargetId(), ref.getTargetVersionNumber());
						idToNode.put(expandNode.getId(), expandNode);
						usedNode.addChild(expandNode);
					}
				}
			}
		}
		return root;
	}
	
	/**
	 * Creates a random id that is not in use yet 
	 */
	public static String createUniqueNodeId(Map<String, ProvTreeNode> idToNode) {
		String id;
		do{
			id = "provNode" + String.valueOf(Random.nextInt());
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
	
	public static void mapReferencesToHeaders(BatchResults<EntityHeader> headers, Map<Reference, EntityHeader> refToHeader) {		
		for(EntityHeader header : headers.getResults()) {
			Reference equivalentRef = new Reference();
			equivalentRef.setTargetId(header.getId());
			equivalentRef.setTargetVersionNumber(header.getVersionNumber());
			refToHeader.put(equivalentRef, header);
		}
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
	 * Returns a KeyValueDisplay to the callback for the given ProvTreeNode nodeId (NOTE: nodeId is not entity id!)
	 * @param nodeId ProvTreeNode id
	 * @param callback
	 * @param synapseClient
	 * @param nodeModelCreator
	 * @param idToNode mapping from id to ProvTreeNode
	 */
	public static void getInfo(String nodeId,
			final AsyncCallback<KeyValueDisplay<String>> callback,
			SynapseClientAsync synapseClient,
			final NodeModelCreator nodeModelCreator,
			Map<String, ProvTreeNode> idToNode) {
		if(callback == null) return;
		if(nodeId == null) callback.onFailure(null);
				
		ProvTreeNode node = idToNode.get(nodeId);
		if(node == null) callback.onFailure(null);
		
		if(node instanceof EntityTreeNode) {
			getInfoEntityTreeNode(synapseClient, nodeModelCreator, callback, (EntityTreeNode)node);
		} else if(node instanceof ActivityTreeNode) { 
			getInfoActivityTreeNode(synapseClient, nodeModelCreator, callback, (ActivityTreeNode)node);
		}
	}
	
	
	/*
	 * Private Methods
	 */
	private static void getInfoActivityTreeNode(
			SynapseClientAsync synapseClient,
			final NodeModelCreator nodeModelCreator,
			final AsyncCallback<KeyValueDisplay<String>> callback,
			ActivityTreeNode atNode) {
		synapseClient.getActivity(atNode.getActivityId(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					Activity activity = nodeModelCreator.createEntity(result, Activity.class);
					callback.onSuccess(ProvUtils.activityToKeyValueDisplay(activity));			
				} catch (RestServiceException e) {
					onFailure(e);
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
			EntityTreeNode etNode) {
		synapseClient.getEntityForVersion(etNode.getEntityId(), etNode.getVersionNumber(), new AsyncCallback<EntityWrapper>() {
			@Override
			public void onSuccess(EntityWrapper result) {
				if(result.getRestServiceException() != null) {
					onFailure(result.getRestServiceException());
					return;
				}
				Entity entity;
				try {
					entity = nodeModelCreator.createEntity(result);
					callback.onSuccess(ProvUtils.entityToKeyValueDisplay(entity));
				} catch (RestServiceException e) {
					onFailure(e);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	
}





