package org.sagebionetworks.web.client.widget.entity.renderer;


import org.gwtbootstrap3.client.ui.html.Span;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamMemberCountViewImpl implements TeamMemberCountView {

	public interface Binder extends UiBinder<Widget, TeamMemberCountViewImpl> {
	}

	@UiField
	Span countContainer;
	@UiField
	Span synAlertContainer;
	Widget widget;

	@Inject
	public TeamMemberCountViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setCount(String count) {
		countContainer.setText(count);
	}

	@Override
	public void setSynAlert(Widget widget) {
		synAlertContainer.clear();
		synAlertContainer.add(widget);
	}


}
