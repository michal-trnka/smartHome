package cz.michaltrnka.smarthome.lambda.temperature_history;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<Object, String> {

    public String handleRequest(Object input, Context context) {
        context.getLogger().log("Input: " + input);
        String output = "Hello, " + input + "!";
        return output;
    }

}
