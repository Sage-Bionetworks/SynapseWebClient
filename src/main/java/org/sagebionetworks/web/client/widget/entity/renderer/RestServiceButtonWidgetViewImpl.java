package org.sagebionetworks.web.client.widget.entity.renderer;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RestServiceButtonWidgetViewImpl implements RestServiceButtonWidgetView {

	public interface Binder extends	UiBinder<Widget, RestServiceButtonWidgetViewImpl> {}

	private Presenter presenter;
	private Widget widget;
	@UiField
	Button button;
	@UiField
	Div synpaseAlertContainer;
	
	@Inject
	public RestServiceButtonWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onClick();
			}
		});

	}
	
	@Override
	public void configure(String buttonText, ButtonType buttonType) {
		button.setText(buttonText);
		button.setType(buttonType);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setSynapseAlert(Widget widget) {
		synpaseAlertContainer.add(widget);
	}

	@Override 
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void showSuccessMessage() {
		DisplayUtils.showInfo(DisplayConstants.SUCCESS, "");
	}
}
