package org.sagebionetworks.web.client.widget.entity.act;

import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class ApproveUserAccessModalViewImpl implements ApproveUserAccessModalView {
	
	public interface Binder extends UiBinder<Widget, ApproveUserAccessModalViewImpl> {}
	private static Binder uiBinder = GWT.create(Binder.class);
	
	@UiField
	Modal modal;
	@UiField
	Button accessReqNum;
	@UiField
	DropDownMenu arDropdownMenu;
	@UiField
	HTML accessReqText;
	@UiField
	Div synAlertContainer;
	@UiField
	Button submitButton;
	@UiField
	Button cancelButton;
	@UiField
	Button previewButton;
	@UiField
	TextBox emailTemplate;
	@UiField
	Div userSelectContainer;
	@UiField
	Div loadingEmail;
	@UiField
	Modal previewModal;
	@UiField
	HTML messageBody;
	@UiField
	Button closeButton;
	
	private Presenter presenter;
	
	Widget widget;

	public ApproveUserAccessModalViewImpl() {
		widget = uiBinder.createAndBindUi(this);
		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSubmit();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
		previewButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				previewModal.show();
			}
		});
		closeButton.addClickHandler(new ClickHandler() {		
			@Override		
			public void onClick(ClickEvent event) {		
				previewModal.hide();		
			}		
		});
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void setStates(List<String> states) {
		arDropdownMenu.clear();
		for (final String state : states) {
			AnchorListItem item = new AnchorListItem(state);
			item.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					presenter.onStateSelected(state);
				}
			});
			arDropdownMenu.add(item);
		}
	}
	
	@Override
	public void setUserPickerWidget(Widget w) {
		userSelectContainer.clear();
		userSelectContainer.add(w);
	}
	
	@Override
	public void setDatasetTitle(String text) {
		emailTemplate.setText(text);
	}
	
	@Override
	public void setLoadingEmailWidget(Widget w) {
		loadingEmail.add(w.asWidget());
	}
	
	@Override
	public void startLoadingEmail() {
		emailTemplate.setVisible(false);
		previewButton.setVisible(false);
		setLoadingEmailVisible(true);
	}
	
	@Override
	public void finishLoadingEmail() {
		setLoadingEmailVisible(false);
		emailTemplate.setVisible(true);
		previewButton.setVisible(true);
	}
	
	@Override
	public void setLoadingEmailVisible(boolean visible) {
		loadingEmail.clear();
		loadingEmail.setVisible(visible);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void show() {
		modal.show();
	}
	
	@Override
	public void hide() {
		modal.hide();
	}
	
	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public String getAccessRequirement() {
		return accessReqNum.getText();
	}

	@Override
	public void setApproveProcessing(boolean processing) {
		if(processing){
			submitButton.state().loading();
		}else{
			submitButton.state().reset();
		}
	}
	
	@Override
	public void setAccessRequirement(String num, String html) {
		accessReqNum.setText(num);
		accessReqText.setHTML(html);
	}

	@Override
	public void setSynAlert(Widget widget) {
		synAlertContainer.clear();
		synAlertContainer.add(widget.asWidget());		
	}
	
	@Override
	public Widget getEmailBodyWidget(String html) {
		HTML display = new HTML();
		display.setHTML(html);
		return display.asWidget();
	}
	
	@Override		
	public void setMessageBody(String html) {		
		messageBody.getElement().setInnerHTML(html);
	}
}
