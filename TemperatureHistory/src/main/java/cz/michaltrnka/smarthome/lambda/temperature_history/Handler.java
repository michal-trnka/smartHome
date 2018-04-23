package cz.michaltrnka.smarthome.lambda.temperature_history;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<Object, String> {
    private final String DYNAMODB_TABLE_NAME = "temperature";

    public String handleRequest(Object input, Context context) {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();

        DynamoDB dynamoDb = new DynamoDB(client);

        Item i = dynamoDb.getTable(DYNAMODB_TABLE_NAME).getItem("sensor_id", "temperatureSensor", "timestamp", 1521757111420l);


        context.getLogger().log("Input: " + input);
        String output = "Hello, " + input + "! + \n Lambda result: \n" + i.toJSONPretty();
        return output;
    }

}
