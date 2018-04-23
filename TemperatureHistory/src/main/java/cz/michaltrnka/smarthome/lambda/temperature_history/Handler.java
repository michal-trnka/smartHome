package cz.michaltrnka.smarthome.lambda.temperature_history;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<Object, String> {
    private final String DYNAMODB_TABLE_NAME = "temperature";

    public String handleRequest(Object input, Context context) {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        client.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
        DynamoDB dynamoDb = new DynamoDB(client);

        //QuerySpec querySpec = new QuerySpec().
        //        withKeyConditionExpression();

        //dynamoDb.getTable(DYNAMODB_TABLE_NAME).ba;
        ScanRequest scanRequest = new ScanRequest().withTableName(DYNAMODB_TABLE_NAME);
        ScanResult scanResult = client.scan(scanRequest);

        context.getLogger().log("Input: " + input);
        String output = "Hello, " + input + "! + \n Lamba result: "+ scanResult.getCount();
        return output;
    }

}
