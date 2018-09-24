package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.DownloadSpeedTester;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PackageSizeSummary implements IsWidget {
	
	private PackageSizeSummaryView view;
	private GWTWrapper gwt;
	private SynapseJSNIUtils jsniUtils;
	private DateTimeUtils dateTimeUtils;
	private double totalFileSize;
	private int totalFileCount;
	private DownloadSpeedTester downloadSpeedTester;
	boolean isTestingDownloadSpeed=false;
	@Inject
	public PackageSizeSummary(
			PackageSizeSummaryView view,
			GWTWrapper gwt,
			DownloadSpeedTester downloadSpeedTester,
			SynapseJSNIUtils jsniUtils,
			DateTimeUtils dateTimeUtils) {
		this.view = view;
		this.gwt = gwt;
		this.downloadSpeedTester = downloadSpeedTester;
		this.jsniUtils = jsniUtils;
		this.dateTimeUtils = dateTimeUtils;
	}
	
	public void clear() {
		totalFileSize = 0;
		totalFileCount = 0;
		updateView();
	}
	
	public void addFile(double fileSize) {
		totalFileCount++;
		totalFileSize += fileSize;
		updateView();
	}
	
	private void updateView() {
		view.setFileCount(totalFileCount);
		view.setSize(gwt.getFriendlySize(totalFileSize, true));
		view.setEstimatedDownloadTime("-");
		if (!isTestingDownloadSpeed) {
			isTestingDownloadSpeed = true;
			downloadSpeedTester.testDownloadSpeed(new AsyncCallback<Double>() {
				@Override
				public void onSuccess(Double bytesPerSecond) {
					// seconds = ((totalFileSizeInBytes) / bytesPerSecond)
					Double seconds = totalFileSize/bytesPerSecond;
					String estimatedTime = dateTimeUtils.getInFriendlyTimeUnits(seconds.longValue());
					view.setEstimatedDownloadTime(estimatedTime);
					isTestingDownloadSpeed = false;
				}
				
				@Override
				public void onFailure(Throwable caught) {
					jsniUtils.consoleError(caught.getMessage());
					isTestingDownloadSpeed = false;
				}
			});
		}
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
