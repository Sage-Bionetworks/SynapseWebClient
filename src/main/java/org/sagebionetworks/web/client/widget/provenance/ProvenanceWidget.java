package org.sagebionetworks.web.client.widget.provenance;

import static org.sagebionetworks.web.shared.EntityBundleTransport.ENTITY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.Versionable;
import org.sagebionetworks.repo.model.provenance.Activity;
import org.sagebionetworks.repo.model.request.ReferenceList;
import org.sagebionetworks.repo.model.widget.ProvenanceWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.model.EntityBundle;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.LayoutServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.EntityBundleTransport;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceWidget implements ProvenanceWidgetView.Presenter, WidgetRendererPresenter {
	
	private ProvenanceWidgetView view;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;	
	private LayoutServiceAsync layoutService;
	private SynapseClientAsync synapseClient;
	private Map<String, ProvTreeNode> idToNode = new HashMap<String, ProvTreeNode>();
	private AdapterFactory adapterFactory;
	private SynapseJSNIUtils synapseJSNIUtils;
	private ProvenanceWidgetDescriptor descriptor;
	
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
	public void configure(String entityId, WidgetDescriptor widgetDescriptor) {
		if (!(widgetDescriptor instanceof ProvenanceWidgetDescriptor))
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_DESCRIPTOR_TYPE);
		//set up view based on descriptor parameters
		descriptor = (ProvenanceWidgetDescriptor)widgetDescriptor;
		int d = 1;
		if (descriptor.getDepth()!= null) {
			d = descriptor.getDepth().intValue();
		}
		final int depth = d;
		final Boolean showExpand = descriptor.getShowExpand();
		int mask = ENTITY;
		AsyncCallback<EntityBundleTransport> callback = new AsyncCallback<EntityBundleTransport>() {
			@Override
			public void onSuccess(EntityBundleTransport transport) {
				EntityBundle newBundle = null;
				try {
					newBundle = nodeModelCreator.createEntityBundle(transport);
				} catch (JSONObjectAdapterException e) {
					onFailure(e);
				}
				Entity newEntity = newBundle.getEntity();
				buildTree(newEntity, depth, showExpand);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				DisplayUtils.showErrorMessage(DisplayConstants.ERROR_UNABLE_TO_LOAD + caught.getMessage());
			}			
		};
		synapseClient.getEntityBundle(descriptor.getEntityId(), mask, callback);
	}
	
	public void setHeight(int height) {
		view.setHeight(height);
	}
		
	public void buildTree(final Entity entity, int depth, final boolean showExpand) {
		// get activity
		Long versionNumber = entity instanceof Versionable ? ((Versionable)entity).getVersionNumber() : null;		
		synapseClient.getActivityForEntityVersion(entity.getId(), versionNumber, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {										
					Activity activity = new Activity(adapterFactory.createNew(result));
					final List<Activity> activities = new ArrayList<Activity>();
					activities.add(activity);					
					lookupReferencesThenBuildTree(entity, showExpand, activities);					
				} catch (JSONObjectAdapterException e) {
					view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof NotFoundException) {
					// Display empty, fake provenance record
					Activity activity = new Activity();
					final List<Activity> activities = new ArrayList<Activity>();
					activities.add(activity);					
					lookupReferencesThenBuildTree(entity, showExpand, activities);					
				} else if(caught instanceof ForbiddenException) {
					view.showInfo("Provenance Error", "You do not have permission to view this Entity's Provenance record");
				} else {
					view.showErrorMessage(DisplayConstants.ERROR_PROVENANCE);
				}
			}
		});		
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
	

	
	/*
	 * Private Methods
	 */
	private void lookupReferencesThenBuildTree(final Entity entity,
			final boolean showExpand, final List<Activity> activities) {	

		// lookup all references in batch to get EntityHeaders
		List<Reference> allRefs = ProvUtils.extractReferences(activities);
		ReferenceList list = new ReferenceList();
		list.setReferences(allRefs);		
		try {
			synapseClient.getEntityHeaderBatch(list.writeToJSONObject(adapterFactory.createNew()).toJSONString(), new AsyncCallback<String>() {
				@Override
				public void onSuccess(String result) {					
					BatchResults<EntityHeader> headers;
					try {
						headers = nodeModelCreator.createBatchResults(result, EntityHeader.class);
						Map<Reference, EntityHeader> refToHeader = ProvUtils.mapReferencesToHeaders(headers);
						buildTreeThenLayout(entity, showExpand, activities, refToHeader);
					} catch (JSONObjectAdapterException e) {
						onFailure(new UnknownErrorException(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION));
					}
				}
				
				@Override
				public void onFailure(Throwable caught) {					
					buildTreeThenLayout(entity, showExpand, activities, new HashMap<Reference, EntityHeader>());
				}
			});
		} catch (JSONObjectAdapterException e) {
			view.showErrorMessage(DisplayConstants.ERROR_INCOMPATIBLE_CLIENT_VERSION);
		}
	}

	
	private void buildTreeThenLayout(final Entity entity,
			final boolean showExpand, List<Activity> activities, Map<Reference, EntityHeader> refToHeader) {
		// build the tree, layout and render
		idToNode = new HashMap<String, ProvTreeNode>();
		ProvTreeNode root = ProvUtils.buildProvTree(activities, entity, idToNode, refToHeader, showExpand, synapseJSNIUtils);					
		layoutTreeThenSendToView(root);
	}

	private void layoutTreeThenSendToView(ProvTreeNode root) {
		// layout tree
		layoutService.layoutProvTree(root, new AsyncCallback<ProvTreeNode>() {
			
			@Override
			public void onSuccess(ProvTreeNode rootLayedOut) {
				view.setTree(rootLayedOut);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(DisplayConstants.ERROR_LAYOUT);
			}
		});
	}

	

}
