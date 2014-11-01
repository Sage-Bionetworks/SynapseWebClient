package org.sagebionetworks.web.client.widget.search;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.gwt.HTMLPanel;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SageImageBundle;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;

/*
 * SuggestionDisplay (for decorating the SuggestBox Popup)
 */
public class UserGroupSuggestionDisplay extends SuggestBox.DefaultSuggestionDisplay {
	private SageImageBundle sageImageBundle;
	
	private Label resultsLabel;
	private ButtonGroup buttonGroup;
	private Button prevButton;
	private Button nextButton;
	
	private Widget popupContents; // to save when loading.
	
	private HTMLPanel loadingPanel;
	
	public UserGroupSuggestionDisplay(SageImageBundle sageImageBundle) {
		super();
		this.sageImageBundle = sageImageBundle;
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
	
	public Label getResultsLabel()	{	return resultsLabel;	}
	public Button getPrevButton()	{	return prevButton;		}
	public Button getNextButton()	{	return nextButton;		}
	public Widget getPopupContents(){	return popupContents;	}
	
	
	public void showLoading(UserGroupSuggestBoxViewImpl suggestBox) {
		popupContents = getPopupPanel().getWidget();
		if (loadingPanel == null) {
			loadingPanel = new HTMLPanel(DisplayUtils.getLoadingHtml(sageImageBundle));
			loadingPanel.setWidth(suggestBox.getOffsetWidth() + "px");
		}
		getPopupPanel().setWidget(loadingPanel);
		getPopupPanel().showRelativeTo(suggestBox);
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
}
