package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.team.TeamBadge;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubjectWidget implements SubjectWidgetView.Presenter, IsWidget {

	SubjectWidgetView view;
	PortalGinInjector ginInjector;
	RestrictableObjectDescriptor rod;
	CallbackP<SubjectWidget> deletedCallback;

	@Inject
	public SubjectWidget(SubjectWidgetView view, PortalGinInjector ginInjector) {
		this.view = view;
		this.ginInjector = ginInjector;
		view.setPresenter(this);
	}

	public void configure(RestrictableObjectDescriptor rod, CallbackP<SubjectWidget> deletedCallback) {
		this.rod = rod;
		this.deletedCallback = deletedCallback;
		if (rod.getType().equals(RestrictableObjectType.ENTITY)) {
			EntityIdCellRenderer entityRenderer = (EntityIdCellRenderer) ginInjector.createEntityIdCellRenderer();
			entityRenderer.setValue(rod.getId(), false);
			view.setSubjectRendererWidget(entityRenderer);
		} else if (rod.getType().equals(RestrictableObjectType.TEAM)) {
			TeamBadge teamBadge = ginInjector.getTeamBadgeWidget();
			teamBadge.configure(rod.getId());
			teamBadge.addStyleName("margin-right-5");
			view.setSubjectRendererWidget(teamBadge.asWidget());
		}
		view.setDeleteVisible(deletedCallback != null);
	}

	@Override
	public void onDelete() {
		deletedCallback.invoke(this);
	}

	public RestrictableObjectDescriptor getRestrictableObjectDescriptor() {
		return rod;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
