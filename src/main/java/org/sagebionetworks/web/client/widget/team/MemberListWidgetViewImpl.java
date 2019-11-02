package org.sagebionetworks.web.client.widget.team;

import java.util.List;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.user.BadgeSize;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.TeamMemberBundle;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class MemberListWidgetViewImpl extends FlowPanel implements MemberListWidgetView {
	public static final String MEMBER_ACCESS = "Member";
	public static final String ADMIN_ACCESS = "Team Manager";

	private Presenter presenter;
	private PortalGinInjector portalGinInjector;
	private SimplePanel loadMoreWidgetContainer;
	private LoadMoreWidgetContainer loadMoreWidget;

	@Inject
	public MemberListWidgetViewImpl(SageImageBundle sageImageBundle, PortalGinInjector portalGinInjector) {
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

			Div userBadgeWrapper = new Div();
			userBadgeWrapper.add(userBadge);
			singleGridItem.add(userBadgeWrapper);

			if (isAdmin) {
				final String ownerId = member.getOwnerId();
				final boolean isTeamAdmin = teamMember.getIsTeamAdmin();
				String commandName = isTeamAdmin ? "Make Team Member" : "Make Team Manager";
				userBadge.addContextCommand(commandName, () -> {
					presenter.setIsAdmin(ownerId, !isTeamAdmin);
				});

				userBadge.addContextCommand("Remove", () -> {
					DisplayUtils.showConfirmDialog("Remove Member?", DisplayUtils.getDisplayName(member) + DisplayConstants.PROMPT_SURE_REMOVE_MEMBER, () -> {
						presenter.removeMember(member.getOwnerId());
					});
				});
			}

			if (teamMember.getIsTeamAdmin()) {
				// otherwise, indicate that this row user is an admin (via label)
				Div teamManagerBadge = new Div();
				teamManagerBadge.addStyleName("lightGreyBackground border-left-1 border-bottom-1 border-right-1");
				teamManagerBadge.setMarginLeft(2);
				teamManagerBadge.setMarginRight(2);
				teamManagerBadge.setPaddingLeft(15);
				teamManagerBadge.add(new Icon(IconType.SYN_MANAGER));
				Span p = new Span();
				p.add(new Text(ADMIN_ACCESS));
				p.setMarginLeft(5);
				teamManagerBadge.add(p);
				singleGridItem.add(teamManagerBadge);
				singleGridItem.setHeight("130px");
			}
			userBadge.configure(member);
			loadMoreWidget.add(singleGridItem);
		}
	}

	@Override
	public void clearMembers() {
		loadMoreWidget.clear();
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
