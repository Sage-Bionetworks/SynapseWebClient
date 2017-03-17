package org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CreateTermsOfUseAccessRequirementStep2ViewImpl implements CreateTermsOfUseAccessRequirementStep2View {

	public interface Binder extends UiBinder<Widget, CreateTermsOfUseAccessRequirementStep2ViewImpl> {}
	
	Widget widget;
	@UiField
	FormGroup oldInstructionsUI;
	@UiField
	Paragraph oldInstructions;
	@UiField
	Div wikiPageContainer;
	@UiField
	Button editWikiButton;
	
	Presenter presenter;
	
	@Inject
	public CreateTermsOfUseAccessRequirementStep2ViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
		editWikiButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEditWiki();
			}
		});
		
	}
	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}

	@Override
	public void setOldTermsVisible(boolean visible) {
		oldInstructionsUI.setVisible(visible);
	}
	@Override
	public void setOldTerms(String terms) {
		oldInstructions.setText(terms);
	}
	@Override
	public void setWikiPageRenderer(IsWidget w) {
		wikiPageContainer.clear();
		wikiPageContainer.add(w);
	}
}
