package handlers;

import com.sun.net.httpserver.*;

import services.FileService;
import services.requests.FileRequest;
import services.responses.FileResponse;

public class FileHandler extends GenericHandler<FileRequest, FileResponse, FileService> {
    @Override
    protected FileRequest parseRequest(HttpExchange exchange) {
        return null; // TODO
    }

    @Override
    protected FileService createBoundService() {
        return new FileService();
    }
}
