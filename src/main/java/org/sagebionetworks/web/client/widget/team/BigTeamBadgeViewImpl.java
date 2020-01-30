package org.sagebionetworks.web.client.widget.team;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.Linkify;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.TextBoxWithCopyToClipboardWidget;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BigTeamBadgeViewImpl implements BigTeamBadgeView {
	public interface Binder extends UiBinder<Widget, BigTeamBadgeViewImpl> {
	}

	@UiField
	Span pictureSpan;
	@UiField
	FocusPanel pictureFocusPanel;
	@UiField
	Span defaultPicture;
	@UiField
	Image teamPicture;
	@UiField
	Anchor nameLink;
	@UiField
	Div descriptionContainer;
	@UiField
	Span notificationsSpan;
	@UiField
	Span memberCountContainer;
	@UiField
	TextBoxWithCopyToClipboardWidget synapseEmailField;

	SynapseJSNIUtils synapseJSNIUtils;
	GlobalApplicationState globalApplicationState;
	IconsImageBundle iconsImageBundle;
	Linkify linkify;
	Widget widget;

	@Inject
	public BigTeamBadgeViewImpl(Binder uiBinder, SynapseJSNIUtils synapseJSNIUtils, GlobalApplicationState globalApplicationState, IconsImageBundle iconsImageBundle, Linkify linkify) {
		widget = uiBinder.createAndBindUi(this);
		this.synapseJSNIUtils = synapseJSNIUtils;
		this.globalApplicationState = globalApplicationState;
		this.iconsImageBundle = iconsImageBundle;
		this.linkify = linkify;
		addStyleName("bigTeamBadge");
	}

	@Override
	public void setTeam(final Team team, String description) {
		teamPicture.setVisible(false);
		defaultPicture.setVisible(false);
		notificationsSpan.clear();
		if (team == null)
			throw new IllegalArgumentException("Team is required");

		String name = team.getName();
		ClickHandler clickHandler = event -> {
			event.preventDefault();
			globalApplicationState.getPlaceChanger().goTo(new org.sagebionetworks.web.client.place.Team(team.getId()));
		};
		if (team.getIcon() != null && team.getIcon().length() > 0) {
			teamPicture.setVisible(true);
			teamPicture.setUrl(synapseJSNIUtils.getFileHandleAssociationUrl(team.getId(), FileHandleAssociateType.TeamAttachment, team.getIcon()));
		} else {
			defaultPicture.setVisible(true);
		}
		String descriptionWithoutHtml = SafeHtmlUtils.htmlEscape(description);
		descriptionContainer.clear();
		descriptionContainer.add(new HTML(linkify.linkify(descriptionWithoutHtml)));

		nameLink.setText(name);
		nameLink.setHref("#!Team:" + team.getId());
		pictureFocusPanel.addClickHandler(clickHandler);
	}

	@Override
	public void showLoadError(String principalId) {
		descriptionContainer.clear();
		descriptionContainer.add(new Text(DisplayConstants.ERROR_LOADING));
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {}

	@Override
	public void showErrorMessage(String message) {}

	@Override
	public void setRequestCount(String count) {
		InlineHTML widget = new InlineHTML(DisplayUtils.getBadgeHtml(count));
		notificationsSpan.add(DisplayUtils.addTooltip(widget, DisplayConstants.PENDING_JOIN_REQUESTS_TOOLTIP));
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clear() {}

	@Override
	public void addStyleName(String style) {
		widget.addStyleName(style);
	}

	@Override
	public void setHeight(String height) {
		widget.setHeight(height);
	}

	@Override
	public void setMemberCountWidget(IsWidget widget) {
		memberCountContainer.clear();
		memberCountContainer.add(widget);
	}

	@Override
	public void setTeamEmailAddress(String teamEmail) {
		synapseEmailField.setText(teamEmail);
	}
}
