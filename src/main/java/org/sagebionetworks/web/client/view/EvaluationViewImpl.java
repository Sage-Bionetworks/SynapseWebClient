package org.sagebionetworks.web.client.view;

import org.sagebionetworks.evaluation.model.EvaluationStatus;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.UserEvaluationState;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EvaluationViewImpl extends Composite implements EvaluationView {

	public interface EvaluationViewImplUiBinder extends UiBinder<Widget, EvaluationViewImpl> {}
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	
	@UiField
	SimplePanel fullWidthPanel;

	private LayoutContainer fullWidthContainer;
	
	private Presenter presenter;
	private Header headerWidget;
	private Footer footerWidget;
	private WikiPageWidget wikiPage;
	
	@Inject
	public EvaluationViewImpl(EvaluationViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, WikiPageWidget wikiPage) {		
		initWidget(binder.createAndBindUi(this));
		
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.wikiPage = wikiPage;
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
	}



	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		header.clear();
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {		
	}

	@Override
	public void showPage(final WikiPageKey wikiKey, UserEvaluationState state, final boolean canEdit){
		fullWidthContainer = initContainerAndPanel(fullWidthContainer, fullWidthPanel);
		fullWidthContainer.removeAll();
		
		fullWidthContainer.add(wikiPage.asWidget());
		wikiPage.configure(wikiKey, canEdit, new WikiPageWidget.Callback() {
			@Override
			public void pageUpdated() {
				presenter.configure(wikiKey.getOwnerObjectId());
			}
		}, false, 24);

		if (!UserEvaluationState.EVAL_REGISTRATION_UNAVAILABLE.equals(state)) {
			//show a register/unregister button
			SimplePanel buttonContainer = new SimplePanel();
			buttonContainer.addStyleName("span-24");
			
			if (UserEvaluationState.EVAL_OPEN_USER_NOT_REGISTERED.equals(state)) {
				LayoutContainer megaButton = new LayoutContainer();
				megaButton.setStyleName("mega-button");
				megaButton.setStyleAttribute("margin-top", "10px;");
				megaButton.setStyleAttribute("float", "left;");
				
				Anchor applyForChallengeLink = new Anchor();
				applyForChallengeLink.setText("Join!");
				applyForChallengeLink.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						presenter.register();
					}
				});
				megaButton.add(applyForChallengeLink);
				buttonContainer.add(megaButton);
			}
			else if (UserEvaluationState.EVAL_OPEN_USER_REGISTERED.equals(state)) {
				Button registrationButton = new Button();
				registrationButton.setText("Unregister");
				registrationButton.addSelectionListener(new SelectionListener<ButtonEvent>() {			
					@Override
					public void componentSelected(ButtonEvent ce) {
						presenter.unregister();
					}
				});
				buttonContainer.add(registrationButton);
			}
			
			fullWidthContainer.add(buttonContainer);
		}
		fullWidthContainer.layout(true);
	}

	
	private LayoutContainer initContainerAndPanel(LayoutContainer container,
			SimplePanel panel) {
		if (container == null) {
			container = new LayoutContainer();
			container.setAutoHeight(true);
			container.setAutoWidth(true);
			panel.clear();
			panel.add(container);
		}
		return container;
	}

}
