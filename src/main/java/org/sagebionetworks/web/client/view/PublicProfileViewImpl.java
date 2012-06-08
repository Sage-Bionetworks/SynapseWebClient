package org.sagebionetworks.web.client.view;

import java.util.ArrayList;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.footer.Footer;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.Header.MenuItems;

import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PublicProfileViewImpl extends Composite implements PublicProfileView {

	public interface PublicProfileViewImplUiBinder extends UiBinder<Widget, PublicProfileViewImpl> {}
	
	@UiField
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel userInfoPanel;

	private Presenter presenter;
	private Header headerWidget;
	private IconsImageBundle iconsImageBundle;
	private SageImageBundle sageImageBundle;

	@Inject
	public PublicProfileViewImpl(PublicProfileViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, IconsImageBundle icons,
			SageImageBundle imageBundle, 
			SageImageBundle sageImageBundle) {
		initWidget(binder.createAndBindUi(this));

		this.iconsImageBundle = icons;
		this.headerWidget = headerWidget;
		this.sageImageBundle = sageImageBundle;
		
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
		headerWidget.setMenuItemActive(MenuItems.PROJECTS);
	}
	
	@Override	
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;		
		headerWidget.refresh();				
		Window.scrollTo(0, 0); // scroll user to top of page
	}		
	
	@Override
	public void render() {
		presenter.getUserInfo();
	}

	@Override
	public void showLoading() {	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {
		userInfoPanel.clear();
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}	
	
	@Override
	public void updateWithUserInfo(String name, ArrayList<String> userInfo) {
		// User's name
		SafeHtmlBuilder shb = new SafeHtmlBuilder();
		shb.appendHtmlConstant("<h2>").appendEscaped(name).appendHtmlConstant("</h2> <ul>");
		
		// Rest of user's info
		for(int i = 0; i < userInfo.size(); i++) {
			shb.appendHtmlConstant("<li>").appendEscaped(userInfo.get(i)).appendHtmlConstant("</li>");
		}
		
		shb.appendHtmlConstant("</ul>");
		userInfoPanel.add(new HTML(shb.toSafeHtml()));
	}
	
	/*
	 * Private Methods
	 */	

	private void createUserInfoPanel() {
		userInfoPanel = new SimplePanel();
		userInfoPanel.clear();
	}
	
}