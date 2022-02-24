package handlers;

import services.LoadService;
import services.requests.LoadRequest;
import services.responses.LoadResponse;

public class LoadHandler extends GenericHandler<LoadRequest, LoadResponse, LoadService> {
    @Override
    protected Class<LoadRequest> getBoundRequestClass() {
        return LoadRequest.class;
    }

    @Override
    protected LoadService createBoundService() {
        return new LoadService();
    }
}
