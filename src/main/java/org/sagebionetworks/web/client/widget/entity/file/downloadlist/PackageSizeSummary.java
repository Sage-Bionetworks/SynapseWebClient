package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.DownloadSpeedTester;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PackageSizeSummary implements IsWidget {

	public static final String FILES = " files";
	public static final String NO_VALUE_STRING = "-";
	public static final String UNKNOWN_TIME = NO_VALUE_STRING;
	private PackageSizeSummaryView view;
	private GWTWrapper gwt;
	private SynapseJSNIUtils jsniUtils;
	private DateTimeUtils dateTimeUtils;
	private double totalFileSize;
	private int totalFileCount;
	private DownloadSpeedTester downloadSpeedTester;
	boolean isTestingDownloadSpeed = false;

	@Inject
	public PackageSizeSummary(PackageSizeSummaryView view, GWTWrapper gwt, DownloadSpeedTester downloadSpeedTester, SynapseJSNIUtils jsniUtils, DateTimeUtils dateTimeUtils) {
		this.view = view;
		this.gwt = gwt;
		this.downloadSpeedTester = downloadSpeedTester;
		this.jsniUtils = jsniUtils;
		this.dateTimeUtils = dateTimeUtils;
	}

	public void clear() {
		totalFileSize = 0;
		totalFileCount = 0;
		view.setFileCount(NO_VALUE_STRING);
		view.setSize(NO_VALUE_STRING);
		view.setEstimatedDownloadTime(NO_VALUE_STRING);
	}

	public void addFile(double fileSize) {
		totalFileCount++;
		totalFileSize += fileSize;
		updateView();
	}

	public void addFiles(int fileCount, double fileSize) {
		totalFileCount += fileCount;
		totalFileSize += fileSize;
		updateView();
	}

	private void updateView() {
		view.setFileCount(Integer.toString(totalFileCount) + FILES);
		view.setSize(gwt.getFriendlySize(totalFileSize, true));
		view.setEstimatedDownloadTime(UNKNOWN_TIME);
		if (!isTestingDownloadSpeed) {
			isTestingDownloadSpeed = true;
			view.setEstimatedDownloadTimeLoadingVisible(true);
			downloadSpeedTester.testDownloadSpeed(new AsyncCallback<Double>() {
				@Override
				public void onSuccess(Double bytesPerSecond) {
					view.setEstimatedDownloadTimeLoadingVisible(false);
					// seconds = ((totalFileSizeInBytes) / bytesPerSecond)
					Double seconds = totalFileSize / bytesPerSecond;
					String estimatedTime = dateTimeUtils.getFriendlyTimeEstimate(seconds.longValue());
					view.setEstimatedDownloadTime(estimatedTime);
					isTestingDownloadSpeed = false;
				}

				@Override
				public void onFailure(Throwable caught) {
					view.setEstimatedDownloadTimeLoadingVisible(false);
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

	public void addTextStyle(String style) {
		view.addTextStyle(style);
	}

	public void showWhiteSpinner() {
		view.showWhiteSpinner();
	}

	public double getTotalFileSize() {
		return totalFileSize;
	}

	public int getTotalFileCount() {
		return totalFileCount;
	}
}
