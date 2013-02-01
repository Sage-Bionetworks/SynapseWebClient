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
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.provenance.UsedEntity;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.LayoutServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvGraph;
import org.sagebionetworks.web.shared.provenance.ProvGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceWidget implements ProvenanceWidgetView.Presenter, WidgetRendererPresenter {
	
	private static final String FAKE_ID_PREFIX = "fakeId";
	private ProvenanceWidgetView view;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;	
	private LayoutServiceAsync layoutService;
	private SynapseClientAsync synapseClient;
	private Map<String, ProvGraphNode> idToNode = new HashMap<String, ProvGraphNode>();
	private AdapterFactory adapterFactory;
	private SynapseJSNIUtils synapseJSNIUtils;
	private Map<String, String> descriptor;
	

	private interface ProcessCallback {
		public void onComplete();
	}
	Map<Reference,String> generatedByActivityId = new HashMap<Reference, String>();
	Map<String,Activity> processedActivities = new HashMap<String, Activity>();
	Set<Reference> references = new HashSet<Reference>();
	Set<Activity> toProcess = new HashSet<Activity>();
	Map<Reference, EntityHeader> refToHeader = new HashMap<Reference, EntityHeader>();
	boolean showExpand;
	int maxDepth = 1; 

	
	@Inject
	public ProvenanceWidget(ProvenanceWidgetView view, SynapseClientAsync synapseClient,
			NodeModelCreator nodeModelCreator,
			AuthenticationController authenticationController, 
			LayoutServiceAsync layoutService, 
			AdapterFactory adapterFactory,
			SynapseJSNIUtils synapseJSNIUtils) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.nodeModelCreator = nodeModelCreator;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.layoutService = layoutService;
		this.adapterFactory = adapterFactory;
		this.synapseJSNIUtils = synapseJSNIUtils;
		view.setPresenter(this);
	}	
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;		
		if(descriptor.containsKey(WidgetConstants.PROV_WIDGET_ENTITY_ID_KEY)) entityId = descriptor.get(WidgetConstants.PROV_WIDGET_ENTITY_ID_KEY);
		final Long versionNumber = descriptor.containsKey(WidgetConstants.PROV_WIDGET_ENTITY_VERSION_NUMBER_KEY) ? Long.parseLong(descriptor.get(WidgetConstants.PROV_WIDGET_ENTITY_VERSION_NUMBER_KEY)) : null;
		maxDepth = descriptor.containsKey(WidgetConstants.PROV_WIDGET_DEPTH_KEY) ? Integer.parseInt(descriptor.get(WidgetConstants.PROV_WIDGET_DEPTH_KEY)) : 1;
		showExpand = descriptor.get(WidgetConstants.PROV_WIDGET_EXPAND_KEY) != null ? Boolean.parseBoolean(descriptor.get(WidgetConstants.PROV_WIDGET_EXPAND_KEY)) : false;
		Reference startRef = new Reference();
		startRef.setTargetId(entityId);
		startRef.setTargetVersionNumber(versionNumber);		

		// call processUsed with depth 0 to start graph creation
		processUsed(startRef, 0, new ProcessCallback() {			
			@Override
			public void onComplete() {
				// TODO : 
				lookupReferencesThenBuildGraph();
				// layout graph
				// send to view				
			}
		});

	}
	
	public void setHeight(int height) {
		view.setHeight(height);
	}
			
	@Override
	public void getInfo(String nodeId, final AsyncCallback<KeyValueDisplay<String>> callback) {
		ProvUtils.getInfo(nodeId, synapseClient, nodeModelCreator, idToNode, callback);
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
		
	}

	
	/*
	 * Private Methods
	 */
	private void processUsed(final Reference ref, final int depth, final ProcessCallback processCallback) {
		// add entry to referenceToHeader		
		references.add(ref);
		
		// check to see if we are all done 
		if(depth >= maxDepth && toProcess.size() == 0) {
			processCallback.onComplete();
		}
		
		// lookup generatedBy activity for ref
		synapseClient.getActivityForEntityVersion(ref.getTargetId(), ref.getTargetVersionNumber(), new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {										
					Activity activity = new Activity(adapterFactory.createNew(result));
					nextSteps(activity);
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);				
					onFailure(e);
					return;
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				// Display empty, fake provenance record
				Activity activity = new Activity();
				activity.setId(FAKE_ID_PREFIX + synapseJSNIUtils.randomNextInt());
				if(caught instanceof NotFoundException) {
					activity.setName(DisplayConstants.ACTIVITY + " " + DisplayConstants.NOT_FOUND);
				} else if(caught instanceof ForbiddenException) {
					activity.setName(DisplayConstants.ACTIVITY + " " + DisplayConstants.UNAUTHORIZED);
				} else {
					activity.setName(DisplayConstants.ERROR_PROVENANCE_RELOAD);
				}
				nextSteps(activity);
			}
			
			private void nextSteps(Activity activity) {
				// add entry to generatedByActivityId
				generatedByActivityId.put(ref, activity.getId());

				// add activity to toProcess and process it as next level in the depth of graph
				toProcess.add(activity);
				processActivity(activity, depth+1, processCallback);				
			}			
		});
		
	}
	
	private void processActivity(final Activity activity, final int depth, final ProcessCallback processCallback) {
		// check if activity has already been processed
		if(processedActivities.containsKey(activity.getId())) {
			if(toProcess.contains(activity)) toProcess.remove(activity);
			return;
		}
		
		// add activity to processedActivites
		processedActivities.put(activity.getId(), activity);
		
		// if this activity errored then no need to go any further with it
		if(activity.getId().startsWith(FAKE_ID_PREFIX)) return;

		// Lookup entities generated by this activity
		synapseClient.getEntitiesGeneratedBy(activity.getId(), Integer.MAX_VALUE, 0, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					PaginatedResults<Reference> generated = nodeModelCreator.createPaginatedResults(result, Reference.class);
					// add references to generatedByActivityId & references
					if(generated != null && generated.getResults() != null) {
						for(Reference ref : generated.getResults()) {
							generatedByActivityId.put(ref, activity.getId());
							references.add(ref);
						}
					} 
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}				
				nextSteps();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				nextSteps();
			}

			private void nextSteps() {
				// remove activity from toProcess set
				toProcess.remove(activity);
				
				// call processUsed for each Reference in the used list
				if(activity.getUsed() != null) {
					Set<UsedEntity> set = activity.getUsed();
					Iterator<UsedEntity> itr = set.iterator();
					while(itr.hasNext()) {
						UsedEntity ue = itr.next();
						if(ue.getReference() != null) {
							processUsed(ue.getReference(), depth, processCallback);
						}
					}
				}
			}
		});
	}
		
	private void lookupReferencesThenBuildGraph() {			
		ReferenceList list = new ReferenceList();
		list.setReferences(new ArrayList<Reference>(references));		
		try {
			synapseClient.getEntityHeaderBatch(list.writeToJSONObject(adapterFactory.createNew()).toJSONString(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {					
					BatchResults<EntityHeader> headers;
					try {
						headers = nodeModelCreator.createBatchResults(result, EntityHeader.class);
						refToHeader = ProvUtils.mapReferencesToHeaders(headers);
						buildGraphThenLayout();
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {					
					buildGraphThenLayout();
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
		}
	}

	
	private void buildGraphThenLayout() {
		// build the tree, layout and render
		idToNode = new HashMap<String, ProvGraphNode>();

		ProvGraph graph = ProvUtils.buildProvGraph(generatedByActivityId, processedActivities, idToNode, refToHeader, showExpand, synapseJSNIUtils);					
		layoutGraphThenSendToView(graph);
	}

	private void layoutGraphThenSendToView(ProvGraph graph) {
		// layout tree
		layoutService.dagLayout(graph, new AsyncCallback<ProvGraph>() {
			
			@Override
			public void onSuccess(ProvGraph graphLayedOut) {
				view.setGraph(graphLayedOut);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_LAYOUT);
			}
		});
	}

}
