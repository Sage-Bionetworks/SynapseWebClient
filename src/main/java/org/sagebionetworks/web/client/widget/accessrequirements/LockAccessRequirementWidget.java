package org.sagebionetworks.web.client.widget.accessrequirements;

import org.sagebionetworks.repo.model.LockAccessRequirement;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LockAccessRequirementWidget implements IsWidget {
	private LockAccessRequirementWidgetView view;
	LockAccessRequirement ar;
	DeleteAccessRequirementButton deleteAccessRequirementButton;
	SubjectsWidget subjectsWidget;

	@Inject
	public LockAccessRequirementWidget(LockAccessRequirementWidgetView view, SubjectsWidget subjectsWidget, DeleteAccessRequirementButton deleteAccessRequirementButton) {
		this.view = view;
		this.subjectsWidget = subjectsWidget;
		this.deleteAccessRequirementButton = deleteAccessRequirementButton;
		view.setDeleteAccessRequirementWidget(deleteAccessRequirementButton);
		view.setSubjectsWidget(subjectsWidget);
	}

	public void setRequirement(LockAccessRequirement ar, Callback refreshCallback) {
		this.ar = ar;
		deleteAccessRequirementButton.configure(ar, refreshCallback);
		subjectsWidget.configure(ar.getSubjectIds());
	}

	public void addStyleNames(String styleNames) {
		view.addStyleNames(styleNames);
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
