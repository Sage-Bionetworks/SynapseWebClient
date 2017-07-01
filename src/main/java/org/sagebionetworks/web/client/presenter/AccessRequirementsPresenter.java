package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

public class AccessRequirementsPresenter extends AbstractActivity implements Presenter<AccessRequirementsPlace> {
	private AccessRequirementsPlace place;
	private PlaceView view;
	private PortalGinInjector ginInjector;
	private SynapseAlert synAlert;
	private DataAccessClientAsync dataAccessClient;
	public static Long LIMIT = 30L;
	Long currentOffset;
	RestrictableObjectDescriptor subject;
	EntityIdCellRendererImpl entityIdRenderer; 
	TeamBadge teamBadge;
	List<AccessRequirement> allArs;
	CreateAccessRequirementButton createAccessRequirementButton;
	DivView noResultsDiv;
	DivView metAccessRequirementsDiv;
	DivView unmetAccessRequirementsDiv;
	
	@Inject
	public AccessRequirementsPresenter(PlaceView view,
			DataAccessClientAsync dataAccessClient,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			EntityIdCellRendererImpl entityIdRenderer, 
			TeamBadge teamBadge,
			CreateAccessRequirementButton createAccessRequirementButton,
			DivView noResultsDiv,
			DivView unmetAccessRequirementsDiv,
			DivView metAccessRequirementsDiv
			) {
		this.view = view;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.dataAccessClient = dataAccessClient;
		this.entityIdRenderer = entityIdRenderer;
		this.teamBadge = teamBadge;
		this.createAccessRequirementButton = createAccessRequirementButton;
		this.noResultsDiv = noResultsDiv;
		this.metAccessRequirementsDiv = metAccessRequirementsDiv;
		this.unmetAccessRequirementsDiv = unmetAccessRequirementsDiv;
		view.addAboveBody(synAlert);
		view.addAboveBody(createAccessRequirementButton);
		unmetAccessRequirementsDiv.addStyleName("min-height-200");
		metAccessRequirementsDiv.addStyleName("min-height-200");
		view.add(unmetAccessRequirementsDiv.asWidget());
		view.add(metAccessRequirementsDiv.asWidget());
		view.addTitle("All conditions for ");
		view.addTitle(entityIdRenderer.asWidget());
		view.addTitle(teamBadge.asWidget());
		noResultsDiv.setText("No access requirements found");
		noResultsDiv.addStyleName("min-height-400");
		noResultsDiv.setVisible(false);
		view.add(noResultsDiv.asWidget());
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
		createAccessRequirementButton.configure(subject);
		metAccessRequirementsDiv.clear();
		unmetAccessRequirementsDiv.clear();
		currentOffset = 0L;
		allArs = new ArrayList<AccessRequirement>();
		loadMore();
	}

	public void loadMore() {
		synAlert.clear();
		// TODO: call should also return the user state (approved, pending, ...) for each access requirement
		dataAccessClient.getAccessRequirements(subject, LIMIT, currentOffset, new AsyncCallback<List<AccessRequirement>>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			
			public void onSuccess(List<AccessRequirement> accessRequirements) {
				noResultsDiv.setVisible(currentOffset == 0 && accessRequirements.isEmpty());
				currentOffset += LIMIT;
				boolean isNewAr = false;
				for (AccessRequirement ar : accessRequirements) {
					if (!allArs.contains(ar)) {
						isNewAr = true;
						allArs.add(ar);
					}
				}
				if (isNewAr) {
					loadMore();
				} else {
					getStatusForEachAccessRequirement();
				}
			};
		});
	}

	public IsWidget getAccessRequirementWidget(AccessRequirement ar) {
		AccessRequirementWidget w = ginInjector.getAccessRequirementWidget();
		w.configure(ar);
		return w;
	}

	public void getStatusForEachAccessRequirement() {
		List<String> arIds = new ArrayList<String>();
		for (AccessRequirement accessRequirement : allArs) {
			arIds.add(Long.toString(accessRequirement.getId()));
		}
		dataAccessClient.getAccessRequirementStatus(arIds, new AsyncCallback<List<Boolean>>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
			@Override
			public void onSuccess(List<Boolean> statuses) {
				for (int i = 0; i < statuses.size(); i++) {
					AccessRequirement ar = allArs.get(i);
					if (statuses.get(i)) {
						metAccessRequirementsDiv.add(getAccessRequirementWidget(ar));	
					} else {
						unmetAccessRequirementsDiv.add(getAccessRequirementWidget(ar));	
					}
				}
			}
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
