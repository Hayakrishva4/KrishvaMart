package com.krishva.krishvamart.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.krishva.krishvamart.dto.ApiResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import javax.servlet.http.HttpServletResponse;

/** Central Gson instance and small helpers for writing the fixed API response envelope. */
public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                @Override
                public void write(JsonWriter out, LocalDateTime value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(value.toString());
                    }
                }

                @Override
                public LocalDateTime read(JsonReader in) throws IOException {
                    return LocalDateTime.parse(in.nextString());
                }
            })
            .create();

    private JsonUtil() {
    }

    public static Gson gson() {
        return GSON;
    }

    public static void writeSuccess(HttpServletResponse resp, int status, Object data) throws IOException {
        write(resp, status, ApiResponse.success(data));
    }

    public static void writeError(HttpServletResponse resp, int status, String code, String message) throws IOException {
        write(resp, status, ApiResponse.error(code, message));
    }

    private static void write(HttpServletResponse resp, int status, ApiResponse<?> body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(GSON.toJson(body));
    }
}
