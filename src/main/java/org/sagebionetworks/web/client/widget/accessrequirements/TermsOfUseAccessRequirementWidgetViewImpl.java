package org.sagebionetworks.web.client.widget.accessrequirements;

import org.gwtbootstrap3.client.ui.BlockQuote;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TermsOfUseAccessRequirementWidgetViewImpl implements TermsOfUseAccessRequirementWidgetView {

	@UiField
	Div approvedHeading;
	@UiField
	Div unapprovedHeading;
	@UiField
	SimplePanel wikiTermsUI;
	@UiField
	BlockQuote termsUI;
	@UiField
	HTML terms;
	@UiField
	Button signTermsButton;
	
	public interface Binder extends UiBinder<Widget, TermsOfUseAccessRequirementWidgetViewImpl> {
	}
	
	Widget w;
	Presenter presenter;
	
	@Inject
	public TermsOfUseAccessRequirementWidgetViewImpl(Binder binder){
		this.w = binder.createAndBindUi(this);
		signTermsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				presenter.onSignTerms();
			}
		});
	}
	
	@Override
	public void addStyleNames(String styleNames) {
		w.addStyleName(styleNames);
	}
	
	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public Widget asWidget() {
		return w;
	}
	@Override
	public void setWikiTermsWidget(Widget wikiWidget) {
		wikiTermsUI.setWidget(wikiWidget);
	}
	@Override
	public void setTerms(String arText) {
		terms.setHTML(arText);
	}
	@Override
	public void showTermsUI() {
		termsUI.setVisible(true);
	}
	@Override
	public void showWikiTermsUI() {
		wikiTermsUI.setVisible(true);
	}
	@Override
	public void showApprovedHeading() {
		approvedHeading.setVisible(true);
	}
	@Override
	public void showUnapprovedHeading() {
		unapprovedHeading.setVisible(true);
	}
	
	@Override
	public void showSignTermsButton() {
		signTermsButton.setVisible(true);
	}
	
	@Override
	public void resetState() {
		approvedHeading.setVisible(false);
		unapprovedHeading.setVisible(false);
		signTermsButton.setVisible(false);	
	}
}
