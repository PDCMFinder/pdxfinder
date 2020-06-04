package org.pdxfinder.services.graphqlclient;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;

import java.io.IOException;

public class OkHttpRequest {


    private OkHttpRequest() {
    }

    public static String client(String url, String request) throws IOException {

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/graphql");
        RequestBody body = RequestBody.create(mediaType, request);
        Request httpRequest = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        return client.newCall(httpRequest).execute().body().string();
    }


}
