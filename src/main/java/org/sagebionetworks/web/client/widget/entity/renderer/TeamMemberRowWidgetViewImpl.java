package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.html.Span;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamMemberRowWidgetViewImpl implements TeamMemberRowWidgetView {
	@UiField
	Span userBadgeContainer;
	@UiField
	Span institutionSpan;
	@UiField
	Span emailSpan;

	private Widget widget;

	public interface Binder extends UiBinder<Widget, TeamMemberRowWidgetViewImpl> {
	}

	@Inject
	public TeamMemberRowWidgetViewImpl(Binder binder) {
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setEmail(String email) {
		emailSpan.setText(email);
	}

	@Override
	public void setInstitution(String institution) {
		institutionSpan.setText(institution);
	}

	@Override
	public void setUserBadge(IsWidget w) {
		userBadgeContainer.clear();
		userBadgeContainer.add(w);
	}

}
