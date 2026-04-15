package sickbay.pokenamon.system.home;

import android.content.Context;

import com.lyft.kronos.AndroidClockFactory;
import com.lyft.kronos.KronosClock;

public class TimeManager {
    private static TimeManager instance;
    private static KronosClock clock;

    public static synchronized TimeManager getInstance(Context context) {
        if (instance == null) {
            instance = new TimeManager();
            clock = AndroidClockFactory.createKronosClock(context);
            clock.sync();
        }

        return instance;
    }

    public long getCurrentTimeInMs() {
        return clock.getCurrentTimeMs();
    }
}
