package org.sagebionetworks.web.client.widget.accessrequirements;

import org.gwtbootstrap3.client.ui.html.Div;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LockAccessRequirementWidgetViewImpl implements LockAccessRequirementWidgetView {
	@UiField
	Div deleteAccessRequirementContainer;
	@UiField
	Div subjectsWidgetContainer;

	public interface Binder extends UiBinder<Widget, LockAccessRequirementWidgetViewImpl> {
	}

	Widget w;

	@Inject
	public LockAccessRequirementWidgetViewImpl(Binder binder) {
		this.w = binder.createAndBindUi(this);
	}

	@Override
	public void addStyleNames(String styleNames) {
		w.addStyleName(styleNames);
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public void setDeleteAccessRequirementWidget(IsWidget w) {
		deleteAccessRequirementContainer.clear();
		deleteAccessRequirementContainer.add(w);
	}

	@Override
	public void setSubjectsWidget(IsWidget w) {
		subjectsWidgetContainer.clear();
		subjectsWidgetContainer.add(w);
	}
}
