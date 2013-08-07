package org.sagebionetworks.web.client.view;

import org.sagebionetworks.repo.model.message.ObjectType;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.IconsImageBundle;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.footer.Footer;
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
	SimplePanel header;
	@UiField
	SimplePanel footer;
	@UiField
	SimplePanel content;
	
	private Presenter presenter;
	private IconsImageBundle icons;
	private Header headerWidget;
	private Footer footerWidget;
	private WikiPageWidget wikiPage;
	
	@Inject
	public ChallengeOverviewViewImpl(ChallengeOverviewViewImplUiBinder binder,
			Header headerWidget, Footer footerWidget, IconsImageBundle icons,
			SageImageBundle imageBundle, WikiPageWidget wikiPage) {		
		initWidget(binder.createAndBindUi(this));

		this.icons = icons;
		this.headerWidget = headerWidget;
		this.footerWidget = footerWidget;
		this.wikiPage = wikiPage;
		header.add(headerWidget.asWidget());
		footer.add(footerWidget.asWidget());
	}



	@Override
	public void setPresenter(final Presenter presenter) {
		this.presenter = presenter;
		header.clear();
		header.add(headerWidget.asWidget());
		footer.clear();
		footer.add(footerWidget.asWidget());
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
	public void showInfo(String title, String message) {
		DisplayUtils.showInfo(title, message);
	}

	@Override
	public void clear() {		
	}

	@Override
	public void showChallengeInfo(){
		content.clear();
		content.add(wikiPage.asWidget());
		WikiPageKey wikiKey = new WikiPageKey("syn1929437", ObjectType.ENTITY.toString(), null, null);
		wikiPage.configure(wikiKey, false, new WikiPageWidget.Callback() {
			@Override
			public void pageUpdated() {
			}
			@Override
			public void noWikiFound() {
			}
		}, true, 24);
	}

	@Override
	public void showSubmissionAcknowledgement() {
		Window.alert("Your submission has been received.");
	}



	@Override
	public void showSubmissionError() {
		Window.alert("There was an error during submission.  Please try again or contact Sage Bionetworks.");
	}

}
