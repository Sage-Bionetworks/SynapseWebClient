package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PackageSizeSummaryViewImpl implements PackageSizeSummaryView, IsWidget {
	Widget w;
	interface PackageSizeSummaryViewImplUiBinder extends UiBinder<Widget, PackageSizeSummaryViewImpl> {}
	@UiField
	Text fileCount;
	@UiField
	Text fileSize;
	@UiField
	Text estimatedTime;
	private static PackageSizeSummaryViewImplUiBinder uiBinder = GWT
			.create(PackageSizeSummaryViewImplUiBinder.class);
	@Inject
	public PackageSizeSummaryViewImpl() {
		w = uiBinder.createAndBindUi(this);
	}
	@Override
	public Widget asWidget() {
		return w;
	}
	@Override
	public void setEstimatedDownloadTime(String time) {
		estimatedTime.setText(time);
	}
	@Override
	public void setFileCount(int count) {
		fileCount.setText(Integer.toString(count));
	}
	@Override
	public void setSize(String friendlyFileSize) {
		fileSize.setText(friendlyFileSize);
	}
}
