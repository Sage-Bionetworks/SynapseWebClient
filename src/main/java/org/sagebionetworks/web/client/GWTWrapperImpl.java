package org.sagebionetworks.web.client;

import java.util.Date;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.user.client.rpc.HasRpcToken;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class GWTWrapperImpl implements GWTWrapper {

	private final static RegExp PATTERN_WHITE_SPACE = RegExp.compile("^\\s+$");
	// Used to replace all characters expect letters and numbers.
	private final static RegExp PRINICPAL_UNIQUENESS_REPLACE_PATTERN = RegExp.compile("[^a-z0-9]", "gi");

	public int scrollTop = -1;

	@Override
	public String getHostPageBaseURL() {
		return GWT.getHostPageBaseURL();
	}

	@Override
	public String getModuleBaseURL() {
		return GWTWrapperImpl.getRealGWTModuleBaseURL();
	}

	@Override
	public void assignThisWindowWith(String url) {
		Window.Location.assign(url);
	}

	@Override
	public String encode(String decodedURL) {
		return URL.encode(decodedURL);
	}

	@Override
	public String encodeQueryString(String queryString) {
		if (queryString == null)
			return "";

		return URL.encodeQueryString(queryString);
	}

	@Override
	public String decodeQueryString(String queryString) {
		if (queryString == null)
			return "";

		return URL.decodeQueryString(queryString);
	}


	@Override
	public XMLHttpRequest createXMLHttpRequest() {
		return XMLHttpRequest.create();
	}

	@Override
	public NumberFormat getNumberFormat(String pattern) {
		return NumberFormat.getFormat(pattern);
	}

	@Override
	public String getHostPrefix() {
		return com.google.gwt.user.client.Window.Location.getProtocol() + "//" + com.google.gwt.user.client.Window.Location.getHost();
	}

	@Override
	public String getCurrentURL() {
		return Window.Location.getHref();
	}

	@Override
	public DateTimeFormat getDateTimeFormat(PredefinedFormat format) {
		return DateTimeFormat.getFormat(format);
	}

	@Override
	public void scheduleExecution(final Callback callback, int delayMillis) {
		Timer timer = new Timer() {
			public void run() {
				callback.invoke();
			}
		};
		timer.schedule(delayMillis);
	}

	@Override
	public void scheduleFixedDelay(final Callback callback, int delayMs) {
		Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
			@Override
			public boolean execute() {
				callback.invoke();
				return true;
			}
		}, delayMs);

	}

	@Override
	public void scheduleDeferred(final Callback callback) {
		Scheduler.get().scheduleDeferred(new Command() {
			@Override
			public void execute() {
				callback.invoke();
			}
		});
	}

	@Override
	public String getUserAgent() {
		return Navigator.getUserAgent();
	}

	@Override
	public String getAppVersion() {
		return Navigator.getAppVersion();
	}

	@Override
	public int nextRandomInt() {
		return Random.nextInt();
	}

	@Override
	public void addDaysToDate(Date date, int days) {
		CalendarUtil.addDaysToDate(date, days);
	}

	@Override
	public boolean isWhitespace(String text) {
		return PATTERN_WHITE_SPACE.test(text);
	}

	@Override
	public void newItem(String historyToken, boolean issueEvent) {
		History.newItem(historyToken, issueEvent);
	}

	@Override
	public void replaceItem(String historyToken, boolean issueEvent) {
		History.replaceItem(historyToken, issueEvent);
	}

	@Override
	public String getCurrentHistoryToken() {
		return History.getToken();
	}

	@Override
	public ServiceDefTarget asServiceDefTarget(Object serviceAsync) {
		return (ServiceDefTarget) serviceAsync;
	}

	@Override
	public HasRpcToken asHasRpcToken(Object service) {
		return (HasRpcToken) service;
	}

	@Override
	public String getUniqueElementId() {
		return HTMLPanel.createUniqueId();
	}

	@Override
	public void saveWindowPosition() {
		scrollTop = Window.getScrollTop();
	}

	@Override
	public void restoreWindowPosition() {
		if (scrollTop >= 0) {
			Window.scrollTo(0, scrollTop);
			scrollTop = -1;
		}
	}

	@Override
	public int nextInt(int upperBound) {
		return Random.nextInt(upperBound);
	}


	/**
	 * Get the string that will be used for a uniqueness check for alias names. Only lower case letters
	 * and numbers contribute to the uniqueness of a principal name. All other characters (-,., ,_) are
	 * ignored.
	 * 
	 * @param inputName
	 * @return
	 */
	@Override
	public String getUniqueAliasName(String inputName) {
		if (inputName == null) {
			throw new IllegalArgumentException("Name cannot be null");
		}
		// Only letters and numbers contribute to the uniqueness.
		// Replace all non-letters and numbers with empty strings
		return PRINICPAL_UNIQUENESS_REPLACE_PATTERN.replace(inputName, "");
	}

	public static native String getHostpageUrl()/*-{
		return $wnd.location.protocol + "//" + $wnd.location.host + "/";
	}-*/;

	public static String getRealGWTModuleBaseURL() {
		return getHostpageUrl() + GWT.getModuleName() + "/";
	}

	@Override
	public String getFriendlySize(double size, boolean abbreviatedUnits) {
		return DisplayUtils.getFriendlySize(size, abbreviatedUnits);
	}

	@Override
	public DateTimeFormat getFormat(String formatPattern) {
		return DateTimeFormat.getFormat(formatPattern);
	}

	@Override
	public DateTimeFormat getFormat(PredefinedFormat predefinedFormat) {
		return DateTimeFormat.getFormat(predefinedFormat);
	}
}
