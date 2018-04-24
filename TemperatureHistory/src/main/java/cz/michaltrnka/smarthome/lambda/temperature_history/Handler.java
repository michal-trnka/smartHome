package cz.michaltrnka.smarthome.lambda.temperature_history;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.json.JSONArray;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Handler implements RequestHandler<TemperatureRequest, String> {
    private final String DYNAMODB_TABLE_NAME = "temperature";
    private final String SENSOR_ID = "temperatureSensor";
    private final String VALUE_FIELD_NAME = "value";
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
        for (int i = 0; i < times.length; i++) {
            json.put(getResultForTime(times[i]));
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

    public String getResultForTime(long time) {
        Map<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("#t", "timestamp");
        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("sensor_id = :id and #t < :time")
                .withNameMap(nameMap)
                .withValueMap(new ValueMap()
                        .withString(":id", SENSOR_ID)
                        .withLong(":time", time))
                .withMaxResultSize(1);

        ItemCollection<QueryOutcome> items = table.query(querySpec);
        //get the closest higher result
        if (items.getAccumulatedItemCount() == 0) {
            querySpec = new QuerySpec()
                    .withKeyConditionExpression("sensor_id = :id and #t > :time")
                    .withNameMap(nameMap)
                    .withValueMap(new ValueMap()
                            .withString(":id", SENSOR_ID)
                            .withLong(":time", time))
                    .withMaxResultSize(1);

            items = table.query(querySpec);
        }

        return items.iterator().next().getJSONPretty(VALUE_FIELD_NAME);
    }
}
