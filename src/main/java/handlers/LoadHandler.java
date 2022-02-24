package handlers;

import com.sun.net.httpserver.*;

import services.LoadService;
import services.requests.LoadRequest;
import services.responses.LoadResponse;

public class LoadHandler extends GenericHandler<LoadRequest, LoadResponse, LoadService> {
    @Override
    protected LoadRequest parseRequest(HttpExchange exchange) {
        return null; // TODO
    }

    @Override
    protected LoadService createBoundService() {
        return new LoadService();
    }
}
