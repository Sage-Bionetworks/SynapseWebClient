package org.sagebionetworks.web.client.widget.entity;

import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHistorySnapshot;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WikiVersionAnchorListItem implements IsWidget {
	public static final String WIKI_VERSION_ATTRIBUTE = "wikiversion";
	private UserBadge modifiedByBadge;
	private DateTimeUtils dateTimeUtils;
	private FocusPanel fp = new FocusPanel();

	@Inject
	public WikiVersionAnchorListItem(UserBadge modifiedByBadge, DateTimeUtils dateTimeUtils) {
		this.modifiedByBadge = modifiedByBadge;
		this.dateTimeUtils = dateTimeUtils;
		modifiedByBadge.setDoNothingOnClick();
		fp.addStyleName("imageButton");
	}

	public void setV2WikiHistorySnapshot(V2WikiHistorySnapshot version) {
		fp.clear();
		modifiedByBadge.configure(version.getModifiedBy());
		String dateTime = dateTimeUtils.getDateTimeString(version.getModifiedOn());
		Div p = new Div();
		p.addStyleName("whitespace-nowrap margin-left-5");
		p.add(new Span("v.<strong>" + version.getVersion() + "</strong> created on " + dateTime + " by "));
		p.add(modifiedByBadge);
		fp.getElement().setAttribute(WIKI_VERSION_ATTRIBUTE, version.getVersion());
		fp.setWidget(p);
	}

	@Override
	public Widget asWidget() {
		return fp;
	}

	public void addClickHandler(ClickHandler clickHandler) {
		fp.addClickHandler(clickHandler);
	}
}
