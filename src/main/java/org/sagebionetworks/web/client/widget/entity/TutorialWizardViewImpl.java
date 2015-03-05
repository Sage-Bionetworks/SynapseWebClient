package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TutorialWizardViewImpl  implements TutorialWizardView {
	
	public static final String SKIP_TUTORIAL = "Skip Tutorial";
	public static final String BACK_TEXT = "< Back";
	public static final String NEXT_TEXT = "Next >";
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	private List<MarkdownWidget> pageContents;
	private List<WikiHeader> wikiHeaders;
	private int currentPageIndex;
	@Inject
	public TutorialWizardViewImpl(PortalGinInjector ginInjector) {
		this.ginInjector = ginInjector;
	}
	
	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}
	
	@Override
	public void showWizard(String ownerObjectId, List<WikiHeader> headers) {
		this.wikiHeaders = headers;
		final Dialog dialog = new Dialog();
       	dialog.setMaximizable(false);
        dialog.setPlain(true); 
        dialog.setModal(true);
        dialog.setWidth(900);
        //dialog.setAutoWidth(true);
        dialog.setAutoHeight(true);
        dialog.setResizable(false);
        dialog.noText = NEXT_TEXT;
        dialog.yesText = BACK_TEXT;
        dialog.cancelText = SKIP_TUTORIAL;
        dialog.setButtons(Dialog.YESNOCANCEL);
        pageContents = new ArrayList<MarkdownWidget>();
        loadAllPageContents(ownerObjectId, headers);
        currentPageIndex = 0;
        dialog.add(wrap(pageContents.get(currentPageIndex)));
 		dialog.setHeading(wikiHeaders.get(currentPageIndex).getTitle());
 		
        final com.extjs.gxt.ui.client.widget.button.Button nextButton = dialog.getButtonById(Dialog.NO);
        final com.extjs.gxt.ui.client.widget.button.Button prevButton = dialog.getButtonById(Dialog.YES);
        prevButton.setEnabled(false);
        final com.extjs.gxt.ui.client.widget.button.Button cancelButton = dialog.getButtonById(Dialog.CANCEL);
        cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				//if the user cancels, then go straight to the submit entity dialog
				dialog.hide();
				presenter.userSkippedTutorial();
			}
        });
        nextButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				//hide if we're on the last page
				if (currentPageIndex == pageContents.size()-1) {
					dialog.hide();
					presenter.userFinishedTutorial();
				} else {
					//else go to the next page
					currentPageIndex++;
					updatePageContents(dialog, currentPageIndex);
					prevButton.setEnabled(true);
			 		if (currentPageIndex == pageContents.size()-1) {
			 			nextButton.setText("OK");
			 		}
				}
			}
        });
        prevButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
					//go to the prev page
				currentPageIndex--;
				nextButton.setText(NEXT_TEXT);
				updatePageContents(dialog, currentPageIndex);
				if (currentPageIndex == 0) {
		 			prevButton.setEnabled(false);
		 		}
			}
        });

		dialog.show();	
	}
	
	private void updatePageContents(Dialog dialog, int currentPageIndex) {
		dialog.removeAll();
		dialog.add(wrap(pageContents.get(currentPageIndex)));
 		dialog.setHeading(wikiHeaders.get(currentPageIndex).getTitle());
 		dialog.layout(true);
	}
	
	public void loadAllPageContents(String ownerObjectId, List<WikiHeader> headers){
		for (WikiHeader header : headers) {
			MarkdownWidget step = ginInjector.getMarkdownWidget();
			step.loadMarkdownFromWikiPage(new WikiPageKey(ownerObjectId, ObjectType.ENTITY.toString(), header.getId()), true, false);
			pageContents.add(step);
		}
	}
	
	public Widget wrap(Widget widget) {
		widget.addStyleName("margin-10");
		return widget;
	}
	
	@Override
	public void showLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
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
	public Widget getTutorialLink(String buttonText) {
		Anchor link = new Anchor(buttonText);
		link.addStyleName("link inline-block");
		link.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				presenter.userClickedTutorialButton();
			}
		});
		return link;
	}
}
