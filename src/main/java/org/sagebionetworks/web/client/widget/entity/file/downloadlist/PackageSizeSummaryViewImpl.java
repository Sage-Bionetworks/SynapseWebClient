package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.web.client.widget.LoadingSpinner;

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
	Span fileCountSpan;
	@UiField
	Span fileSizeSpan;
	@UiField
	Span estimatedTimeSpan;
	@UiField
	Span estimatedTimeLoading;
	@UiField
	LoadingSpinner spinner;
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
	private void setSpanValue(Span span, String value) {
		span.clear();
		String[] splitValue = value.split("\\s");
		if (splitValue.length > 1) {
			Span s1 = new Span();
			s1.add(new Text(splitValue[0]));
			s1.addStyleName("font-weight-700 font-size-15");
			
			Span s2 = new Span();
			s2.add(new Text(value.substring(splitValue[0].length())));
			s2.addStyleName("font-size-15");
			
			span.add(s1);
			span.add(s2);
		}
	}
	@Override
	public void setEstimatedDownloadTime(String time) {
		setSpanValue(estimatedTimeSpan, time);
	}
	
	@Override
	public void setFileCount(String count) {
		setSpanValue(fileCountSpan, count);
	}
	@Override
	public void setSize(String friendlyFileSize) {
		setSpanValue(fileSizeSpan, friendlyFileSize);
	}
	@Override
	public void addTextStyle(String style) {
		fileCountSpan.addStyleName(style);
		fileSizeSpan.addStyleName(style);
		estimatedTimeSpan.addStyleName(style);
	}
	@Override
	public void setEstimatedDownloadTimeLoadingVisible(boolean visible) {
		estimatedTimeSpan.setVisible(!visible);
		estimatedTimeLoading.setVisible(visible);
	}
	@Override
	public void showWhiteSpinner() {
		spinner.setIsWhite(true);
	}
}
