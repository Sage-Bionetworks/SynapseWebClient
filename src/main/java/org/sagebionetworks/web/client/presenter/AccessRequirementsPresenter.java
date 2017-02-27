package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.repo.model.EntityBundle.ACCESS_REQUIREMENTS;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY_PATH;
import static org.sagebionetworks.repo.model.EntityBundle.UNMET_ACCESS_REQUIREMENTS;

import java.util.List;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class AccessRequirementsPresenter extends AbstractActivity implements Presenter<AccessRequirementsPlace> {
	private AccessRequirementsPlace place;
	private PlaceView view;
	private PortalGinInjector ginInjector;
	private SynapseAlert synAlert;
	private GlobalApplicationState globalAppState;
	private SynapseClientAsync synapseClient;
	Breadcrumb breadcrumbs;
	EntityBundle bundle;
	
	@Inject
	public AccessRequirementsPresenter(PlaceView view,
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			GlobalApplicationState globalAppState,
			Breadcrumb breadcrumbs) {
		this.view = view;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.globalAppState = globalAppState;
		this.synapseClient = synapseClient;
		view.addAboveBody(breadcrumbs.asWidget());
		view.addBelowBody(synAlert.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}
	
	@Override
	public void setPlace(AccessRequirementsPlace place) {
		this.place = place;
		view.initHeaderAndFooter();
		String entityId = place.getParam(AccessRequirementsPlace.ENTITY_ID_PARAM);
		synAlert.clear();
		if (entityId != null) {
			getEntityBundle(entityId);
		} else {
			synAlert.showError("Synapse id not found in parameters.");
		}
	}

	public void getEntityBundle(String entityId) {
		int partsMask = ENTITY | ENTITY_PATH | UNMET_ACCESS_REQUIREMENTS | ACCESS_REQUIREMENTS;
		synAlert.clear();
		view.clear();
		synapseClient.getEntityBundle(entityId, partsMask, new AsyncCallback<EntityBundle>() {
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			
			public void onSuccess(EntityBundle bundle) {
				AccessRequirementsPresenter.this.bundle = bundle;
				initBreadcrumbs(bundle.getPath());
				List<AccessRequirement> unmetArs = bundle.getUnmetAccessRequirements();
				List<AccessRequirement> accessRequirements = bundle.getAccessRequirements();
				for (AccessRequirement ar : accessRequirements) {
					boolean isUnmet = unmetArs.contains(ar);
					//TODO: create a new row for each access requirement
					if( ar instanceof ACTAccessRequirement) {
//						ACTAccessRequirementWidget w = ginInjector.getACTAccessRequirementWidget();
//						w.configure(ar, isUnmet);
//						view.add(w.asWidget());
					} else if (ar instanceof TermsOfUseAccessRequirement) {
//						TermsOfUseAccessRequirementWidget w = ginInjector.TermsOfUseAccessRequirementWidget();
//						w.configure(ar, isUnmet);
//						view.add(w.asWidget());						
					} else {
						synAlert.showError("unsupported access requirement type: " + ar.getClass().getName());
					}
				}
			};
		});
	}
	public void initBreadcrumbs(EntityPath entityPath) {
		List<EntityHeader> path = entityPath.getPath();
		EntityHeader lastHeader = new EntityHeader();
		lastHeader.setName("Conditions for use");
		path.add(lastHeader);
		breadcrumbs.configure(entityPath, null);
	}
	public AccessRequirementsPlace getPlace() {
		return place;
	}
	
	@Override
    public String mayStop() {
        return null;
    }
	
}
