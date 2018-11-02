package org.sagebionetworks.web.client.widget.accessrequirements;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SelfSignAccessRequirementWidgetViewImpl implements SelfSignAccessRequirementWidgetView {

	@UiField
	Div approvedHeading;
	@UiField
	Div unapprovedHeading;
	@UiField
	SimplePanel wikiContainer; 
	@UiField
	Button signTermsButton;
	@UiField
	Button loginButton;
	@UiField
	Button certifyButton;
	@UiField
	Alert certifyNote;
	@UiField
	Button validateProfileButton;
	@UiField
	Alert validateProfileNote;
	@UiField
	Div editAccessRequirementContainer;
	@UiField
	Div deleteAccessRequirementContainer;
	@UiField
	Div subjectsWidgetContainer;
	@UiField
	Div manageAccessContainer;
	
	Callback onAttachCallback;
	public interface Binder extends UiBinder<Widget, SelfSignAccessRequirementWidgetViewImpl> {
	}
	
	Widget w;
	Presenter presenter;
	
	@Inject
	public SelfSignAccessRequirementWidgetViewImpl(Binder binder, GlobalApplicationState globalAppState){
		this.w = binder.createAndBindUi(this);
		signTermsButton.addClickHandler(event -> {
			presenter.onSignTerms();
		});
		validateProfileButton.addClickHandler(event -> {
			presenter.onValidateProfile();
		});
		certifyButton.addClickHandler(event -> {
			presenter.onCertify();
		});
		loginButton.addClickHandler(event -> {
			globalAppState.getPlaceChanger().goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
		});

		w.addAttachHandler(event -> {
			if (event.isAttached()) {
				onAttachCallback.invoke();
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
		wikiContainer.setWidget(wikiWidget);
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
		certifyButton.setVisible(false);
		certifyNote.setVisible(false);
		validateProfileButton.setVisible(false);
		validateProfileNote.setVisible(false);
		loginButton.setVisible(false);
	}
	@Override
	public void showGetCertifiedUI() {
		certifyButton.setVisible(true);
		certifyNote.setVisible(true);
	}
	@Override
	public void showGetProfileValidatedUI() {
		validateProfileButton.setVisible(true);
		validateProfileNote.setVisible(true);
	}
	
	@Override
	public void setEditAccessRequirementWidget(IsWidget w) {
		editAccessRequirementContainer.clear();
		editAccessRequirementContainer.add(w);
	}
	@Override
	public void setDeleteAccessRequirementWidget(IsWidget w) {
		deleteAccessRequirementContainer.clear();
		deleteAccessRequirementContainer.add(w);
	}
	@Override
	public void setSubjectsWidget(IsWidget w) {
		subjectsWidgetContainer.clear();
		subjectsWidgetContainer.add(w);
	}
	@Override
	public void setOnAttachCallback(Callback onAttachCallback) {
		this.onAttachCallback = onAttachCallback;
	}
	@Override
	public boolean isInViewport() {
		return DisplayUtils.isInViewport(w);
	}
	@Override
	public boolean isAttached() {
		return w.isAttached();
	}
	@Override
	public void setManageAccessWidget(IsWidget w) {
		manageAccessContainer.clear();
		manageAccessContainer.add(w);
	}
	@Override
	public void showLoginButton() {
		loginButton.setVisible(true);	
	}
}
