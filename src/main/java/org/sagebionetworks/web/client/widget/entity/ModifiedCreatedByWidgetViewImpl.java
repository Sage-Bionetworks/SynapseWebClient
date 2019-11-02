package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
	Div container;
	@UiField
	Span createdByUI;
	@UiField
	Span modifiedByUI;

	public interface ModifiedCreatedByWidgetViewImplUiBinder extends UiBinder<Widget, ModifiedCreatedByWidgetViewImpl> {
	}

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
	public void setVisible(boolean isVisible) {
		container.setVisible(isVisible);
	}

	@Override
	public void setCreatedByUIVisible(boolean visible) {
		createdByUI.setVisible(visible);
	}

	@Override
	public void setModifiedByUIVisible(boolean visible) {
		modifiedByUI.setVisible(visible);
	}
}
