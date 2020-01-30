package org.sagebionetworks.web.unitclient.widget.entity.file.downloadlist;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummary.FILES;
import static org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummary.NO_VALUE_STRING;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.DownloadSpeedTester;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummary;
import org.sagebionetworks.web.client.widget.entity.file.downloadlist.PackageSizeSummaryView;
import com.google.gwt.user.client.rpc.AsyncCallback;

@RunWith(MockitoJUnitRunner.class)
public class PackageSizeSummaryTest {
	PackageSizeSummary widget;
	@Mock
	PackageSizeSummaryView mockView;
	@Mock
	GWTWrapper mockGwt;
	@Mock
	DownloadSpeedTester mockDownloadSpeedTester;
	@Mock
	SynapseJSNIUtils mockJsniUtils;
	@Mock
	DateTimeUtils mockDateTimeUtils;
	// capturing download speed tester async test since we don't want it to auto-respond
	@Captor
	ArgumentCaptor<AsyncCallback> asyncCallbackCaptor;
	public static final String FRIENDLY_SIZE = "16 bytes";
	public static final String ZERO_BYTES = "0 bytes";
	public static final String FRIENDLY_TIME_ESTIMATE = "1 s";

	@Before
	public void setUp() throws Exception {
		widget = new PackageSizeSummary(mockView, mockGwt, mockDownloadSpeedTester, mockJsniUtils, mockDateTimeUtils);
		when(mockGwt.getFriendlySize(anyDouble(), anyBoolean())).thenReturn(FRIENDLY_SIZE);
		when(mockDateTimeUtils.getFriendlyTimeEstimate(anyLong())).thenReturn(FRIENDLY_TIME_ESTIMATE);
		widget.clear();
	}

	@Test
	public void testClear() {
		// cleared in setup
		verify(mockView).setEstimatedDownloadTime(NO_VALUE_STRING);
		verify(mockView).setFileCount(NO_VALUE_STRING);
		verify(mockView).setSize(NO_VALUE_STRING);
		verify(mockDownloadSpeedTester, never()).testDownloadSpeed(any(AsyncCallback.class));
	}

	@Test
	public void testAddFiles() {
		double fileSize1 = 16.0;

		widget.addFile(fileSize1);

		verify(mockView).setFileCount(Integer.toString(1) + FILES);
		verify(mockView).setSize(FRIENDLY_SIZE);
		verify(mockDownloadSpeedTester).testDownloadSpeed(any(AsyncCallback.class));

		double fileSize2 = 32.0;

		widget.addFile(fileSize2);

		verify(mockView).setFileCount(Integer.toString(2) + FILES);
		// verify it did not start another download speed test
		verify(mockDownloadSpeedTester).testDownloadSpeed(any(AsyncCallback.class));
	}

	@Test
	public void testSpeedTestSuccess() {
		Double bytesPerSecond = 16.0;
		Double fileSize1 = bytesPerSecond;

		widget.addFile(fileSize1);

		verify(mockDownloadSpeedTester).testDownloadSpeed(asyncCallbackCaptor.capture());
		asyncCallbackCaptor.getValue().onSuccess(bytesPerSecond);
		verify(mockDateTimeUtils).getFriendlyTimeEstimate(anyLong());
		verify(mockView).setEstimatedDownloadTime(FRIENDLY_TIME_ESTIMATE);
	}

	@Test
	public void testSpeedTestFailure() {
		String errorMessage = "unable to determine download speed";
		Double fileSize1 = 16.0;

		widget.addFile(fileSize1);

		verify(mockDownloadSpeedTester).testDownloadSpeed(asyncCallbackCaptor.capture());
		asyncCallbackCaptor.getValue().onFailure(new Exception(errorMessage));
		verify(mockJsniUtils).consoleError(errorMessage);
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}
}
