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

    @Override
    protected int getStatusCode(LoadResponse response) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected String convertResponse(LoadResponse response) {
        // TODO Auto-generated method stub
        return null;
    }
}
