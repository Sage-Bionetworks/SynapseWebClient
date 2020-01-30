package org.sagebionetworks.web.client.widget.search;

import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.SearchUtil;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchBox implements SearchBoxView.Presenter, SynapseWidgetPresenter {

	private SearchBoxView view;
	private GlobalApplicationState globalApplicationState;
	public static final RegExp DOI_REGEX = RegExp.compile("10[.]{1}[0-9]+[/]{1}(syn([0-9]+[.]?[0-9]*)+)$", "i");

	@Inject
	public SearchBox(SearchBoxView view, GlobalApplicationState globalApplicationState) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		view.setPresenter(this);
	}

	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}

	public void clearState() {
		view.clear();
	}

	@Override
	public void search(String value) {
		if (value != null && !value.isEmpty()) {
			value = value.trim();
			if (value.charAt(0) == '@') {
				globalApplicationState.getPlaceChanger().goTo(new PeopleSearch(value.substring(1)));
			} else {
				MatchResult matcher = DOI_REGEX.exec(value);
				if (matcher != null && matcher.getGroupCount() > 0) {
					globalApplicationState.getPlaceChanger().goTo(new Synapse(matcher.getGroup(1)));
				} else {
					SearchUtil.searchForTerm(value, globalApplicationState);
				}
			}
		}
	}

	public void setVisible(boolean isVisible) {
		view.setVisible(isVisible);
	}
}
