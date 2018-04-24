package cz.michaltrnka.smarthome.lambda.temperature_history;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.json.JSONArray;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Handler implements RequestHandler<TemperatureRequest, String> {
    private final String DYNAMODB_TABLE_NAME = "temperature";
    private final String SENSOR_ID = "temperatureSensor";
    private final String VALUE_FIELD_NAME = "value";
    private final String SORT_TIMESTAMP_NAME = "timestamp";
    private final String SENSOR_ID_FIELD_NAME = "sensor_id";
    private final String VALUE_TIMESTAMP_FULL_NAME = "value.timestamp";
    private final int COUNT = 100;
    private Table table;

    /**
     * Gets temperature history.
     * <p>
     * From and To parameters are inclusive,
     *
     * @param input Input to get temperatures from to date, including
     * @return JSON array of found temperature records
     */
    public String handleRequest(TemperatureRequest input, Context context) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDb = new DynamoDB(client);
        table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);

        long[] times = getTimes(input.getFrom(), input.getTo(), COUNT);

        JSONArray json = new JSONArray();
        for (long time : times) {
            context.getLogger().log(time + "\n");
            try {
                json.put(getResultForTime(time));
            } catch (NoResultException e) {
                // nothing to do
            }
        }

        return json.toString();
    }

    /**
     * Get approximate times for sampling the time range for given amount of results
     *
     * @param from  time to start
     * @param to    time to finish
     * @param count count of results
     * @return approximate times for results
     */
    public long[] getTimes(long from, long to, int count) {
        if (to == 0) {
            to = new Date().getTime();
        }

        if (from == 0) {
            from = getEarliestEntry();
            System.out.print(from);
        }

        if (to < from) {
            long temp = to;
            to = from;
            from = temp;
        }
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

    public String getResultForTime(long time) throws NoResultException {
        Map<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("#t", SORT_TIMESTAMP_NAME);
        nameMap.put("#s", SENSOR_ID_FIELD_NAME);
        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("#s = :id and #t <= :time")
                .withNameMap(nameMap)
                .withValueMap(new ValueMap()
                        .withString(":id", SENSOR_ID)
                        .withLong(":time", time))
                .withMaxResultSize(1)
                .withScanIndexForward(false);

        ItemCollection<QueryOutcome> items = table.query(querySpec);
        //get the closest higher result
        if (!items.iterator().hasNext()) {
            querySpec = new QuerySpec()
                    .withKeyConditionExpression("#s = :id and #t > :time")
                    .withNameMap(nameMap)
                    .withValueMap(new ValueMap()
                            .withString(":id", SENSOR_ID)
                            .withLong(":time", time))
                    .withMaxResultSize(1);

            items = table.query(querySpec);
        }
        if (!items.iterator().hasNext()) {
            throw new NoResultException();
        }
        return items.iterator().next().getJSON(VALUE_FIELD_NAME);
    }

    private long getEarliestEntry() {
        Map<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("#s", SENSOR_ID_FIELD_NAME);
        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("#s = :id")
                .withNameMap(nameMap)
                .withValueMap(new ValueMap()
                        .withString(":id", SENSOR_ID))
                .withMaxResultSize(1)
                .withAttributesToGet(VALUE_TIMESTAMP_FULL_NAME);
        System.out.print(table.query(querySpec).iterator().next().toString()+"\n");
        return Long.parseLong(table.query(querySpec).iterator().next().toString());
    }
}
