package org.sagebionetworks.web.client.widget.sharing;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * Implementation with zero business logic.
 * 
 * @author John
 *
 */
public class AccessControlListModalWidgetViewImpl implements AccessControlListModalWidgetView {

	public interface Binder extends UiBinder<Modal, AccessControlListModalWidgetViewImpl> {
	}

	@UiField
	Modal uiModal;
	@UiField
	SimplePanel editorPanel;
	@UiField
	Button primaryButton;
	@UiField
	Button defaultButton;
	@UiField
	Span modalTitleContainer;
	Modal modal;

	@Inject
	public AccessControlListModalWidgetViewImpl(Binder binder) {
		modal = binder.createAndBindUi(this);
		modal.addAttachHandler(DisplayUtils.getHideModalOnDetachHandler());
		primaryButton.addDomHandler(DisplayUtils.getPreventTabHandler(primaryButton), KeyDownEvent.getType());
	}

	@Override
	public void showDialog() {
		modal.show();
	}

	@Override
	public void setDefaultButtonText(String text) {
		defaultButton.setText(text);
	}

	@Override
	public void setPrimaryButtonVisible(boolean visible) {
		primaryButton.setVisible(visible);
	}

	@Override
	public void setPrimaryButtonEnabled(boolean enabled) {
		primaryButton.setEnabled(enabled);
	}

	@Override
	public Widget asWidget() {
		return modal;
	}

	@Override
	public void addEditor(IsWidget editor) {
		this.editorPanel.setWidget(editor);
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		primaryButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onPrimary();
			}
		});
	}

	@Override
	public void hideDialog() {
		modal.hide();
	}

	@Override
	public void setLoading(boolean loading) {
		if (loading) {
			this.primaryButton.state().loading();
		} else {
			this.primaryButton.state().reset();
		}
	}

	@Override
	public void setTitle(String title) {
		modalTitleContainer.clear();
		Heading h = new Heading(HeadingSize.H4);
		h.addStyleName("displayInline");
		h.setText(title);
		modalTitleContainer.add(h);
	}
}
