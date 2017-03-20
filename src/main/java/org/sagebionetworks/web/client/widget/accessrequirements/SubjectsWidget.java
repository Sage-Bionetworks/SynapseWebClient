package org.sagebionetworks.web.client.widget.accessrequirements;

import java.util.List;

import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubjectsWidget implements IsWidget {
	
	DivView view;
	PortalGinInjector ginInjector;
	
	@Inject
	public SubjectsWidget(DivView view, 
			PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	public void configure(List<RestrictableObjectDescriptor> subjects) {
		view.clear();
		for (RestrictableObjectDescriptor rod : subjects) {
			if (rod.getType().equals(RestrictableObjectType.ENTITY)) {
				EntityIdCellRenderer entityRenderer = ginInjector.createEntityIdCellRenderer();
				entityRenderer.setValue(rod.getId());
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
