package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ModifiedCreatedByWidgetViewImpl implements ModifiedCreatedByWidgetView {

	@UiField
	Span createdBadgePanel;
	@UiField
	Span createdOnText;
	@UiField
	Span modifiedBadgePanel;
	@UiField
	Span modifiedOnText;
	@UiField
	HTMLPanel container;
	
	public interface ModifiedCreatedByWidgetViewImplUiBinder extends UiBinder<Widget, ModifiedCreatedByWidgetViewImpl> {}

	private Widget widget;
	
	@Inject
	public ModifiedCreatedByWidgetViewImpl(ModifiedCreatedByWidgetViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setCreatedOnText(String text) {
		createdOnText.setText(text);
	}

	@Override
	public void setModifiedOnText(String text) {
		modifiedOnText.setText(text);
	}
	
	@Override
	public void setModifiedBadge(IsWidget modifiedBadge) {
		modifiedBadgePanel.clear();
		modifiedBadgePanel.add(modifiedBadge);
	}
	
	@Override
	public void setCreatedBadge(IsWidget createdBadge) {
		createdBadgePanel.clear();
		createdBadgePanel.add(createdBadge);
	}

	@Override
	public void clear() {
		container.setVisible(false);
		modifiedOnText.setText("");
		createdOnText.setText("");		
	}

	@Override
	public void setVisible(boolean isVisible) {
		container.setVisible(isVisible);
	}
}
