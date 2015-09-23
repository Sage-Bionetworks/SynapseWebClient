package org.sagebionetworks.web.client.widget.entity.tabs;

import org.gwtbootstrap3.client.ui.Button;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class AdminTabViewImpl implements AdminTabView {
	
	@UiField
	Button evaluationListContainer;
	
	public interface TabsViewImplUiBinder extends UiBinder<Widget, AdminTabViewImpl> {}
	
	Widget widget;
	public AdminTabViewImpl() {
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

