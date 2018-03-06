package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.inject.Inject;

public class WikiVersionAnchorListItem extends AnchorListItem {
	public static final String WIKI_VERSION_ATTRIBUTE = "wikiversion";
	private UserBadge modifiedByBadge;
	private DateTimeUtils dateTimeUtils;
	
	@Inject
	public WikiVersionAnchorListItem(
			UserBadge modifiedByBadge,
			DateTimeUtils dateTimeUtils) {
		this.modifiedByBadge = modifiedByBadge;
		this.dateTimeUtils = dateTimeUtils;
		modifiedByBadge.setDoNothingOnClick();
	}
	
	public void setV2WikiHistorySnapshot(V2WikiHistorySnapshot version) {
		clear();
		modifiedByBadge.configure(version.getModifiedBy());
		String dateTime = dateTimeUtils.convertDateToSmallString(version.getModifiedOn());
		add(new Span(version.getVersion() + " - " + dateTime + " - "));
		add(modifiedByBadge);
		getElement().setAttribute(WIKI_VERSION_ATTRIBUTE, version.getVersion());
	}
}
