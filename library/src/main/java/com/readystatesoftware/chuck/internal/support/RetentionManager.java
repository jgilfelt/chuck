package com.readystatesoftware.chuck.internal.support;

import com.readystatesoftware.chuck.ChuckInterceptor;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RetentionManager {

    private static long getThreshold(ChuckInterceptor.Period period) {
        Date now = new Date();
        return now.getTime() - toMillis(period);
    }

    private static long toMillis(ChuckInterceptor.Period period) {
        switch (period) {
            case OneHour:
                return TimeUnit.HOURS.toMillis(1);
            case OneDay:
                return TimeUnit.DAYS.toMillis(1);
            case OneWeek:
                return TimeUnit.DAYS.toMillis(7);
            default:
                return 0;
        }
    }
}
