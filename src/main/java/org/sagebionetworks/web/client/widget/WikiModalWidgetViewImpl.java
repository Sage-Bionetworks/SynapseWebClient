package org.sagebionetworks.web.client.widget;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiModalWidgetViewImpl implements WikiModalWidgetView {

	public interface Binder extends UiBinder<Widget, WikiModalWidgetViewImpl> {}
	
	WikiModalWidgetView.Presenter presenter;
	
	Widget widget;

	@UiField
	Button okButton;
	@UiField
	Modal dialog;
	@UiField
	Div synAlertContainer;
	@UiField
	Div wikiPageContainer;
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Inject
	public WikiModalWidgetViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				dialog.hide();
			}
		});
	}

	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
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
	public void clear() {
		dialog.setTitle("");
	}

	@Override
	public void show() {
		dialog.show();
	}

	@Override
	public void hide() {
		dialog.hide();
	}

	@Override
	public void setSynAlert(Widget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public void setWikiPage(Widget w) {
		wikiPageContainer.clear();
		wikiPageContainer.add(w);
	}

	@Override
	public void setTitle(String title) {
		dialog.setTitle(title);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
}
