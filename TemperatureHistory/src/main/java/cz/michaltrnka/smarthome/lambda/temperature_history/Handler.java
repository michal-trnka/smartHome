package cz.michaltrnka.smarthome.lambda.temperature_history;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.json.JSONArray;

public class Handler implements RequestHandler<Request, String> {
    private final int DEFAULT_COUNT = 100;
    private Service service;


    /**
     * Gets temperature history.
     * <p>
     * From and To parameters are inclusive. If the count is 1 then the latest record is retrieved if the "to" is
     * specified. If the "from" is specified, the first record is retrieved. If both time elements are specified,
     * the latest record is retrieved only if it is in the range. If the count value is high, it may contain
     * duplicates. Too high count will not result in all records as paging to the DB is not implemented.
     *
     * @param input Input to get temperatures from to date (inclusive), count of records to get.
     * @return JSON array of found temperature records
     */
    public String handleRequest(Request input, Context context) {
        JSONArray ar = new JSONArray();
        System.out.println(ar.toString());
        ar = new JSONArray();
        ar.put("");
        System.out.println(ar.toString());

        service = new Service();
        sanitizeInputs(input);

        if(input.getCount()==1){
            if(input.getFrom()==0 && input.getTo()==0){
                return service.getLatestTemperature();
            }
            if(input.getFrom()==0){
                return service.getLatestTemperatureBefore(input.getTo());
            }
            if(input.getTo()==0){
                return service.getEarliestTemperatureAfter(input.getFrom());
            }
            return service.getLatestTemperatureBetween(input.getTo(), input.getFrom());
        }

        return service.getTemperaturesBetween(input.getFrom(), input.getTo(), input.getCount());
    }

    private void sanitizeInputs(Request input){
        if(input.getCount()==0){
            input.setCount(DEFAULT_COUNT);
        }

        if(input.getTo() < input.getFrom()){
            long temp = input.getTo();
            input.setTo(input.getFrom());
            input.setFrom(temp);
        }
    }
}
