package org.sagebionetworks.web.client.widget.provenance;

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
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.services.LayoutServiceAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.KeyValueDisplay;
import org.sagebionetworks.web.shared.exceptions.NotFoundException;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceWidget implements ProvenanceWidgetView.Presenter, SynapseWidgetPresenter {
	
	private ProvenanceWidgetView view;
	private NodeModelCreator nodeModelCreator;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;	
	private LayoutServiceAsync layoutService;
	private SynapseClientAsync synapseClient;
	private Map<String, ProvTreeNode> idToNode = new HashMap<String, ProvTreeNode>();
	private AdapterFactory adapterFactory;
	private SynapseJSNIUtils synapseJSNIUtils;
	
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
				}
				view.showErrorMessage(DisplayConstants.ERROR_PROVENANCE);
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
					
					BatchResults<EntityHeader> headers = nodeModelCreator.createBatchResults(result, EntityHeader.class);
					Map<Reference, EntityHeader> refToHeader = ProvUtils.mapReferencesToHeaders(headers);
					buildTreeThenLayout(entity, showExpand, activities, refToHeader);
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
