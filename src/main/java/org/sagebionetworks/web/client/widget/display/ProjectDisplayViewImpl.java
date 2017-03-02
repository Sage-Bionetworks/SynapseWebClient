package org.sagebionetworks.web.client.widget.display;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Modal;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProjectDisplayViewImpl implements ProjectDisplayView {
	
	public interface ProjectDisplayViewImplUiBinder 
			extends UiBinder<Widget, ProjectDisplayViewImpl> {}
	
	@UiField
	Modal modal;
	
	@UiField
	SimplePanel synAlertPanel;
	
	@UiField
	CheckBox wikiButton;
	@UiField
	CheckBox filesButton;
	@UiField
	CheckBox tablesButton;
	@UiField
	CheckBox challengeButton;
	@UiField
	CheckBox discussionButton;
	
	@UiField
	Button saveButton;
	
	@UiField
	Button cancelButton;
	
	
	Widget widget;
	Presenter presenter;
	
	@Inject
	public ProjectDisplayViewImpl(ProjectDisplayViewImplUiBinder binder) {
		widget = binder.createAndBindUi(this);
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSave();
			}
		});
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				modal.hide();
			}
		});
		
	}
	
	@Override
	public void configure() {
		
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setSynAlertWidget(IsWidget synAlert) {
		synAlertPanel.setWidget(synAlert);
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void clear() {
		
	}
	
	@Override
	public void hide() {
		modal.hide();
	}
	
	@Override
	public void show() {
		modal.show();
	}

	@Override
	public void onSave() {
		// TODO Auto-generated method stub
		
	}
}

