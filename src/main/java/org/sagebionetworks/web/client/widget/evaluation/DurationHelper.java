package org.sagebionetworks.web.client.widget.evaluation;

public class DurationHelper {
	public static final int SECOND_MS = 1000;
	public static final int MINUTE_MS = 60 * SECOND_MS;
	public static final int HOUR_MS = 60 * MINUTE_MS;
	public static final int DAY_MS = 24 * HOUR_MS;
	private Integer days, hours, minutes, seconds, ms;
	Long durationMs = null;
	
	public DurationHelper(Long durationMs) {
		this.durationMs = durationMs;
		if (durationMs != null) {
			int leftOver = durationMs.intValue();
			days = Math.floorDiv(leftOver, DAY_MS);
			leftOver -= days*DAY_MS;
			hours = Math.floorDiv(leftOver, HOUR_MS);
			leftOver -= hours*HOUR_MS;
			minutes = Math.floorDiv(leftOver, MINUTE_MS);
			leftOver -= minutes*MINUTE_MS;
			seconds = Math.floorDiv(leftOver, SECOND_MS);
			leftOver -= seconds*SECOND_MS;
			ms = leftOver;
		}
	}
	
	public DurationHelper(Double secondsDouble, Double minutesDouble, Double hoursDouble, Double daysDouble) {
		int duration = 0; 
		if (daysDouble != null) {
			duration += daysDouble.intValue() * DAY_MS;
		}
		if (hoursDouble != null) {
			duration += hoursDouble.intValue() * HOUR_MS;
		}
		if (minutesDouble != null) {
			duration += minutesDouble.intValue() * MINUTE_MS;
		}
		if (secondsDouble != null) {
			duration += secondsDouble.intValue() * SECOND_MS;
		}
		if (duration > 0) {
			this.durationMs = new Long(duration);	
		}
	}
	
	public Integer getDays() {
		return days;
	}
	public Integer getHours() {
		return hours;
	}
	public Integer getMinutes() {
		return minutes;
	}
	public Integer getSeconds() {
		return seconds;
	}
	public Long getDurationMs() {
		return durationMs;
	}
}