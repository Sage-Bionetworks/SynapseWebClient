package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.AccessApprovalInfo;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.BatchAccessApprovalInfoRequest;
import org.sagebionetworks.repo.model.BatchAccessApprovalInfoResponse;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.AccessRequirementsPlace;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.view.PlaceView;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.CreateAccessRequirementButton;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
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
	private AuthenticationController authController;
	public static Long LIMIT = 30L;
	Long currentOffset;
	RestrictableObjectDescriptor subject;
	EntityIdCellRenderer entityIdRenderer;
	TeamBadge teamBadge;
	List<AccessRequirement> allArs;
	CreateAccessRequirementButton createAccessRequirementButton;
	DivView noResultsDiv;
	DivView metAccessRequirementsDiv;
	DivView unmetAccessRequirementsDiv;
	Callback refreshCallback;

	@Inject
	public AccessRequirementsPresenter(PlaceView view, DataAccessClientAsync dataAccessClient, SynapseAlert synAlert, PortalGinInjector ginInjector, EntityIdCellRenderer entityIdRenderer, TeamBadge teamBadge, CreateAccessRequirementButton createAccessRequirementButton, DivView noResultsDiv, DivView unmetAccessRequirementsDiv, DivView metAccessRequirementsDiv, AuthenticationController authController) {
		this.view = view;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.dataAccessClient = dataAccessClient;
		fixServiceEntryPoint(dataAccessClient);
		this.entityIdRenderer = entityIdRenderer;
		this.teamBadge = teamBadge;
		this.createAccessRequirementButton = createAccessRequirementButton;
		this.noResultsDiv = noResultsDiv;
		this.metAccessRequirementsDiv = metAccessRequirementsDiv;
		this.unmetAccessRequirementsDiv = unmetAccessRequirementsDiv;
		this.authController = authController;
		view.addAboveBody(synAlert);
		view.addAboveBody(createAccessRequirementButton);
		unmetAccessRequirementsDiv.addStyleName("markdown");
		metAccessRequirementsDiv.addStyleName("markdown min-height-400");
		view.add(unmetAccessRequirementsDiv.asWidget());
		view.add(metAccessRequirementsDiv.asWidget());
		view.addTitle("All conditions for ");
		view.addTitle(entityIdRenderer.asWidget());
		view.addTitle(teamBadge.asWidget());
		noResultsDiv.setText("No access requirements found");
		noResultsDiv.addStyleName("min-height-400");
		noResultsDiv.setVisible(false);
		view.add(noResultsDiv.asWidget());
		refreshCallback = new Callback() {
			@Override
			public void invoke() {
				loadData();
			}
		};

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
		String id = place.getParam(AccessRequirementsPlace.ID_PARAM);
		String typeString = place.getParam(AccessRequirementsPlace.TYPE_PARAM);
		RestrictableObjectType type = RestrictableObjectType.valueOf(typeString.toUpperCase());
		synAlert.clear();
		subject = new RestrictableObjectDescriptor();
		subject.setType(type);
		subject.setId(id);
		if (RestrictableObjectType.ENTITY.equals(type)) {
			teamBadge.setVisible(false);
			entityIdRenderer.setVisible(true);
			entityIdRenderer.setValue(id);
		} else {
			teamBadge.setVisible(true);
			entityIdRenderer.setVisible(false);
			teamBadge.configure(id);
		}
		loadData();
	}

	public void loadData() {
		createAccessRequirementButton.configure(subject, refreshCallback);
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
		w.configure(ar, subject, refreshCallback);
		return w;
	}

	public void getStatusForEachAccessRequirement() {
		if (!authController.isLoggedIn()) {
			for (AccessRequirement ar : allArs) {
				unmetAccessRequirementsDiv.add(getAccessRequirementWidget(ar));
			}
			return;
		}

		List<String> arIds = new ArrayList<String>();
		for (AccessRequirement accessRequirement : allArs) {
			arIds.add(Long.toString(accessRequirement.getId()));
		}
		BatchAccessApprovalInfoRequest request = new BatchAccessApprovalInfoRequest();
		request.setAccessRequirementIds(arIds);
		request.setUserId(authController.getCurrentUserPrincipalId());

		dataAccessClient.getAccessRequirementStatus(request, new AsyncCallback<BatchAccessApprovalInfoResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}

			@Override
			public void onSuccess(BatchAccessApprovalInfoResponse response) {
				List<AccessApprovalInfo> results = response.getResults();
				for (int i = 0; i < results.size(); i++) {
					AccessRequirement ar = allArs.get(i);
					if (results.get(i).getHasAccessApproval()) {
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
