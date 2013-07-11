package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.repo.model.wiki.WikiHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TutorialWizardViewImpl  implements TutorialWizardView {
	
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
        dialog.okText = "Next";
        dialog.cancelText = "Skip Tutorial";
        dialog.setButtons(Dialog.OKCANCEL);
        pageContents = new ArrayList<MarkdownWidget>();
        loadAllPageContents(ownerObjectId, headers);
        currentPageIndex = 0;
        dialog.add(wrap(pageContents.get(currentPageIndex)));
 		dialog.setHeading(wikiHeaders.get(currentPageIndex).getTitle());
 		
        final com.extjs.gxt.ui.client.widget.button.Button button = dialog.getButtonById(Dialog.OK);
        final com.extjs.gxt.ui.client.widget.button.Button cancelButton = dialog.getButtonById(Dialog.CANCEL);
        cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				//if the user cancels, then go straight to the submit entity dialog
				dialog.hide();
				presenter.userSkippedTutorial();
			}
        });
        button.addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				//hide if we're on the last page
				if (currentPageIndex == pageContents.size()-1) {
					dialog.hide();
					presenter.userFinishedTutorial();
				} else {
					//else go to the next page
					dialog.removeAll();
					currentPageIndex++;
					dialog.add(wrap(pageContents.get(currentPageIndex)));
			 		dialog.setHeading(wikiHeaders.get(currentPageIndex).getTitle());
			 		dialog.layout(true);
			 		if (currentPageIndex == pageContents.size()-1) {
			 			button.setText("OK");
			 		}
				}
			}
        });
		dialog.show();	
	}
	
	public void loadAllPageContents(String ownerObjectId, List<WikiHeader> headers){
		for (WikiHeader header : headers) {
			MarkdownWidget step = ginInjector.getMarkdownWidget();
			step.loadMarkdownFromWikiPage(new WikiPageKey(ownerObjectId, ObjectType.ENTITY.toString(), header.getId()), true);
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

}
