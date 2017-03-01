package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.ACTAccessRequirement;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.ACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.TermsOfUseAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
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
	LoadMoreWidgetContainer loadMoreContainer;
	public static Long LIMIT = 30L;
	Long currentOffset;
	RestrictableObjectDescriptor subject;
	EntityIdCellRendererImpl entityIdRenderer; 
	TeamBadge teamBadge;
	List<AccessRequirement> allArs;
	
	@Inject
	public AccessRequirementsPresenter(PlaceView view,
			SynapseClientAsync synapseClient,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			GlobalApplicationState globalAppState,
			LoadMoreWidgetContainer loadMoreContainer, 
			EntityIdCellRendererImpl entityIdRenderer, 
			TeamBadge teamBadge) {
		this.view = view;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.globalAppState = globalAppState;
		this.synapseClient = synapseClient;
		this.loadMoreContainer = loadMoreContainer;
		this.entityIdRenderer = entityIdRenderer;
		this.teamBadge = teamBadge;
		view.add(loadMoreContainer.asWidget());
		view.addBelowBody(synAlert.asWidget());
		view.addTitle(entityIdRenderer.asWidget());
		view.addTitle(teamBadge.asWidget());
//		view.addTitle(": Conditions for use");
		loadMoreContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
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
		String teamId = place.getParam(AccessRequirementsPlace.TEAM_ID_PARAM);
		synAlert.clear();
		subject = new RestrictableObjectDescriptor();
		if (entityId != null) {
			teamBadge.setVisible(false);
			entityIdRenderer.setVisible(true);
			subject.setId(entityId);
			subject.setType(RestrictableObjectType.ENTITY);
			entityIdRenderer.setValue(entityId);
			loadData();
		} else if (teamId != null) {
			teamBadge.setVisible(true);
			entityIdRenderer.setVisible(false);
			subject.setId(teamId);
			subject.setType(RestrictableObjectType.TEAM);
			teamBadge.configure(teamId);
			loadData();
		} else {
			synAlert.showError("Synapse id not found in parameters.");
		}
	}
	
	public void loadData() {
		loadMoreContainer.clear();
		currentOffset = 0L;
		allArs = new ArrayList<AccessRequirement>();
		loadMore();
	}

	public void loadMore() {
		synAlert.clear();
		synapseClient.getAccessRequirements(subject, LIMIT, currentOffset, new AsyncCallback<List<AccessRequirement>>() {
			
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
				loadMoreContainer.setIsMore(false);
			}
			
			public void onSuccess(List<AccessRequirement> accessRequirements) {
				currentOffset += LIMIT;
				boolean isNewAr = false;
				for (AccessRequirement ar : accessRequirements) {
					if (!allArs.contains(ar)) {
						isNewAr = true;
						allArs.add(ar);
						// create a new row for each access requirement.
						// need state of approval/submission.
						if( ar instanceof ACTAccessRequirement) {
							ACTAccessRequirementWidget w = ginInjector.getACTAccessRequirementWidget();
							w.configure((ACTAccessRequirement)ar); 
							loadMoreContainer.add(w.asWidget());
						} else if (ar instanceof TermsOfUseAccessRequirement) {
							TermsOfUseAccessRequirementWidget w = ginInjector.getTermsOfUseAccessRequirementWidget();
							w.configure((TermsOfUseAccessRequirement)ar);
							loadMoreContainer.add(w.asWidget());						
						} else {
							synAlert.showError("unsupported access requirement type: " + ar.getClass().getName());
						}
					}
				}
				loadMoreContainer.setIsMore(isNewAr);
			};
		});
	}
	
	public AccessRequirementsPlace getPlace() {
		return place;
	}
	
	@Override
    public String mayStop() {
        return null;
    }
	
}
