package org.sagebionetworks.web.client.widget.evaluation;

public class DurationHelper {

  public static final long SECOND_MS = 1000;
  public static final long MINUTE_MS = 60 * SECOND_MS;
  public static final long HOUR_MS = 60 * MINUTE_MS;
  public static final long DAY_MS = 24 * HOUR_MS;
  private Long days, hours, minutes, seconds, ms;
  Long durationMs = null;

  public DurationHelper(Long durationMs) {
    this.durationMs = durationMs;
    if (durationMs != null) {
      Long leftOver = durationMs.longValue();
      days = Math.floorDiv(leftOver, DAY_MS);
      leftOver -= days * DAY_MS;
      hours = Math.floorDiv(leftOver, HOUR_MS);
      leftOver -= hours * HOUR_MS;
      minutes = Math.floorDiv(leftOver, MINUTE_MS);
      leftOver -= minutes * MINUTE_MS;
      seconds = Math.floorDiv(leftOver, SECOND_MS);
      leftOver -= seconds * SECOND_MS;
      ms = leftOver;
    }
  }

  public DurationHelper(
    Double secondsDouble,
    Double minutesDouble,
    Double hoursDouble,
    Double daysDouble
  ) {
    long duration = 0;
    if (daysDouble != null) {
      duration += daysDouble * DAY_MS;
    }
    if (hoursDouble != null) {
      duration += hoursDouble * HOUR_MS;
    }
    if (minutesDouble != null) {
      duration += minutesDouble * MINUTE_MS;
    }
    if (secondsDouble != null) {
      duration += secondsDouble * SECOND_MS;
    }
    if (duration > 0) {
      this.durationMs = duration;
    }
  }

  public Long getDays() {
    return days;
  }

  public Long getHours() {
    return hours;
  }

  public Long getMinutes() {
    return minutes;
  }

  public Long getSeconds() {
    return seconds;
  }

  public Long getDurationMs() {
    return durationMs;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (days != null && days > 0) {
      sb.append(days + " d ");
    }
    if (hours != null && hours > 0) {
      sb.append(hours + " h ");
    }
    if (minutes != null && minutes > 0) {
      sb.append(minutes + " min ");
    }
    if (seconds != null && (seconds > 0 || sb.toString().isEmpty())) {
      sb.append(seconds + " s");
    }
    return sb.toString();
  }
}
