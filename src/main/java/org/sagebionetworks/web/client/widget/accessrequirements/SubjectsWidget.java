package org.sagebionetworks.web.client.widget.accessrequirements;

import java.util.List;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubjectsWidget implements IsWidget {
	DivView view;
	PortalGinInjector ginInjector;
	IsACTMemberAsyncHandler isACTMemberAsyncHandler;
	CallbackP<SubjectWidget> subjectWidgetDeletedCallback;

	@Inject
	public SubjectsWidget(DivView view, PortalGinInjector ginInjector, IsACTMemberAsyncHandler isACTMemberAsyncHandler) {
		this.view = view;
		this.ginInjector = ginInjector;
		this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
		view.addStyleName("margin-bottom-5");
		view.setVisible(false);
	}

	@Override
	public Widget asWidget() {
		return this.view.asWidget();
	}

	public void configure(final List<RestrictableObjectDescriptor> subjects) {
		isACTMemberAsyncHandler.isACTActionAvailable(new CallbackP<Boolean>() {
			@Override
			public void invoke(Boolean isACT) {
				view.setVisible(isACT);
				if (isACT) {
					configureAfterACTCheck(subjects);
				}
			}
		});
	}

	private void configureAfterACTCheck(List<RestrictableObjectDescriptor> subjects) {
		view.clear();
		for (RestrictableObjectDescriptor rod : subjects) {
			SubjectWidget subjectWidget = ginInjector.getSubjectWidget();
			subjectWidget.configure(rod, subjectWidgetDeletedCallback);
			view.add(subjectWidget);
		}
	}

	public void setDeleteCallback(final CallbackP<RestrictableObjectDescriptor> subjectDeletedCallback) {
		subjectWidgetDeletedCallback = new CallbackP<SubjectWidget>() {
			@Override
			public void invoke(SubjectWidget subjectWidget) {
				view.remove(subjectWidget);
				subjectDeletedCallback.invoke(subjectWidget.getRestrictableObjectDescriptor());
			}
		};
	}
}
