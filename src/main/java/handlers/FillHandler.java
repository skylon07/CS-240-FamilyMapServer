package handlers;

import com.sun.net.httpserver.*;

import services.FillService;
import services.requests.FillRequest;
import services.responses.FillResponse;

public class FillHandler extends GenericHandler<FillRequest, FillResponse, FillService> {
    @Override
    protected FillRequest parseRequest(HttpExchange exchange) {
        return null; // TODO
    }

    @Override
    protected FillService createBoundService() {
        return new FillService();
    }
}
