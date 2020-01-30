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

public class CreateBasicAccessRequirementStep2ViewImpl implements CreateBasicAccessRequirementStep2View {

	public interface Binder extends UiBinder<Widget, CreateBasicAccessRequirementStep2ViewImpl> {
	}

	Widget widget;
	@UiField
	FormGroup oldInstructionsUI;
	@UiField
	Paragraph oldInstructions;
	@UiField
	Div wikiPageContainer;
	@UiField
	Button editWikiButton;
	@UiField
	Button clearOldTermsButton;
	@UiField
	Div synAlertContainer;
	@UiField
	Div hasAccessorRequirementUI;
	@UiField
	CheckBox validatedCheckbox;
	@UiField
	CheckBox certifiedCheckbox;
	Presenter presenter;

	@Inject
	public CreateBasicAccessRequirementStep2ViewImpl(Binder binder) {
		widget = binder.createAndBindUi(this);
		editWikiButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onEditWiki();
			}
		});
		clearOldTermsButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				presenter.onClearOldInstructions();
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

	@Override
	public void setSynAlert(IsWidget w) {
		synAlertContainer.clear();
		synAlertContainer.add(w);
	}

	@Override
	public boolean isCertifiedUserRequired() {
		return certifiedCheckbox.getValue();
	}

	@Override
	public void setIsCertifiedUserRequired(boolean value) {
		certifiedCheckbox.setValue(value);
	}

	@Override
	public boolean isValidatedProfileRequired() {
		return validatedCheckbox.getValue();
	}

	@Override
	public void setIsValidatedProfileRequired(boolean value) {
		validatedCheckbox.setValue(value);
	}

	@Override
	public void setHasAccessorRequirementUIVisible(boolean visible) {
		hasAccessorRequirementUI.setVisible(visible);
	}
}
