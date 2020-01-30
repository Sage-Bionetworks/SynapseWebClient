package org.sagebionetworks.web.client.widget.search;

import java.util.Collection;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.DisplayUtils;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestBox.SuggestionCallback;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/*
 * SuggestionDisplay (for decorating the SuggestBox Popup)
 */
public class SynapseSuggestionDisplay extends SuggestBox.DefaultSuggestionDisplay {
	private Label resultsLabel;
	private ButtonGroup buttonGroup;
	private Button prevButton;
	private Button nextButton;

	private Widget popupContents; // to save when loading.

	private Div loadingPanel;

	public SynapseSuggestionDisplay() {
		super();
		getPopupPanel().addStyleName("userGroupSuggestBoxPopup");
	}

	/**
	 * Decorates the SuggestBox Popup
	 */
	@Override
	protected Widget decorateSuggestionList(Widget suggestionList) {
		setUpFields();
		FlowPanel suggestList = new FlowPanel();
		suggestList.add(suggestionList);

		FlowPanel pagingArea = new FlowPanel();
		pagingArea.addStyleName("userGroupSuggestionPagingArea");
		pagingArea.add(buttonGroup);
		pagingArea.add(resultsLabel);
		suggestList.add(pagingArea);
		return suggestList;
	}

	public Label getResultsLabel() {
		return resultsLabel;
	}

	public Button getPrevButton() {
		return prevButton;
	}

	public Button getNextButton() {
		return nextButton;
	}

	public Widget getPopupContents() {
		return popupContents;
	}

	public void showLoading(UIObject suggestBox) {
		if (loadingPanel == null) {
			loadingPanel = new Div();
			loadingPanel.setWidth(suggestBox.getOffsetWidth() + "px");
			loadingPanel.add(new Text("Loading..."));
		}
		if (!getPopupPanel().getWidget().equals(loadingPanel)) {
			popupContents = getPopupPanel().getWidget();
			getPopupPanel().setWidget(loadingPanel);
		}
		// When in a bootstrap modal, the popup panel only has the correct top position when the window is
		// scrolled up.
		// When the modal is scrolled down in the page, the gwt PopupPanel gets confused (because the
		// suggestBox always reports the same top position).
		if (isInModal(suggestBox.getElement())) {
			DisplayUtils.scrollToTop();
		}
	}

	public void hideLoading() {
		getPopupPanel().setWidget(popupContents);
	}

	private void setUpFields() {
		resultsLabel = new Label();
		resultsLabel.addStyleName("userGroupSuggesionResultsLabel");

		prevButton = new Button("Prev");
		prevButton.setEnabled(false);
		nextButton = new Button("Next");

		buttonGroup = new ButtonGroup();
		buttonGroup.addStyleName("btn-group btn-group-xs userGroupSuggestionPager");
		buttonGroup.add(prevButton);
		buttonGroup.add(nextButton);
	}

	@Override
	protected void showSuggestions(final com.google.gwt.user.client.ui.SuggestBox suggestBox, final Collection<? extends Suggestion> suggestions, final boolean isDisplayStringHTML, final boolean isAutoSelectEnabled, final SuggestionCallback callback) {
		int scrollTop = Window.getScrollTop();
		int scrollLeft = Window.getScrollLeft();
		Window.scrollTo(0, 0);
		super.showSuggestions(suggestBox, suggestions, isDisplayStringHTML, isAutoSelectEnabled, callback);
		Window.scrollTo(scrollLeft, scrollTop);
	}

	private boolean isInModal(Element el) {
		if (el == null) {
			return false;
		} else {
			String className = el.getClassName();
			if (className != null && className.contains("modal")) {
				return true;
			} else {
				return isInModal(el.getParentElement());
			}
		}
	}
}
