package cz.michaltrnka.smarthome.lambda.temperature_history;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<TemperatureRequest, String> {
    private final String DYNAMODB_TABLE_NAME = "temperature";

    /**
     * Gets temperature history.
     *
     * From and To parameters are inclusive,
     *
     * @param input Input to get temperatures from to date, including
     * @param context
     * @return
     */
    public String handleRequest(TemperatureRequest input, Context context) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        DynamoDB dynamoDb = new DynamoDB(client);

        Item i = dynamoDb.getTable(DYNAMODB_TABLE_NAME).getItem("sensor_id", "temperatureSensor", "timestamp", 1521757111420l);


        context.getLogger().log("Input: " + input);
        String output = "Hello, " + input + "! + \n Input: \n" + input.getFrom().toString();
        return output;
    }

}
