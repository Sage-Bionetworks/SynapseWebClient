package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Italic;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateAccessRequirementStep1ViewImpl implements CreateAccessRequirementStep1View {

	public interface Binder extends UiBinder<Widget, CreateAccessRequirementStep1ViewImpl> {}
	
	Widget widget;
	@UiField
	Div subjectsContainer;
	@UiField
	TextBox entityIds;
	@UiField
	Button synapseMultiIdButton;
	@UiField
	TextBox teamIds;
	@UiField
	Button teamMultiIdButton;
	@UiField
	FormGroup arTypeUI;
	
	@UiField
	Radio actTypeButton;
	@UiField
	Radio termsOfUseButton;
	Presenter presenter;
	
	@Inject
	public CreateAccessRequirementStep1ViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
		synapseMultiIdButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSetEntities();
			}
		});
		
		teamMultiIdButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSetTeams();
			}
		});
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void addSubject(IsWidget w) {
		subjectsContainer.add(w);
	}
	
	@Override
	public void clearSubjects() {
		subjectsContainer.clear();
	}
	
	@Override
	public String getEntityIds() {
		return entityIds.getText();
	}
	@Override
	public String getTeamIds() {
		return teamIds.getText();
	}
	
	@Override
	public boolean isACTAccessRequirementType() {
		return actTypeButton.getValue();
	}
	
	@Override
	public boolean isTermsOfUseAccessRequirementType() {
		return termsOfUseButton.getValue();
	}
	@Override
	public void setAccessRequirementTypeSelectionVisible(boolean visible) {
		arTypeUI.setVisible(visible);
	}
	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}
}
