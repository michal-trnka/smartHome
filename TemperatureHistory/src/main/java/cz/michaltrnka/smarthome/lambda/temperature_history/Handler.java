package cz.michaltrnka.smarthome.lambda.temperature_history;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
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

    /**
     * Gets temperature history.
     * <p>
     * From and To parameters are inclusive,
     *
     * @param input   Input to get temperatures from to date, including
     * @return JSON array of found temperature records
     */
    public String handleRequest(TemperatureRequest input, Context context) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDb = new DynamoDB(client);

        long from = input.getFrom();
        long to = input.getTo() == 0L ? new Date().getTime() : input.getTo();
        Map<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("#t","timestamp");

        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("sensor_id = :id and #t between :from and :to")
                .withNameMap(nameMap)
                .withValueMap(new ValueMap()
                    .withString(":id",SENSOR_ID)
                    .withLong(":from", from)
                    .withLong(":to", to));

        ItemCollection<QueryOutcome> items = dynamoDb.getTable(DYNAMODB_TABLE_NAME).query(querySpec);

        JSONArray json = new JSONArray();
        for(Item item : items){
            json.put(item.getJSONPretty(VALUE_FIELD_NAME));
        }

        return json.toString();
    }

}
