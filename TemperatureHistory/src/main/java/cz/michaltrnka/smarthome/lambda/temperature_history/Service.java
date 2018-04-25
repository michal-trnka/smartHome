package cz.michaltrnka.smarthome.lambda.temperature_history;

import org.json.JSONArray;

import java.util.Date;

public class Service {
    private Persistence persistence;
    private final String EMPTY_JSON_ARRAY = "[]";

    public Service() {
        persistence = new Persistence();
    }

    /**
     * Get latest temperature record
     */
    public String getLatestTemperature() {
        return getLatestTemperatureBefore(new Date().getTime());
    }

    /**
     * Get latest temperature record before given time
     */
    public String getLatestTemperatureBefore(Long before) {
        try {
            Long time = persistence.getLatestEntryTime(before);
            String result = persistence.getResultForTime(time);
            return encapsulateToJSONArray(result);
        } catch (NoResultException e) {
            e.printStackTrace();
        }
        return EMPTY_JSON_ARRAY;
    }

    /**
     * Get latest temperature record if it exists in given time range
     */
    public String getLatestTemperatureBetween(Long before, Long after) {
        try {
            Long time = persistence.getLatestEntryTime(before);
            if (time < after) {
                return EMPTY_JSON_ARRAY;
            }
            String result = persistence.getResultForTime(time);
            return encapsulateToJSONArray(result);
        } catch (NoResultException e) {
            e.printStackTrace();
        }
        return EMPTY_JSON_ARRAY;
    }

    /**
     * Get earliest temperature record after time
     */
    public String getEarliestTemperatureAfter(Long after) {
        try {
            Long time = persistence.getEarliestEntryTime(after);
            String result = persistence.getResultForTime(time);
            return encapsulateToJSONArray(result);
        } catch (NoResultException e) {
            e.printStackTrace();
        }
        return EMPTY_JSON_ARRAY;
    }

    /**
     * Returns temperature records between times and capped to the amount of records given by count.
     *
     * May contain duplicates!
     */
    public String getTemperaturesBetween(Long from, Long to, int count) {
        if (to == 0) {
            to = new Date().getTime();
        }

        long[] times;

        try {
            times = getTimes(from, to, count);
        } catch (NoResultException e) {
            return EMPTY_JSON_ARRAY;
        }

        JSONArray json = new JSONArray();
        for (long time : times) {
            try {
                json.put(persistence.getResultForTime(time));
            } catch (NoResultException e) {
                // nothing to do
            }
        }

        return json.toString();
    }

    private long[] getTimes(long from, long to, int count) throws NoResultException {

        from = persistence.getEarliestEntryTime(from);
        to = persistence.getLatestEntryTime(to);

        long[] times = new long[count];
        times[0] = from;
        times[count - 1] = to;

        if (count <= 2) {
            return times;
        }

        long period = (to - from) / (count - 1);

        for (int i = 1; i < count; i++) {
            times[i] = from + i * period;
        }

        return times;
    }

    private String encapsulateToJSONArray(String json) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(json);
        return jsonArray.toString();
    }
}
