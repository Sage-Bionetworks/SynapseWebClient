package org.sagebionetworks.web.unitclient.widget.asynch;

import org.sagebionetworks.repo.model.asynch.AsynchronousRequestBody;
import org.sagebionetworks.repo.model.asynch.AsynchronousResponseBody;
import org.sagebionetworks.web.client.widget.asynch.AsynchronousProgressHandler;
import org.sagebionetworks.web.client.widget.asynch.JobTrackingWidget;
import org.sagebionetworks.web.shared.asynch.AsynchType;
import com.google.gwt.user.client.ui.Widget;

/**
 * Helper for testing dependencies of JobTrackingWidget.
 * 
 * @author John
 *
 */
public class JobTrackingWidgetStub implements JobTrackingWidget {

	AsynchronousResponseBody response;
	Throwable error;
	boolean onCancel = false;

	@Override
	public Widget asWidget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startAndTrackJob(String title, boolean isDeterminate, AsynchType type, AsynchronousRequestBody requestBody, AsynchronousProgressHandler handler) {
		if (this.onCancel) {
			handler.onCancel();
		} else if (error != null) {
			handler.onFailure(error);
		} else {
			handler.onComplete(response);
		}
	}

	public void setResponse(AsynchronousResponseBody response) {
		this.response = response;
	}

	public void setError(Throwable error) {
		this.error = error;
	}

	public void setOnCancel(boolean onCancel) {
		this.onCancel = onCancel;
	}

}
