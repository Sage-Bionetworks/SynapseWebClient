package org.sagebionetworks.web.client.widget.doi;

import com.google.gwt.user.client.ui.IsWidget;

public interface CreateOrUpdateDoiModalView extends IsWidget {

	void setPresenter(Presenter presenter);

	interface Presenter {
		void onSaveDoi();
	}

	String getCreators();

	void setCreators(String creators);

	String getTitles();

	void setTitles(String titles);

	String getResourceTypeGeneral();

	void setResourceTypeGeneral(String resourceTypeGeneral);

	Long getPublicationYear();

	void setPublicationYear(Long publicationYear);

	void showOverwriteWarning(boolean showWarning);

	void show();

	void hide();

	void setModalTitle(String title);

	void setJobTrackingWidget(IsWidget w);

	void setSynAlert(IsWidget w);

	void reset();

	void setIsLoading(boolean isLoading);
}
