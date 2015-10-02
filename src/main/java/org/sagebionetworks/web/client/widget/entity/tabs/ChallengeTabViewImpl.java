package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ChallengeTabViewImpl implements ChallengeTabView {
	
	@UiField
	SimplePanel evaluationListContainer;
	
	public interface TabsViewImplUiBinder extends UiBinder<Widget, ChallengeTabViewImpl> {}
	
	Widget widget;
	public ChallengeTabViewImpl() {
		//empty constructor, you can include this widget in the ui xml
		TabsViewImplUiBinder binder = GWT.create(TabsViewImplUiBinder.class);
		widget = binder.createAndBindUi(this);
	}
	
	
	@Override
	public void setEvaluationList(Widget w) {
		evaluationListContainer.add(w);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
}

