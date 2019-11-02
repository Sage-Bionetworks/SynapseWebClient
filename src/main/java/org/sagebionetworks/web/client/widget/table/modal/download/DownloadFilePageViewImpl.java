package org.sagebionetworks.web.client.widget.table.modal.download;

import org.gwtbootstrap3.client.ui.Form;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownloadFilePageViewImpl implements DownloadFilePageView {

	public interface Binder extends UiBinder<Form, DownloadFilePageViewImpl> {
	}

	Form form;

	@Inject
	public DownloadFilePageViewImpl(Binder binder) {
		form = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return form;
	}

}
