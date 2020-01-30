package org.sagebionetworks.web.client.widget.provenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.Used;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.transform.JsoProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.provenance.nchart.LayoutResult;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartCharacters;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartLayersArray;
import org.sagebionetworks.web.client.widget.provenance.nchart.NChartUtil;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceWidget implements ProvenanceWidgetView.Presenter, WidgetRendererPresenter {

	private static final int DEFAULT_DEPTH_KEY = 1;
	private static final String FAKE_ID_PREFIX = "fakeId";
	private static final int DEFAULT_HEIGHT = 270;
	private ProvenanceWidgetView view;
	SynapseJavascriptClient jsClient;
	private Map<String, ProvGraphNode> idToNode = new HashMap<String, ProvGraphNode>();
	private SynapseJSNIUtils synapseJSNIUtils;
	private Map<String, String> descriptor;
	private JsoProvider jsoProvider;

	private interface ProcessCallback {
		public void onComplete();
	}

	Map<Reference, String> generatedByActivityId = new HashMap<Reference, String>();
	Map<String, Activity> processedActivities = new HashMap<String, Activity>();
	Set<Reference> references = new HashSet<Reference>();
	Map<Reference, EntityHeader> refToHeader = new HashMap<Reference, EntityHeader>();
	Set<Reference> startRefs;
	boolean showExpand;
	boolean showUndefinedAndErrorActivity;
	int maxDepth = 1;
	Stack<ProcessItem> toProcess;
	ProcessCallback doneCallback;
	Set<Reference> noExpandNode;
	List<String> lookupVersion;
	ProvGraph currentGraph;
	ClientCache clientCache;
	DateTimeUtils dateTimeUtils;
	SynapseAlert synAlert;

	@Inject
	public ProvenanceWidget(ProvenanceWidgetView view, SynapseJSNIUtils synapseJSNIUtils, JsoProvider jsoProvider, ClientCache clientCache, DateTimeUtils dateTimeUtils, SynapseJavascriptClient jsClient, SynapseAlert synAlert) {
		this.view = view;
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.jsoProvider = jsoProvider;
		this.clientCache = clientCache;
		this.dateTimeUtils = dateTimeUtils;
		this.jsClient = jsClient;
		this.synAlert = synAlert;
		view.setSynAlert(synAlert);
		view.setPresenter(this);
	}

	public void configure(Map<String, String> widgetDescriptor) {
		configure(null, widgetDescriptor, null, null);
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		view.setPresenter(this);
		view.showLoading();
		// set up view based on descriptor parameters
		descriptor = widgetDescriptor;

		// parse referenced entities and put into start refs
		lookupVersion = new ArrayList<String>();
		startRefs = new HashSet<Reference>();
		String entityListStr = null;
		if (descriptor.containsKey(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY))
			entityListStr = descriptor.get(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY);
		if (entityListStr != null) {
			String[] refs = entityListStr.split(WidgetConstants.PROV_WIDGET_ENTITY_LIST_DELIMETER);
			if (refs != null) {
				for (String refString : refs) {
					// Only add valid References
					Reference ref = DisplayUtils.parseEntityVersionString(refString);
					if (ref != null && ref.getTargetId() != null) {
						if (ref.getTargetVersionNumber() == null) {
							// if any current versions are requested then we need to look them up before
							// proceeding.
							lookupVersion.add(ref.getTargetId());
						} else {
							startRefs.add(ref);
						}
					}
				}
			}
		}
		// backwards compatibility for original ProvenanceWidget API
		if (descriptor.containsKey(WidgetConstants.PROV_WIDGET_ENTITY_ID_KEY)) {
			lookupVersion.add(descriptor.get(WidgetConstants.PROV_WIDGET_ENTITY_ID_KEY));
		}

		// Set max depth (default 1), undefined (false) and expand (false)
		maxDepth = descriptor.containsKey(WidgetConstants.PROV_WIDGET_DEPTH_KEY) ? Integer.parseInt(descriptor.get(WidgetConstants.PROV_WIDGET_DEPTH_KEY)) : 1;
		showUndefinedAndErrorActivity = descriptor.get(WidgetConstants.PROV_WIDGET_UNDEFINED_KEY) != null ? Boolean.parseBoolean(descriptor.get(WidgetConstants.PROV_WIDGET_UNDEFINED_KEY)) : true;
		showExpand = descriptor.get(WidgetConstants.PROV_WIDGET_EXPAND_KEY) != null ? Boolean.parseBoolean(descriptor.get(WidgetConstants.PROV_WIDGET_EXPAND_KEY)) : false;
		if (descriptor.containsKey(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY)) {
			setHeight(Integer.parseInt(descriptor.get(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY)));
		} else {
			setHeight(330);
		}

		// do not create expand nodes for these (generally for previously
		// expanded/discovered nodes without an activity)
		noExpandNode = new HashSet<Reference>();
		synAlert.clear();
		// callback after all starting references have their current version looked up
		// (if Versionable)
		AsyncCallback<Void> lookupCurrentVersionCallback = new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				// load stack with starting nodes at depth 0
				toProcess = new Stack<ProvenanceWidget.ProcessItem>();
				for (Reference ref : startRefs) {
					toProcess.push(new ReferenceProcessItem(ref, 0));
				}

				doneCallback = new ProcessCallback() {
					@Override
					public void onComplete() {
						lookupReferencesThenBuildGraph();
					}
				};
				// start processing the stack of start references
				processNext();
			}

			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		};
		// lookup versions for the start references, if needed
		lookupVersion(lookupCurrentVersionCallback);
	}

	public void setHeight(int height) {
		view.setHeight(height);
	}

	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}

	@Override
	public void expand(ExpandGraphNode node) {
		showUndefinedAndErrorActivity = false; // don't show when in expand mode after first expand
		maxDepth = 1;
		Reference ref = new Reference();
		ref.setTargetId(node.getEntityId());
		ref.setTargetVersionNumber(node.getVersionNumber());
		toProcess.push(new ReferenceProcessItem(ref, 0));
		processNext();
	}

	/*
	 * Private Methods
	 */

	/**
	 * Recursively look up versions, adding the references to the startRefs until the lookupVersion
	 * stack is empty
	 * 
	 * @param doneCallback
	 */
	private void lookupVersion(final AsyncCallback<Void> doneCallback) {
		// single call to look up the latest version
		jsClient.getEntityHeaderBatch(lookupVersion, new AsyncCallback<ArrayList<EntityHeader>>() {
			@Override
			public void onFailure(Throwable caught) {
				doneCallback.onFailure(caught);
			}

			@Override
			public void onSuccess(ArrayList<EntityHeader> headers) {
				for (EntityHeader header : headers) {
					Reference ref = new Reference();
					ref.setTargetId(header.getId());
					if (header.getVersionNumber() != null) {
						ref.setTargetVersionNumber(header.getVersionNumber());
					}
					startRefs.add(ref);
				}
				doneCallback.onSuccess(null);
			}
		});
	}

	/**
	 * Process the next ProcessItem in the stack until we're done
	 */
	private void processNext() {
		if (toProcess.size() == 0) {
			doneCallback.onComplete();
			return;
		}

		ProcessItem next = toProcess.pop();
		if (next instanceof ReferenceProcessItem) {
			processUsed((ReferenceProcessItem) next);
		} else if (next instanceof ActivityProcessItem) {
			processActivity((ActivityProcessItem) next);
		}
	}

	private void processUsed(final ReferenceProcessItem item) {
		if (item.getReference() == null) {
			processNext();
			return;
		}
		// add entry to references
		references.add(item.getReference());

		// if at the max depth then do not lookup activity
		if (item.getDepth() >= maxDepth) {
			processNext();
			return;
		}

		// lookup generatedBy activity for ref
		jsClient.getActivityForEntityVersion(item.getReference().getTargetId(), item.getReference().getTargetVersionNumber(), new AsyncCallback<Activity>() {
			@Override
			public void onSuccess(Activity activity) {
				addActivityToStack(activity);
			}

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException && showExpand) {
					noExpandNode.add(item.getReference());
				}

				Activity activity = null;
				if (showUndefinedAndErrorActivity)
					activity = createErrorActivity(caught); // Display empty, fake provenance record
				addActivityToStack(activity);
			}

			private void addActivityToStack(Activity activity) {
				if (activity != null) {
					// add entry to generatedByActivityId
					generatedByActivityId.put(item.getReference(), activity.getId());
					// add activity to toProcess and process it as next level in the depth of graph
					toProcess.push(new ActivityProcessItem(activity, item.getDepth() + 1));
				}
				processNext();
			}
		});

	}

	private void processActivity(final ActivityProcessItem item) {
		// TODO : remove this check if depth becomes specified for each entity in the
		// start list
		// check if activity has already been processed
		if (processedActivities.containsKey(item.getActivity().getId())) {
			processNext();
			return;
		}

		cleanupCycles(item, references);

		// add activity to processedActivites
		processedActivities.put(item.getActivity().getId(), item.getActivity());

		// if this activity errored then no need to lookup its generatedBy or used list
		if (item.getActivity().getId().startsWith(FAKE_ID_PREFIX)) {
			processNext();
			return;
		}

		// Lookup entities generated by this activity
		jsClient.getEntitiesGeneratedBy(item.getActivity().getId(), Integer.MAX_VALUE, 0, new AsyncCallback<ArrayList<Reference>>() {
			@Override
			public void onSuccess(ArrayList<Reference> results) {
				// add references to generatedByActivityId & references
				if (results != null) {
					for (Reference ref : results) {
						generatedByActivityId.put(ref, item.getActivity().getId());
						references.add(ref);
					}
				}
				addUsedToStack();
			}

			@Override
			public void onFailure(Throwable caught) {
				addUsedToStack();
			}

			// process used list
			private void addUsedToStack() {
				// add used references to process stack
				if (item.getActivity().getUsed() != null) {
					Set<Used> used = item.getActivity().getUsed();
					for (Used u : used) {
						if (u instanceof UsedEntity) { // ignore UsedUrl, nothing to process
							UsedEntity ue = (UsedEntity) u;
							if (ue.getReference() != null) {
								toProcess.push(new ReferenceProcessItem(ue.getReference(), item.getDepth())); // same
								// depth
								// as
								// activity
							}
						}
					}
				}
				processNext();
			}
		});
	}

	public void cleanupCycles(ActivityProcessItem item, Set<Reference> references) {
		if (item.getActivity().getUsed() != null) {
			// SWC-3568 : avoid cycle that results in stack overflow during the creation of
			// the graph.
			// PLFM-4288: ignore empty references (any references in the used list that
			// contain no target Synapse ID).
			List<Used> previouslyProcessed = new ArrayList<Used>();
			Set<Used> used = item.getActivity().getUsed();
			for (Used u : used) {
				if (u instanceof UsedEntity) { // ignore UsedUrl, nothing to process
					UsedEntity ue = (UsedEntity) u;
					if (ue.getReference() != null && (references.contains(ue.getReference()) || ue.getReference().getTargetId() == null)) {
						previouslyProcessed.add(u);
					}
				}
			}
			// remove from used (to avoid cycle)
			used.removeAll(previouslyProcessed);
		}
	}

	private void lookupReferencesThenBuildGraph() {
		if (references.isEmpty()) {
			view.clear();
			return;
		}
		jsClient.getEntityHeaderBatchFromReferences(new ArrayList<Reference>(references), new AsyncCallback<ArrayList<EntityHeader>>() {
			@Override
			public void onSuccess(ArrayList<EntityHeader> headers) {
				refToHeader = ProvUtils.mapReferencesToHeaders(headers);
				buildGraphLayoutSendToView();
			}

			@Override
			public void onFailure(Throwable caught) {
				buildGraphLayoutSendToView();
			}
		});
	}

	private void buildGraphLayoutSendToView() {
		// make sure that any references that were not returned in the header list are
		// incorporated in the map
		for (Reference ref : references) {
			if (!refToHeader.containsKey(ref)) {
				EntityHeader header = new EntityHeader();
				header.setId(ref.getTargetId());
				header.setVersionNumber(ref.getTargetVersionNumber());
				refToHeader.put(ref, header);
			}
		}

		// build the tree, layout and render
		idToNode = new HashMap<String, ProvGraphNode>();
		ProvGraph graph = ProvUtils.buildProvGraph(generatedByActivityId, processedActivities, idToNode, refToHeader, showExpand, startRefs, noExpandNode);
		currentGraph = graph;

		NChartCharacters characters = NChartUtil.createNChartCharacters(jsoProvider, graph.getNodes());
		NChartLayersArray layerArray = NChartUtil.createLayers(jsoProvider, graph);
		LayoutResult layoutResult = synapseJSNIUtils.nChartlayout(layerArray, characters);
		NChartUtil.fillPositions(layoutResult, graph);
		NChartUtil.repositionExpandNodes(graph);
		view.setGraph(graph);
	}

	private Activity createErrorActivity(Throwable caught) {
		Activity activity = new Activity();
		activity.setId(FAKE_ID_PREFIX + synapseJSNIUtils.randomNextInt());
		if (caught instanceof NotFoundException) {
			activity.setName(DisplayConstants.ACTIVITY + " " + DisplayConstants.NOT_FOUND);
		} else if (caught instanceof ForbiddenException) {
			activity.setName(DisplayConstants.ACTIVITY + " " + DisplayConstants.UNAUTHORIZED);
		} else {
			activity.setName(DisplayConstants.ERROR_PROVENANCE_RELOAD);
		}
		return activity;
	}

	/*
	 * Stack Classes
	 */
	private abstract class ProcessItem {
		int depth;

		public int getDepth() {
			return depth;
		}

		public void setDepth(int depth) {
			this.depth = depth;
		}

	}

	public class ReferenceProcessItem extends ProcessItem {
		Reference reference;

		public ReferenceProcessItem(Reference reference, int depth) {
			super();
			this.setDepth(depth);
			this.reference = reference;
		}

		public Reference getReference() {
			return reference;
		}

		public void setReference(Reference reference) {
			this.reference = reference;
		}
	}

	public class ActivityProcessItem extends ProcessItem {
		Activity activity;

		public ActivityProcessItem(Activity activity, int depth) {
			super();
			this.setDepth(depth);
			this.activity = activity;
		}

		public Activity getActivity() {
			return activity;
		}

		public void setActivity(Activity activity) {
			this.activity = activity;
		}
	}

	@Override
	public void findOldVersions() {
		if (currentGraph == null)
			return;

		final Map<Reference, String> refToNodeId = new HashMap<Reference, String>();
		Set<Reference> entityIds = new HashSet<Reference>();

		// create reverse lookup of reference to node id and save set of entity ids
		for (ProvGraphNode node : currentGraph.getNodes()) {
			if (node instanceof EntityGraphNode) {
				if (((EntityGraphNode) node).getVersionNumber() != null) {
					Reference ref = new Reference();
					ref.setTargetId(((EntityGraphNode) node).getEntityId());
					ref.setTargetVersionNumber(((EntityGraphNode) node).getVersionNumber());
					refToNodeId.put(ref, node.getId());

					Reference currentVersionRef = new Reference();
					currentVersionRef.setTargetId(ref.getTargetId());
					entityIds.add(currentVersionRef);
				}
			}
		}

		// batch request all entity ids to get current version. Notify view of non
		// current versions
		synAlert.clear();
		jsClient.getEntityHeaderBatchFromReferences(new ArrayList<Reference>(entityIds), new AsyncCallback<ArrayList<EntityHeader>>() {
			@Override
			public void onSuccess(ArrayList<EntityHeader> currentVersions) {
				Map<String, Long> entityToCurrentVersion = new HashMap<String, Long>();
				for (EntityHeader header : currentVersions) {
					entityToCurrentVersion.put(header.getId(), header.getVersionNumber());
				}

				// find graph nodes that should be marked as not current version
				List<String> notCurrentNodeIds = new ArrayList<String>();
				for (Reference ref : refToNodeId.keySet()) {
					Long currrentVersion = entityToCurrentVersion.get(ref.getTargetId());
					if (ref.getTargetVersionNumber() != null && currrentVersion != null && !ref.getTargetVersionNumber().equals(currrentVersion)) {
						notCurrentNodeIds.add(refToNodeId.get(ref));
					}
				}
				view.markOldVersions(notCurrentNodeIds);
			}

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof NotFoundException) {
					// SWC-1843: do not redirect home. log full exception to the console
					synapseJSNIUtils.consoleError(caught.getMessage() + "\n" + DisplayUtils.getStackTrace(caught));
				} else {
					synAlert.handleException(caught);
				}
			}
		});
	}

	public static Map<String, String> getDefaultWidgetDescriptor() {
		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put(WidgetConstants.PROV_WIDGET_EXPAND_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_UNDEFINED_KEY, Boolean.toString(true));
		configMap.put(WidgetConstants.PROV_WIDGET_DEPTH_KEY, Integer.toString(DEFAULT_DEPTH_KEY));
		configMap.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, Integer.toString(DEFAULT_HEIGHT));
		return configMap;
	}
}
