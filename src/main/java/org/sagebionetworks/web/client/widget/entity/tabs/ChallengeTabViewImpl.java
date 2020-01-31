package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.html.Span;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ChallengeTabViewImpl implements ChallengeTabView {

	@UiField
	SimplePanel evaluationListContainer;
	@UiField
	SimplePanel challengeWidgetContainer;
	@UiField
	Span actionMenuContainer;

	public interface TabsViewImplUiBinder extends UiBinder<Widget, ChallengeTabViewImpl> {
	}

	Widget widget;

	public ChallengeTabViewImpl() {
		// empty constructor, you can include this widget in the ui xml
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
	}

	@Override
	public void setEvaluationList(Widget w) {
		evaluationListContainer.setWidget(w);
	}

	@Override
	public void setChallengeWidget(Widget w) {
		challengeWidgetContainer.setWidget(w);
	}
	
	@Override
	public void setActionMenu(IsWidget w) {
		actionMenuContainer.clear();
		actionMenuContainer.add(w);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
}

