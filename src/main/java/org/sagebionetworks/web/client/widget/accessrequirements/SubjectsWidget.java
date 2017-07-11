package org.sagebionetworks.web.client.widget.accessrequirements;

import java.util.List;

import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRendererImpl;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubjectsWidget implements IsWidget {
	
	DivView view;
	PortalGinInjector ginInjector;
	IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	
	@Inject
	public SubjectsWidget(DivView view, 
			PortalGinInjector ginInjector,
			IsACTMemberAsyncHandler isACTMemberAsyncHandler) {
		this.view = view;
		this.ginInjector = ginInjector;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		view.setVisible(false);
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	public void configure(final List<RestrictableObjectDescriptor> subjects, final boolean hideIfLoadError) {
		isACTMemberAsyncHandler.isACTActionAvailable(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isACT) {
				view.setVisible(isACT);
				if (isACT) {
					configureAfterACTCheck(subjects, hideIfLoadError);
				}
			}
		});
	}
	
	private void configureAfterACTCheck(List<RestrictableObjectDescriptor> subjects, boolean hideIfLoadError) {
		view.clear();
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
	
}
