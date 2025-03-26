package org.example.fashion_web.backend.services.servicesimpl;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.fashion_web.backend.configurations.QdrantConfig;
import java.io.IOException;

@Service
public class QdrantService {
    private final OkHttpClient client = new OkHttpClient();

    @Autowired
    private QdrantConfig qdrantConfig;

    // Kiểm tra trạng thái Qdrant
    public String getQdrantHealth() throws IOException {
        Request request = new Request.Builder()
                .url(qdrantConfig.getQdrantUrl() + "/health")
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    // Thêm dữ liệu vào Qdrant
    public String insertVector(String collectionName, int id, double[] vector) throws IOException {
        StringBuilder vectorJson = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            vectorJson.append(vector[i]);
            if (i < vector.length - 1) vectorJson.append(", ");
        }
        vectorJson.append("]");

        String jsonBody = "{"
                + "\"points\": [{"
                + "\"id\": " + id + ","
                + "\"vector\": " + vectorJson
                + "}]"
                + "}";

        RequestBody body = RequestBody.create(
                jsonBody, MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(qdrantConfig.getQdrantUrl() + "/collections/" + collectionName + "/points")
                .put(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
