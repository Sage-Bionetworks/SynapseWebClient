package org.sagebionetworks.web.client.widget.refresh;

import org.gwtbootstrap3.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RefreshAlertViewImpl implements RefreshAlertView {

	@UiField
	Button refreshButton;

	public interface Binder extends UiBinder<Widget, RefreshAlertViewImpl> {
	}

	Widget w;
	Presenter presenter;

	@Inject
	public RefreshAlertViewImpl(Binder binder) {
		this.w = binder.createAndBindUi(this);
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onRefresh();
			}
		});
		w.addAttachHandler(new AttachEvent.Handler() {
			@Override
			public void onAttachOrDetach(AttachEvent event) {
				if (event.isAttached()) {
					presenter.onAttach();
				}
			}
		});
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
	public void setVisible(boolean visible) {
		w.setVisible(visible);
	}

	@Override
	public boolean isAttached() {
		return w.isAttached();
	}
}
