package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.BlockQuote;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AccessRequirementDialogViewImpl implements AccessRequirementDialogView {
	
	public interface Binder extends UiBinder<Widget, AccessRequirementDialogViewImpl> {}
	
	Presenter presenter;
	
	//this UI widget
	Modal widget;
	
	@Inject
	public AccessRequirementDialogViewImpl(Binder binder) {
		this.widget = (Modal)binder.createAndBindUi(this);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter=presenter;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void open(String url) {
		Window.open(url, "_blank", "");	
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
	
	@Override
	public void showLoading() {
	}
	
	
	@Override
	public void showModal() {
		widget.show();
	}
	
	@Override
	public void clear() {
	}

	/*
	 * Private Methods
	 */

}
