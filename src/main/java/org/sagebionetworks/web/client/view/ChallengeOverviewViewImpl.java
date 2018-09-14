package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ChallengeOverviewViewImpl extends Composite implements ChallengeOverviewView {

	public interface ChallengeOverviewViewImplUiBinder extends UiBinder<Widget, ChallengeOverviewViewImpl> {}

	@UiField
	SimplePanel content;
	
	private Presenter presenter;
	private Header headerWidget;
	private WikiPageWidget wikiPage;
	
	@Inject
	public ChallengeOverviewViewImpl(ChallengeOverviewViewImplUiBinder binder,
			Header headerWidget, WikiPageWidget wikiPage) {		
		initWidget(binder.createAndBindUi(this));

		this.headerWidget = headerWidget;
		this.wikiPage = wikiPage;
		headerWidget.configure();
	}

	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		headerWidget.configure();
		headerWidget.refresh();
		Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public void showOverView() {
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {		
	}

	@Override
	public void showChallengeInfo(){
		content.clear();
		content.add(wikiPage.asWidget());
		WikiPageKey wikiKey = new WikiPageKey("syn1929437", ObjectType.ENTITY.toString(), null);
		wikiPage.configure(wikiKey, false, new WikiPageWidget.Callback() {
			@Override
			public void pageUpdated() {
			}
			@Override
			public void noWikiFound() {
			}
		});
	}

	@Override
	public void showSubmissionAcknowledgement() {
		DisplayUtils.showInfoDialog("Submission Received", "Your submission has been received.", null);
	}



	@Override
	public void showSubmissionError() {
		DisplayUtils.showErrorMessage("There was an error during submission.  Please try again or contact Sage Bionetworks.");
	}

}
