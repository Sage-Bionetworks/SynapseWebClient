package org.sagebionetworks.web.client.widget.accessrequirements;

import java.util.List;

import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptorResponse;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubjectsWidget implements IsWidget {
	
	DivView view;
	PortalGinInjector ginInjector;
	IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	DataAccessClientAsync dataAccessClient;
	
	@Inject
	public SubjectsWidget(DivView view, 
			PortalGinInjector ginInjector,
			IsACTMemberAsyncHandler isACTMemberAsyncHandler,
			DataAccessClientAsync dataAccessClient) {
		this.view = view;
		this.ginInjector = ginInjector;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		this.dataAccessClient = dataAccessClient;
		view.setVisible(false);
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	public void configure(final String accessRequirementId, final boolean hideIfLoadError) {
		isACTMemberAsyncHandler.isACTActionAvailable(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isACT) {
				view.setVisible(isACT);
				if (isACT) {
					clear();
					configureAfterACTCheck(accessRequirementId, hideIfLoadError, null);
				}
			}
		});
	}
	
	private void configureAfterACTCheck(final String accessRequirementId, final boolean hideIfLoadError, String nextPageToken) {
		//get the associated subjects
		dataAccessClient.getSubjects(accessRequirementId, nextPageToken, new AsyncCallback<RestrictableObjectDescriptorResponse>() {
			@Override
			public void onSuccess(RestrictableObjectDescriptorResponse response) {
				addSubjects(response.getSubjects(), hideIfLoadError);
				if (response.getNextPageToken() != null) {
					configureAfterACTCheck(accessRequirementId, hideIfLoadError, response.getNextPageToken());
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				SynapseAlert synAlert = ginInjector.getSynapseAlertWidget();
				synAlert.handleException(caught);
				view.add(synAlert);
			}
		});
	}
	
	public void addSubjects(List<RestrictableObjectDescriptor> subjects, boolean hideIfLoadError) {
		for (RestrictableObjectDescriptor rod : subjects) {
			if (rod.getType().equals(RestrictableObjectType.ENTITY)) {
				EntityIdCellRendererImpl entityRenderer = (EntityIdCellRendererImpl)ginInjector.createEntityIdCellRenderer();
				entityRenderer.setValue(rod.getId(), hideIfLoadError);
				view.add(entityRenderer);
			} else if (rod.getType().equals(RestrictableObjectType.TEAM)) {
				TeamBadge teamBadge = ginInjector.getTeamBadgeWidget();
				teamBadge.configure(rod.getId());
				teamBadge.addStyleName("margin-right-5");
				view.add(teamBadge.asWidget());
			}
		}
	}
	
	public void clear() {
		view.clear();
	}
}
