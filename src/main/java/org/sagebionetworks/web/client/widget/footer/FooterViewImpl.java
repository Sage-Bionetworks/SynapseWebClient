package org.sagebionetworks.web.client.widget.footer;

import java.util.Date;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FooterViewImpl extends Composite implements FooterView {
	
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
	
	private Presenter presenter;
	private CookieProvider cookies;
	private GlobalApplicationState globalAppState;
	@Inject
	public FooterViewImpl(Binder binder, CookieProvider cookies, GlobalApplicationState globalAppState) {
		this.initWidget(binder.createAndBindUi(this));
		this.cookies = cookies;
		this.globalAppState = globalAppState;
		initDebugModeLink();		
		copyrightYear.setText(DateTimeFormat.getFormat("yyyy").format(new Date()) + " Sage Bionetworks");
	}
	
	private void initDebugModeLink() {
		debugLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!DisplayUtils.isInTestWebsite(cookies)) {
					//verify
					DisplayUtils.showConfirmDialog(
							"Alpha Test Mode", 
							DisplayConstants.TEST_MODE_WARNING, 
							new Callback() {
								@Override
								public void invoke() {
									//switch to pre-release test website mode
									DisplayUtils.setTestWebsite(true, cookies);
									Window.scrollTo(0, 0);
									globalAppState.refreshPage();
								}
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
		portalVersionSpan.setText(portalVersion);
		repoVersionSpan.setText(repoVersion);		
	}
}
