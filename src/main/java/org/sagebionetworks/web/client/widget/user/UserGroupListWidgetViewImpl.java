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
import org.sagebionetworks.web.client.widget.team.BigTeamBadge;
import org.sagebionetworks.web.client.widget.team.MemberListWidgetView;
import org.sagebionetworks.web.client.widget.team.TeamBadge;

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

	private Presenter presenter;
	
	@Inject
	public UserGroupListWidgetViewImpl(UserGroupListWidgetViewImplUiBinder uiBinder) {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@Override
	public void showLoading() {
		clear();
		mainContainer.add(DisplayUtils.getLoadingWidget());
	}
	
	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
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
	public void configure(List<UserGroupHeader> users) {
		
		for (UserGroupHeader user : users) {
			Widget badge = presenter.getBadgeWidget(user.getOwnerId(), user.getIsIndividual(), user.getUserName());
			styleBadgeWidget(badge, presenter.getIsBig());
			mainRow.add(badge);
		}
		
		if (users.isEmpty())
			mainRow.add(new HTML(SafeHtmlUtils.fromSafeConstant("<div class=\"smallGreyText\">" + EntityTreeBrowserViewImpl.EMPTY_DISPLAY + "</div>").asString()));
	}
	
	private void styleBadgeWidget(Widget badge, boolean isBig) {
		if (isBig) {
			badge.addStyleName("col-sm-12 col-md-6 margin-top-15");
			badge.setHeight("120px");
		} else {
			badge.addStyleName("col-sm-12 col-md-3 margin-top-5");
		}
	}
	
	@Override
	public void clear() {
		mainRow.clear();
	}

}
