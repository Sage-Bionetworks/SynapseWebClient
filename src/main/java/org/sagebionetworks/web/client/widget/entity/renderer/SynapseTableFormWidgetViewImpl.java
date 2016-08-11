package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseTableFormWidgetViewImpl implements SynapseTableFormWidgetView {

	public interface Binder extends UiBinder<Widget, SynapseTableFormWidgetViewImpl> {}
	
	@UiField
	Div synAlertContainer;
	@UiField
	Div rowWidgetContainer;
	@UiField
	Button submitButton;
	@UiField
	Span successMessageText;
	@UiField
	Alert successMessageUI;
	@UiField
	Div formUI;
	@UiField
	Span userBadgeContainer;
	
	Widget w;
	Presenter presenter;
	@Inject
	public SynapseTableFormWidgetViewImpl(Binder binder) {
		w=binder.createAndBindUi(this);
		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSubmit();
			}
		});
	}
	
	@Override
	public void setPresenter(Presenter p) {
		presenter = p;
	}
	@Override
	public Widget asWidget() {
		return w;
	}
	
	@Override
	public void setRowFormWidget(Widget w) {
		rowWidgetContainer.clear();
		rowWidgetContainer.add(w);
	}
	
	@Override
	public void setSynAlertWidget(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}
	
	@Override
	public void setSubmitButtonLoading(boolean isLoading) {
		if (isLoading) {
			this.submitButton.state().loading();
		} else {
			this.submitButton.state().reset();
		}
	}
	@Override
	public void setFormUIVisible(boolean visible) {
		formUI.setVisible(visible);
	}
	@Override
	public void setSuccessMessage(String text) {
		successMessageText.setText(text);
	}
	@Override
	public void setSuccessMessageVisible(boolean visible) {
		successMessageUI.setVisible(visible);
	}
	
	@Override
	public void setUserBadge(Widget w) {
		userBadgeContainer.clear();
		userBadgeContainer.add(w);
	}
}
