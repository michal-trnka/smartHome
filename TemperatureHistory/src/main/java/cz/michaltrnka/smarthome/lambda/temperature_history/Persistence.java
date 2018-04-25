package cz.michaltrnka.smarthome.lambda.temperature_history;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Persistence {
    private final String DYNAMODB_TABLE_NAME = "temperature";
    private final String SENSOR_ID = "temperatureSensor";
    private final String VALUE_FIELD_NAME = "value";
    private final String SORT_TIMESTAMP_NAME = "timestamp";
    private final String SENSOR_ID_FIELD_NAME = "sensor_id";

    private Table table;

    public Persistence(){
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDb = new DynamoDB(client);
        table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);
    }

    /**
     * Returns temperature record for the given time
     *
     * @param time time for which the record is retrieved
     * @return record in JSON format
     */
    public String getResultForTime(long time) throws NoResultException {
        Map<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("#s", SENSOR_ID_FIELD_NAME);
        nameMap.put("#t", SORT_TIMESTAMP_NAME);
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
            System.out.print(items.getAccumulatedItemCount());
            throw new NoResultException();
        }
        return items.iterator().next().getJSON(VALUE_FIELD_NAME);
    }

    /**
     * Get earliest entry timestamp after time
     *
     * @param startTime earliest entry after that time
     * @return timestamp of the earliest entry
     */
    public long getEarliestEntryTime(long startTime) throws NoResultException {
        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("#s = :id and #t >= :time")
                .withValueMap(new ValueMap()
                        .withString(":id", SENSOR_ID)
                        .withLong(":time", startTime));
        return finishQueryAndReturnTime(querySpec);
    }

    /**
     * Get latest entry timestamp before time
     *
     * @param endTime earliest entry before that time
     * @return timestamp of the earliest entry
     */
    public long getLatestEntryTime(long endTime) throws NoResultException {
        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("#s = :id and #t <= :time")
                .withValueMap(new ValueMap()
                        .withString(":id", SENSOR_ID)
                        .withLong(":time", endTime))
                .withScanIndexForward(false);
        return finishQueryAndReturnTime(querySpec);
    }

    private long finishQueryAndReturnTime(QuerySpec querySpec) throws NoResultException {
        Map<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("#s", SENSOR_ID_FIELD_NAME);
        nameMap.put("#v", VALUE_FIELD_NAME);
        nameMap.put("#t", SORT_TIMESTAMP_NAME);

        querySpec.withNameMap(nameMap)
                .withMaxResultSize(1)
                .withProjectionExpression("#v");
        ItemCollection<QueryOutcome> items = table.query(querySpec);
        if(!items.iterator().hasNext()){
            throw new NoResultException();
        }
        Map map = (Map) items.iterator().next().get("value");
        BigDecimal time = (BigDecimal) map.get("time");
        return time.longValue();
    }
}
