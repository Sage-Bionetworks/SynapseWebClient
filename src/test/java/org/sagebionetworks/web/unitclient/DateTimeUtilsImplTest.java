package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.sagebionetworks.web.client.DateTimeUtilsImpl.*;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.web.client.DateTimeUtilsImpl;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.Moment;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;

@RunWith(MockitoJUnitRunner.class)
public class DateTimeUtilsImplTest {
	
	@Mock
	Moment mockMoment;
	@Mock
	GWTWrapper mockGWT;
	@Mock
	DateTimeFormat mockDateOnlyFormat;
	@Mock
	DateTimeFormat mockDateOnlyFormatUTC;
	@Mock
	DateTimeFormat mockSmallDateFormat;
	@Mock
	DateTimeFormat mockSmallDateFormatUTC;
	@Mock
	DateTimeFormat mockLongDateFormat;
	@Mock
	DateTimeFormat mockLongDateFormatUTC;
	@Mock
	DateTimeFormat mockISO8601Format;
	@Mock
	DateTimeFormat mockYearFormat;
	
	DateTimeUtilsImpl dateTimeUtils;
	
	@Before
	public void setup(){
		when(mockGWT.getFormat(DATE_ONLY_FORMAT_STRING)).thenReturn(mockDateOnlyFormat);
		when(mockGWT.getFormat(DATE_ONLY_FORMAT_STRING + UTC)).thenReturn(mockDateOnlyFormatUTC);
		when(mockGWT.getFormat(SMALL_DATE_FORMAT_STRING)).thenReturn(mockSmallDateFormat);
		when(mockGWT.getFormat(SMALL_DATE_FORMAT_STRING + UTC)).thenReturn(mockSmallDateFormatUTC);
		when(mockGWT.getFormat(LONG_DATE_FORMAT_STRING)).thenReturn(mockLongDateFormat);
		when(mockGWT.getFormat(LONG_DATE_FORMAT_STRING + UTC)).thenReturn(mockLongDateFormatUTC);
		when(mockGWT.getFormat(PredefinedFormat.ISO_8601)).thenReturn(mockISO8601Format);
		when(mockGWT.getFormat(YEAR_ONLY_FORMAT_STRING)).thenReturn(mockYearFormat);
		dateTimeUtils = new DateTimeUtilsImpl(mockMoment, mockGWT);
	}	

	@Test
	public void testGetDateString() {
		Date d = new Date();
		dateTimeUtils.getDateString(d);
		verify(mockDateOnlyFormat).format(d);
		
		dateTimeUtils.setShowUTCTime(true);
		dateTimeUtils.getDateString(d);
		verify(mockDateOnlyFormatUTC).format(d);
		
		dateTimeUtils.setShowUTCTime(false);
		dateTimeUtils.getDateString(d);
		verify(mockDateOnlyFormat, times(2)).format(d);
	}
	
	@Test
	public void testRelativeTime() {
		Date d = new Date();
		CalendarUtil.addDaysToDate(d, -2);
		dateTimeUtils.getRelativeTime(d);
		verifyZeroInteractions(mockMoment);
		verify(mockDateOnlyFormat).format(d);
		
		d = new Date();
		CalendarUtil.addDaysToDate(d, 2);
		dateTimeUtils.getRelativeTime(d);
		verify(mockDateOnlyFormat).format(d);
		verifyZeroInteractions(mockMoment);
		
		d = new Date();
		dateTimeUtils.getRelativeTime(d);
		verify(mockMoment).getRelativeTime(anyString());
	}
	
	@Test
	public void testFriendlyTimeEstimate() {
		assertEquals("0 s", dateTimeUtils.getFriendlyTimeEstimate(0));
		assertEquals("5 s", dateTimeUtils.getFriendlyTimeEstimate(5));
		assertEquals("1 min", dateTimeUtils.getFriendlyTimeEstimate(60));
		assertEquals("1 min 8 s", dateTimeUtils.getFriendlyTimeEstimate(60 + 8));
		assertEquals("1 h", dateTimeUtils.getFriendlyTimeEstimate(60*60));
		assertEquals("1 h 1 min", dateTimeUtils.getFriendlyTimeEstimate(60*60 + 60));
		// ignore seconds if greater than an hour
		assertEquals("1 h 1 min", dateTimeUtils.getFriendlyTimeEstimate(60*60 + 60 + 9));
	}

	@Test
	public void testGetYear() {
		Date d = new Date();
		dateTimeUtils.getYear(d);
		verify(mockYearFormat).format(d);
	}

}






