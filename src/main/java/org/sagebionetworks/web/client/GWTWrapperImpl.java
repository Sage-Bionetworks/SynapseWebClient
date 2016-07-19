package org.sagebionetworks.web.client;

import java.util.Date;

import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class GWTWrapperImpl implements GWTWrapper {

	private final static RegExp PATTERN_WHITE_SPACE = RegExp.compile("^\\s+$");
	
	@Override
	public String getHostPageBaseURL() {
		return GWT.getHostPageBaseURL();
	}

	@Override
	public String getModuleBaseURL() {
		return GWT.getModuleBaseURL();
	}
	
	@Override
	public void assignThisWindowWith(String url){
		 Window.Location.assign(url);
	}
	
	@Override
	public String encodeQueryString(String queryString){
		if (queryString == null)
			return "";
		
		return URL.encodeQueryString(queryString);
	}
	
	@Override
	public String decodeQueryString(String queryString){
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
		return com.google.gwt.user.client.Window.Location.getProtocol()+"//"+com.google.gwt.user.client.Window.Location.getHost();
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
	public String getFormattedDateString(Date date) {
		return DisplayUtils.convertDataToPrettyString(date);
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
}
