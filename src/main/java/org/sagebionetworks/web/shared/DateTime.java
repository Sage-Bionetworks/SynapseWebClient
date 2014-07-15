package org.sagebionetworks.web.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DateTime implements IsSerializable {

    private String displayTime;

    private int hour;
    private int minute;
    private int second;
    private boolean expired;

    public String getDisplayTime() {
        return displayTime;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecond() {
        return second;
    }

    public boolean isExpired() {
        return expired;
    }

    // This is necessary for GWT to be able to serialize this class.
    private DateTime() {
        this.displayTime = "";
        this.hour = 1;
        this.minute = 0;
        this.second = 0;
        this.expired = true;
    }

    public DateTime(String displayTime,
                    int hour, int minute, int second) {
        this.displayTime = displayTime;

        if (1 <= hour && hour <= 12)
            this.hour = hour;
        else
            throw new IllegalArgumentException("Bad hour.");

        if (0 <= minute && minute <= 59)
            this.minute = minute;
        else
            throw new IllegalArgumentException("Bad minute.");

        if (0 <= second && second <= 59)
            this.second = second;
        else
            throw new IllegalArgumentException("Bad second.");

        this.expired = false;
    }

    /**
     *  Increment this time by one second.  This properly rolls over
     *  hours and minutes, but doesn't track AM/PM and days, but when
     *  the hour rolls over from 12 to 1, it sets it's expired flag to
     *  true.
     */
    public DateTime tick() {
        this.second++;

        if (this.second > 59) {
            this.minute++;
            this.second = 0;
        }

        if (this.minute > 59) {
            this.hour++;
            this.minute = 0;
        }

        if (this.hour > 12) {
            this.hour = 1;
            this.expired = true;
        }

        return this;
    }
}
