package org.sagebionetworks.web.client.widget.biodalliance13;

import java.util.List;
import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceConfigInterface;
import org.gwtvisualizationwrappers.client.biodalliance13.BiodallianceSource;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface BiodallianceWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * 
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	void setContainerId(String id);

	void setSynAlert(Widget w);

	boolean isAttached();

	/**
	 * View handles creation of biodalliance js object (biodalliance visualization puts itself into
	 * containerId).
	 * 
	 * @param urlPrefix
	 * @param containerId
	 * @param initChr
	 * @param initViewStart
	 * @param initViewEnd
	 * @param currentConfig
	 * @param sources
	 */
	void showBiodallianceBrowser(String urlPrefix, String containerId, String initChr, int initViewStart, int initViewEnd, BiodallianceConfigInterface currentConfig, List<BiodallianceSource> sources);


	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void viewAttached();
	}
}
