package org.sagebionetworks.web.client.widget.provenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.VersionableEntity;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.Used;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.repo.model.provenance.UsedURL;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.ActivityTypeUtil;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ExternalGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphEdge;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;

public class ProvUtils {

	private static int sequence = 0;

	public static ProvGraph buildProvGraph(Map<Reference, String> generatedByActivityId, Map<String, Activity> processedActivities, Map<String, ProvGraphNode> idToNode, Map<Reference, EntityHeader> refToHeader, boolean showExpand, Set<Reference> startRefs, Set<Reference> noExpandNode) {
		ProvGraph graph = new ProvGraph();
		Set<ProvGraphNode> nodeHasExpandNode = new HashSet<ProvGraphNode>();

		// maps for local building retrieval
		Map<Reference, EntityGraphNode> entityNodes = new HashMap<Reference, EntityGraphNode>();
		Map<UsedURL, ExternalGraphNode> externalGraphNodes = new HashMap<UsedURL, ExternalGraphNode>();
		Map<Activity, ActivityGraphNode> activityNodes = new HashMap<Activity, ActivityGraphNode>();

		// create ProvGraphNodes for the entities
		for (Reference ref : refToHeader.keySet()) {
			EntityHeader header = refToHeader.get(ref);
			if (header == null) {
				header = new EntityHeader();
			}
			boolean isStartNode = startRefs.contains(ref) ? true : false;
			String name = header.getName() == null ? ref.getTargetId() : header.getName();
			EntityGraphNode entityNode = new EntityGraphNode(createUniqueNodeId(), ref.getTargetId(), name, header.getVersionLabel(), ref.getTargetVersionNumber(), header.getType(), false, isStartNode);
			idToNode.put(entityNode.getId(), entityNode);
			graph.addNode(entityNode);
			entityNodes.put(ref, entityNode);
		}

		// create ProvGraphNodes for the activities
		// add used edges
		// create Expand nodes and add edges to used nodes if requested
		for (Activity act : processedActivities.values()) {
			ActivityType type = ActivityTypeUtil.get(act);
			ActivityGraphNode activityNode = new ActivityGraphNode(createUniqueNodeId(), act.getId(), act.getName(), type, act.getModifiedBy(), act.getModifiedOn(), false);
			idToNode.put(activityNode.getId(), activityNode);
			graph.addNode(activityNode);
			activityNodes.put(act, activityNode);

			// add used edges
			if (act.getUsed() != null) {
				Iterator<Used> itr = act.getUsed().iterator();
				while (itr.hasNext()) {
					Used used = itr.next();
					if (used instanceof UsedEntity) {
						UsedEntity ue = (UsedEntity) used;
						Reference ref = ue.getReference();
						EntityGraphNode entityNode = entityNodes.get(ref);
						graph.addEdge(new ProvGraphEdge(activityNode, entityNode));

						// create expand nodes for those that don't have generatedBy activities defined
						// also, if entity header name is null, do not create expand
						EntityHeader refHeader = refToHeader.containsKey(ref) ? refToHeader.get(ref) : new EntityHeader();
						if (showExpand && !generatedByActivityId.containsKey(ref) && !noExpandNode.contains(ref) && !nodeHasExpandNode.contains(entityNode) && refHeader.getName() != null) {
							ProvGraphNode expandNode = new ExpandGraphNode(createUniqueNodeId(), ref.getTargetId(), ref.getTargetVersionNumber());
							idToNode.put(expandNode.getId(), expandNode);
							graph.addEdge(new ProvGraphEdge(entityNode, expandNode));
							nodeHasExpandNode.add(entityNode);
						}
					} else if (used instanceof UsedURL) {
						UsedURL ue = (UsedURL) used;
						ExternalGraphNode externalGraphNode = externalGraphNodes.get(ue);
						if (externalGraphNode == null) {
							externalGraphNode = new ExternalGraphNode(createUniqueNodeId(), ue.getName(), ue.getUrl(), ue.getWasExecuted());
							idToNode.put(externalGraphNode.getId(), externalGraphNode);
							graph.addNode(externalGraphNode);
							externalGraphNodes.put(ue, externalGraphNode);
						}

						graph.addEdge(new ProvGraphEdge(activityNode, externalGraphNode));
					}
				}

			}
		}

		// add generatedBy edges
		for (Reference ref : generatedByActivityId.keySet()) {
			String activityId = generatedByActivityId.get(ref);
			Activity act = processedActivities.get(activityId);
			if (act != null) {
				graph.addEdge(new ProvGraphEdge(entityNodes.get(ref), activityNodes.get(act)));
			}
		}

		return graph;
	}

	/**
	 * Creates a random id that is not in use yet
	 */
	public static String createUniqueNodeId() {
		return "provNode" + sequence++;
	}

	public static List<Reference> extractReferences(List<Activity> activities) {
		List<Reference> allRefs = new ArrayList<Reference>();
		if (activities == null)
			return allRefs;
		for (Activity act : activities) {
			if (act.getUsed() == null)
				continue;

			for (Used used : act.getUsed()) {
				if (used instanceof UsedEntity) {
					UsedEntity ue = (UsedEntity) used;
					if (ue != null && ue.getReference() != null) {
						allRefs.add(ue.getReference());
					}
				}
			}
		}
		return allRefs;
	}

	public static Map<Reference, EntityHeader> mapReferencesToHeaders(ArrayList<EntityHeader> headers) {
		Map<Reference, EntityHeader> refToHeader = new HashMap<Reference, EntityHeader>();
		for (EntityHeader header : headers) {
			Reference equivalentRef = new Reference();
			equivalentRef.setTargetId(header.getId());
			equivalentRef.setTargetVersionNumber(header.getVersionNumber());
			refToHeader.put(equivalentRef, header);
		}
		return refToHeader;
	}

	public static KeyValueDisplay<String> entityToKeyValueDisplay(Entity entity, String modifiedBy, DateTimeUtils dateTimeUtils) {
		Map<String, String> map = new HashMap<String, String>();
		List<String> order = new ArrayList<String>();

		order.add("ID");
		map.put("ID", entity.getId());

		order.add("Name");
		map.put("Name", entity.getName());

		if (entity instanceof VersionableEntity) {
			order.add("Version");
			map.put("Version", DisplayUtils.getVersionDisplay((VersionableEntity) entity));
		}
		order.add("Modified By");
		map.put("Modified By", modifiedBy);

		order.add("Modified On");

		if (entity.getModifiedOn() != null)
			map.put("Modified On", dateTimeUtils.getLongFriendlyDate(entity.getModifiedOn()));

		order.add("Description");
		map.put("Description", entity.getDescription());

		return new KeyValueDisplay<String>(map, order);
	}

	public static KeyValueDisplay<String> activityToKeyValueDisplay(Activity activity, String modifiedBy, DateTimeUtils dateTimeUtils) {
		Map<String, String> map = new HashMap<String, String>();
		List<String> order = new ArrayList<String>();

		order.add("Name");
		map.put("Name", activity.getName());

		order.add("Activity id");
		map.put("Activity id", activity.getId());

		order.add("Modified By");
		map.put("Modified By", modifiedBy);

		order.add("Modified On");
		map.put("Modified On", dateTimeUtils.getLongFriendlyDate(activity.getModifiedOn()));

		order.add("Description");
		map.put("Description", activity.getDescription());

		return new KeyValueDisplay<String>(map, order);
	}

	public static KeyValueDisplay<String> externalNodeToKeyValueDisplay(ExternalGraphNode externalNode) {
		Map<String, String> map = new HashMap<String, String>();
		List<String> order = new ArrayList<String>();

		order.add("Name");
		map.put("Name", externalNode.getName());

		order.add("URL");
		map.put("URL", externalNode.getUrl());

		return new KeyValueDisplay<String>(map, order);
	}
}


