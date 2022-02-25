package handlers;

import java.net.HttpURLConnection;

import com.sun.net.httpserver.*;

import services.FileService;
import services.requests.FileRequest;
import services.responses.FileResponse;

public class FileHandler extends GenericHandler<FileRequest, FileResponse, FileService> {
    @Override
    protected FileRequest parseRequest(HttpExchange exchange) {
        FileRequest request = new FileRequest();
        request.path = exchange.getRequestURI().toString();
        return request;
    }

    @Override
    protected FileService createBoundService() {
        return new FileService();
    }

    @Override
    protected int getStatusCode(FileResponse response) {
        if (response.success) {
            return HttpURLConnection.HTTP_OK;
        } else {
            return HttpURLConnection.HTTP_NOT_FOUND;
        }
    }

    @Override
    protected String convertResponse(FileResponse response) {
        return response.data;
    }
}
