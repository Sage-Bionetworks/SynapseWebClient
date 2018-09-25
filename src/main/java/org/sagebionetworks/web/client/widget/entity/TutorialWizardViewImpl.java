package org.sagebionetworks.web.client.widget.entity;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.v2.wiki.V2WikiHeader;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TutorialWizardViewImpl implements TutorialWizardView {
	
	public static final String SKIP_TUTORIAL = "Skip Tutorial";
	public static final String BACK_TEXT = "Back";
	public static final String NEXT_TEXT = "Next";
	private Presenter presenter;
	private PortalGinInjector ginInjector;
	private List<MarkdownWidget> pageContents;
	private List<V2WikiHeader> wikiHeaders;
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
	public void showWizard(String ownerObjectId, List<V2WikiHeader> headers) {
		this.wikiHeaders = headers;
		final Modal dialog = new Modal();
		dialog.setSize(ModalSize.LARGE);
		final ModalBody body = new ModalBody();
		dialog.add(body);
		ModalFooter footer = new ModalFooter();
		final Button nextButton = new Button(NEXT_TEXT);
		nextButton.setIcon(IconType.CHEVRON_RIGHT);
		final Button prevButton = new Button(BACK_TEXT);
		prevButton.setIcon(IconType.CHEVRON_LEFT);
		final Button cancelButton = new Button(SKIP_TUTORIAL);
		final Button okButton = new Button("OK");
		footer.add(prevButton);
		footer.add(nextButton);
		footer.add(okButton);
		footer.add(cancelButton);
		dialog.add(footer);
		okButton.setVisible(false);
		pageContents = new ArrayList<MarkdownWidget>();
        loadAllPageContents(ownerObjectId, headers);
        currentPageIndex = 0;
        body.add(wrap(pageContents.get(currentPageIndex).asWidget()));
        
        dialog.setTitle(wikiHeaders.get(currentPageIndex).getTitle());
 		prevButton.setEnabled(false);
 		
 		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//if the user cancels, then go straight to the submit entity dialog
				dialog.hide();
				presenter.userSkippedTutorial();
			}
		});

 		okButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				dialog.hide();
				presenter.userFinishedTutorial();
			}
		});
 		
 		nextButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//else go to the next page
				currentPageIndex++;
				updatePageContents(dialog, body, currentPageIndex);
				prevButton.setEnabled(true);
				boolean isLastPage = currentPageIndex == pageContents.size()-1; 
		 		nextButton.setVisible(!isLastPage);
		 		okButton.setVisible(isLastPage);
			}
		});
        prevButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//go to the prev page
				currentPageIndex--;
				nextButton.setVisible(true);
				okButton.setVisible(false);
				updatePageContents(dialog, body, currentPageIndex);
				if (currentPageIndex == 0) {
		 			prevButton.setEnabled(false);
		 		}
			}
		});

		dialog.show();	
	}
	
	private void updatePageContents(Modal dialog, ModalBody body, int currentPageIndex) {
		body.clear();
		body.add(wrap(pageContents.get(currentPageIndex).asWidget()));
 		dialog.setTitle(wikiHeaders.get(currentPageIndex).getTitle());
	}
	
	public void loadAllPageContents(String ownerObjectId, List<V2WikiHeader> headers){
		for (V2WikiHeader header : headers) {
			MarkdownWidget step = ginInjector.getMarkdownWidget();
			step.loadMarkdownFromWikiPage(new WikiPageKey(ownerObjectId, ObjectType.ENTITY.toString(), header.getId()), false);
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
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
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
