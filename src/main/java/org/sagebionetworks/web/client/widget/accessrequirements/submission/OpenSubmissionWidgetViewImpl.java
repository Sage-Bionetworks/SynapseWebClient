package org.sagebionetworks.web.client.widget.accessrequirements.submission;

import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OpenSubmissionWidgetViewImpl implements OpenSubmissionWidgetView {

	@UiField
	Div synAlertContainer;
	@UiField
	Div accessRequirementContainer;
	@UiField
	Span numberOfSubmissions;

	Callback onAttachCallback;

	public interface Binder extends UiBinder<Widget, OpenSubmissionWidgetViewImpl> {
	}

	Widget w;
	Presenter presenter;

	@Inject
	public OpenSubmissionWidgetViewImpl(Binder binder) {
		this.w = binder.createAndBindUi(this);
		w.addAttachHandler(new AttachEvent.Handler() {

			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					onAttachCallback.invoke();
				}
			}
		});
	}

	@Override
	public void setNumberOfSubmissions(long number) {
		numberOfSubmissions.setText(number + " ");
	}

	@Override
	public void setACTAccessRequirementWidget(IsWidget w) {
		accessRequirementContainer.clear();
		accessRequirementContainer.add(w);
	}

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return w;
	}

	@Override
	public boolean isInViewport() {
		return DisplayUtils.isInViewport(w);
	}

	@Override
	public boolean isAttached() {
		return w.isAttached();
	}

	@Override
	public void setOnAttachCallback(Callback onAttachCallback) {
		this.onAttachCallback = onAttachCallback;
	}

}
