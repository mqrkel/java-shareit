package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class RequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    public RequestClient(@Value("${shareit-server.url}") String serverUrl,
                         RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build());
    }

    public ResponseEntity<Object> createRequest(long userId, ItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getUserRequests(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAllRequests(long userId, int from, int size) {
        Map<String, Object> params = Map.of("from", from, "size", size);
        return get("/all?from={from}&size={size}", userId, params);
    }

    public ResponseEntity<Object> getRequestById(long userId, long requestId) {
        return get("/" + requestId, userId);
    }
}
