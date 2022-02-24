package handlers;

import services.FillService;
import services.requests.FillRequest;
import services.responses.FillResponse;

public class FillHandler extends GenericHandler<FillRequest, FillResponse, FillService> {
    @Override
    protected Class<FillRequest> getBoundRequestClass() {
        return FillRequest.class;
    }

    @Override
    protected FillService createBoundService() {
        return new FillService();
    }
}
