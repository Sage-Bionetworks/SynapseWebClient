package org.sagebionetworks.web.client.widget.footer;

import java.util.Date;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.accessrequirements.ToggleACTActionsButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FooterViewImpl implements FooterView {
	
	public interface Binder extends UiBinder<Widget, FooterViewImpl> {
	}

	@UiField
	Anchor debugLink;	
	@UiField
	Anchor copyrightYear;
	@UiField
	Span portalVersionSpan;
	@UiField
	Span repoVersionSpan;
	@UiField
	Div sponsorsUI;
	@UiField
	Anchor reportAbuseLink;
	@UiField
	Span hideACTActionsContainer;
	String portalVersion, repoVersion;
	private Presenter presenter;
	private CookieProvider cookies;
	private GlobalApplicationState globalAppState;
	private ToggleACTActionsButton hideACTActionsButton;
	Div container = new Div();
	boolean sponsorsVisible;
	
	@Inject
	public FooterViewImpl(Binder binder, CookieProvider cookies, GlobalApplicationState globalAppState, ToggleACTActionsButton hideACTActionsButton, GWTWrapper gwt) {
		//defer constructing this view (to give a chance for other page components to load first)
		Callback constructViewCallback = () -> {
			IsWidget widget = binder.createAndBindUi(this);
			container.add(widget);
			
			initDebugModeLink();
			hideACTActionsContainer.add(hideACTActionsButton);
			copyrightYear.setText(DateTimeFormat.getFormat("yyyy").format(new Date()) + " Sage Bionetworks");
			reportAbuseLink.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					presenter.onReportAbuseClicked();
				}
			});
			
			if (portalVersion != null) {
				portalVersionSpan.setText(portalVersion);
				repoVersionSpan.setText(repoVersion);
			}
			
			sponsorsUI.setVisible(sponsorsVisible);
		};
		gwt.scheduleExecution(constructViewCallback, 2500);
		this.cookies = cookies;
		this.globalAppState = globalAppState;
		this.hideACTActionsButton = hideACTActionsButton;
	}
	
	@Override
	public Widget asWidget() {
		return container;
	}
	
	private void initDebugModeLink() {
		debugLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!DisplayUtils.isInTestWebsite(cookies)) {
					//verify
					DisplayUtils.confirm(DisplayConstants.TEST_MODE_WARNING, () -> {
						//switch to pre-release test website mode
						DisplayUtils.setTestWebsite(true, cookies);
						Window.scrollTo(0, 0);
						globalAppState.refreshPage();
					});
				} else {
					//switch back to standard mode
					DisplayUtils.setTestWebsite(false, cookies);
					globalAppState.refreshPage();
				}
				
			}
		});
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void setVersion(String portalVersion, String repoVersion) {
		if(portalVersion == null) portalVersion = "--";
		if(repoVersion == null) repoVersion = "--";
		this.portalVersion = portalVersion;
		this.repoVersion = repoVersion;
		if (portalVersionSpan != null) {
			portalVersionSpan.setText(portalVersion);
			repoVersionSpan.setText(repoVersion);	
		}
	}
	@Override
	public void open(String url) {
		DisplayUtils.newWindow(url, "_blank", "");
	}
	
	@Override
	public void refresh() {
		hideACTActionsButton.refresh();	
	}
	
	@Override
	public void setSynapseSponsorsVisible(boolean visible) {
		sponsorsVisible = visible;
		if (sponsorsUI != null) {
			sponsorsUI.setVisible(visible);	
		}
	}
}
