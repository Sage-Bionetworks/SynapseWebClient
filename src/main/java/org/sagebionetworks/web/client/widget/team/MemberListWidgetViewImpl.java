package org.sagebionetworks.web.client.widget.team;

import java.util.List;

import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.DisplayUtils.ButtonType;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.TeamMemberBundle;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class MemberListWidgetViewImpl extends FlowPanel implements	MemberListWidgetView {
	public static final String MEMBER_ACCESS = "Member";
	public static final String ADMIN_ACCESS = "Team Manager";
	
	private Presenter presenter;
	private PortalGinInjector portalGinInjector;
	private SimplePanel loadMoreWidgetContainer;
	private LoadMoreWidgetContainer loadMoreWidget;
	
	@Inject
	public MemberListWidgetViewImpl(SageImageBundle sageImageBundle,
			PortalGinInjector portalGinInjector) {
		this.portalGinInjector = portalGinInjector;
		loadMoreWidgetContainer = new SimplePanel();
		add(loadMoreWidgetContainer);
	}
	@Override
	public void setMembersContainer(LoadMoreWidgetContainer loadMoreWidget) {
		this.loadMoreWidget = loadMoreWidget;
		loadMoreWidget.addStyleName("SRC-card-grid-row");
		loadMoreWidgetContainer.clear();
		loadMoreWidgetContainer.add(loadMoreWidget);
	}
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void addMembers(List<TeamMemberBundle> members, boolean isAdmin) {
		for (TeamMemberBundle teamMember : members) {
			Div singleGridItem = new Div();
			singleGridItem.addStyleName("SRC-grid-item");
			final UserProfile member = teamMember.getUserProfile();
			UserBadge userBadge = portalGinInjector.getUserBadgeWidget();
			userBadge.setSize(BadgeSize.MEDIUM);
			userBadge.configure(member);
			Div userBadgeWrapper = new Div();
			userBadgeWrapper.add(userBadge);
			singleGridItem.add(userBadgeWrapper);
			
			if (isAdmin) {
				//add simple combo
				ListBox combo = getAccessCombo(member.getOwnerId(), teamMember.getIsTeamAdmin());
				SimplePanel wrap = new SimplePanel();
				wrap.addStyleName("margin-top-5");
				wrap.setWidget(combo);
				singleGridItem.add(wrap);
				
				//add delete member button
				Button leaveButton = DisplayUtils.createButton("Remove", ButtonType.DANGER);
				leaveButton.addStyleName("pull-right margin-left-5");
				leaveButton.addClickHandler(new ClickHandler() {			
					@Override
					public void onClick(ClickEvent event) {
						DisplayUtils.showConfirmDialog("Remove Member?", DisplayUtils.getDisplayName(member) + DisplayConstants.PROMPT_SURE_REMOVE_MEMBER, 
								new Callback() {
									@Override
									public void invoke() {
										presenter.removeMember(member.getOwnerId());
									}
								});
					}
				});
				singleGridItem.add(leaveButton);
			} else if (teamMember.getIsTeamAdmin()) {
				//otherwise, indicate that this row user is an admin (via label)
				singleGridItem.add(new HTML("<span>"+ADMIN_ACCESS+"</span>"));
			}
			
			loadMoreWidget.add(singleGridItem);
		}
	}
	
	@Override
	public void clearMembers() {
		loadMoreWidget.clear();
	}
	
	private ListBox getAccessCombo(final String ownerId, boolean isAdmin) {
		final ListBox accessCombo = new ListBox();
		accessCombo.setStyleName("form-control");
		accessCombo.addItem(MEMBER_ACCESS);
		accessCombo.addItem(ADMIN_ACCESS);
		accessCombo.setWidth("150px");
		accessCombo.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean isAdmin = accessCombo.getSelectedIndex() == 1;
				presenter.setIsAdmin(ownerId, isAdmin);
			}
		});
		accessCombo.setSelectedIndex(isAdmin ? 1 : 0);
		
		return accessCombo;
	}
	
	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}
	
	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}
}
