package org.sagebionetworks.web.client.widget.user;

import java.util.List;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Row;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.view.PeopleSearchViewImpl;
import org.sagebionetworks.web.client.widget.entity.EntityBadgeViewImpl.Binder;
import org.sagebionetworks.web.client.widget.entity.browse.EntityTreeBrowserViewImpl;
import org.sagebionetworks.web.client.widget.team.MemberListWidgetView;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserGroupListWidgetViewImpl extends Composite implements UserGroupListWidgetView {
	public interface UserGroupListWidgetViewImplUiBinder extends UiBinder<Widget, UserGroupListWidgetViewImpl> {};
	
	@UiField
	Container mainContainer;
	@UiField
	Row mainRow;

	private SageImageBundle sageImageBundle;
	private PortalGinInjector portalGinInjector;
	
	private Presenter presenter;
	
	@Inject
	public UserGroupListWidgetViewImpl(UserGroupListWidgetViewImplUiBinder uiBinder,
										SageImageBundle sageImageBundle,
										PortalGinInjector portalGinInjector) {
		initWidget(uiBinder.createAndBindUi(this));
		this.sageImageBundle = sageImageBundle;
		this.portalGinInjector = portalGinInjector;
	}
	
	@Override
	public void showLoading() {
		clear();
		mainContainer.add(DisplayUtils.getLoadingWidget(sageImageBundle));
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public void configure(List<UserGroupHeader> users, boolean isBig) {
		
		for (UserGroupHeader user : users) {
			mainRow.add(getBadgeWidget(user.getOwnerId(), isBig));
		}
		
		if (users.isEmpty())
			mainRow.add(new HTML(SafeHtmlUtils.fromSafeConstant("<div class=\"smallGreyText\">" + EntityTreeBrowserViewImpl.EMPTY_DISPLAY + "</div>").asString()));
	}
	
	private Widget getBadgeWidget(String ownerId, boolean isBig) {
		if (isBig) {
			BigUserBadge userBadge = portalGinInjector.getBigUserBadgeWidget();
			userBadge.configure(ownerId);
			Widget result = userBadge.asWidget();
			result.addStyleName("col-sm-12 col-md-6 margin-top-15");
			result.setHeight("120px");
			return userBadge.asWidget();
		} else {
			UserBadge userBadge = portalGinInjector.getUserBadgeWidget();
			userBadge.configure(ownerId);
			Widget result = userBadge.asWidget();
			result.addStyleName("col-sm-12 col-md-3 margin-top-5");
			return result;
		}
	}

	@Override
	public void clear() {
		mainRow.clear();
	}

}
