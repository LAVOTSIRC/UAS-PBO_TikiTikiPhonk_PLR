package com.plr.frontend.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.plr.frontend.dto.TaskClientDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * HTTP client singleton untuk berkomunikasi dengan Spring Boot REST API.
 */
public class ApiClient {

    // Eager initialization — thread-safe tanpa synchronized
    private static final ApiClient instance = new ApiClient();
    private static final String BASE_URL = "http://localhost:8080";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(
            com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );
    }

    public static ApiClient getInstance() {
        return instance;
    }

    // Helper: add Authorization header only when user is logged in to avoid sending
    // malformed "Bearer null" headers which cause 403 responses from the API.
    private HttpRequest.Builder withAuth(HttpRequest.Builder builder) {
        if (com.plr.frontend.util.SessionManager.getInstance().isLoggedIn()) {
            return builder.header("Authorization", com.plr.frontend.util.SessionManager.getInstance().getAuthorizationHeader());
        }
        return builder;
    }

    // ========== GENERIC AUTH HELPERS ==========

    public static String get(String path) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .GET();
        builder = instance.withAuth(builder);
        HttpRequest request = builder.build();

        HttpResponse<String> response = instance.httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        throw new Exception("Request GET gagal (" + response.statusCode() + "): " + response.body());
    }

    public static String put(String path, String jsonBody) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
        builder = instance.withAuth(builder);
        HttpRequest request = builder.build();

        HttpResponse<String> response = instance.httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        throw new Exception("Request PUT gagal (" + response.statusCode() + "): " + response.body());
    }

    public static String delete(String path, String jsonBody) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + path))
            .header("Content-Type", "application/json")
            .method("DELETE", HttpRequest.BodyPublishers.ofString(jsonBody));
        builder = instance.withAuth(builder);
        HttpRequest request = builder.build();

        HttpResponse<String> response = instance.httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        throw new Exception("Request DELETE gagal (" + response.statusCode() + "): " + response.body());
    }

    // ========== AUTH ENDPOINTS ==========

    public Map<String, Object> login(String username, String password) throws Exception {
        Map<String, String> body = Map.of("username", username, "password", password);
        String json = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
        } else {
            throw new Exception("Login gagal: " + response.body());
        }
    }

    public Map<String, Object> register(String username, String email,
                                        String password) throws Exception {
        Map<String, String> body = Map.of(
            "username", username,
            "email", email,
            "password", password
        );
        String json = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/auth/register"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
        } else {
            throw new Exception("Registrasi gagal: " + response.body());
        }
    }

    // ========== TASK ENDPOINTS ==========

    public List<TaskClientDto> getTasks() throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/tasks"));
        rb = withAuth(rb);
        HttpRequest request = rb.GET().build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<List<TaskClientDto>>() {});
        } else {
            throw new Exception("Gagal mengambil tugas: " + response.statusCode());
        }
    }

    public TaskClientDto createTask(TaskClientDto taskData) throws Exception {
        String json = objectMapper.writeValueAsString(taskData);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/tasks"))
            .header("Content-Type", "application/json");
        rb = withAuth(rb);
        HttpRequest request = rb.POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(),
                new TypeReference<TaskClientDto>() {});
        } else {
            throw new Exception("Gagal membuat tugas: " + response.body());
        }
    }

    public TaskClientDto updateTask(Long id, TaskClientDto taskData) throws Exception {
        String json = objectMapper.writeValueAsString(taskData);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/tasks/" + id))
            .header("Content-Type", "application/json");
        rb = withAuth(rb);
        HttpRequest request = rb.PUT(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<TaskClientDto>() {});
        } else {
            throw new Exception("Gagal memperbarui tugas: " + response.body());
        }
    }

    public void deleteTask(Long id) throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/tasks/" + id));
        rb = withAuth(rb);
        HttpRequest request = rb.DELETE().build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new Exception("Gagal menghapus tugas: " + response.statusCode());
        }
    }

    // ========== POMODORO ENDPOINTS ==========

    public List<Map<String, Object>> getPomodoroSessions() throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/pomodoro/sessions"));
        rb = withAuth(rb);
        HttpRequest request = rb.GET().build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<List<Map<String, Object>>>() {});
        } else {
            throw new Exception("Gagal mengambil sesi: " + response.statusCode());
        }
    }

    public List<Map<String, Object>> getPomodoroSessionsByTask(Long taskId) throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/pomodoro/sessions/task/" + taskId));
        rb = withAuth(rb);
        HttpRequest request = rb.GET().build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<List<Map<String, Object>>>() {});
        } else {
            throw new Exception("Gagal mengambil sesi tugas: " + response.statusCode());
        }
    }

    public Map<String, Object> logPomodoroSession(Map<String, Object> sessionData) throws Exception {
        String json = objectMapper.writeValueAsString(sessionData);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/pomodoro/sessions"))
            .header("Content-Type", "application/json");
        rb = withAuth(rb);
        HttpRequest request = rb.POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
        } else {
            throw new Exception("Gagal menyimpan sesi: " + response.body());
        }
    }

    // ========== STATS ENDPOINTS ==========

    public Map<String, Object> getStats() throws Exception {
        SessionManager sm = SessionManager.getInstance();
        if (!sm.isLoggedIn()) {
            throw new Exception("Pengguna belum login");
        }

        String authHeader = sm.getAuthorizationHeader();
        System.out.println("[ApiClient] getStats URL: " + BASE_URL + "/api/stats");
        System.out.println("[ApiClient] getStats Auth: " + authHeader.substring(0, Math.min(30, authHeader.length())) + "...");

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/stats"))
            .header("Authorization", authHeader)
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        System.out.println("[ApiClient] getStats response: " + response.statusCode() + " body='" + response.body() + "'");

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
        } else {
            throw new Exception("Gagal mengambil statistik: " + response.statusCode()
                + " body=" + response.body());
        }
    }

    // ========== USER PROFILE ENDPOINTS ==========

    public Map<String, Object> getUserProfile() throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/users/profile"));
        rb = withAuth(rb);
        HttpRequest request = rb.GET().build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
        } else {
            throw new Exception("Gagal mengambil profil: " + response.statusCode());
        }
    }

    public Map<String, Object> updateUserProfile(String username, String email) throws Exception {
        Map<String, String> body = Map.of("username", username, "email", email);
        String json = objectMapper.writeValueAsString(body);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/users/profile"))
            .header("Content-Type", "application/json");
        rb = withAuth(rb);
        HttpRequest request = rb.PUT(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
        } else {
            throw new Exception("Gagal memperbarui profil: " + response.body());
        }
    }

    public Map<String, Object> changePassword(String oldPassword, String newPassword) throws Exception {
        Map<String, String> body = Map.of("oldPassword", oldPassword, "newPassword", newPassword);
        String json = objectMapper.writeValueAsString(body);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/users/change-password"))
            .header("Content-Type", "application/json");
        rb = withAuth(rb);
        HttpRequest request = rb.PUT(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
        } else {
            Map<String, Object> err = objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
            throw new Exception((String) err.getOrDefault("error", "Gagal mengubah password"));
        }
    }

    public Map<String, Object> deleteAccount(String password) throws Exception {
        Map<String, String> body = Map.of("password", password);
        String json = objectMapper.writeValueAsString(body);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/users/delete"))
            .header("Content-Type", "application/json");
        rb = withAuth(rb);
        HttpRequest request = rb.method("DELETE", HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
        } else {
            Map<String, Object> err = objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
            throw new Exception((String) err.getOrDefault("error", "Gagal menghapus akun"));
        }
    }

    // ========== PLAYLIST ENDPOINTS ==========

    public List<Map<String, Object>> getPlaylists() throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists"));
        rb = withAuth(rb);
        HttpRequest request = rb.GET().build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<List<Map<String, Object>>>() {});
        } else {
            throw new Exception("Gagal mengambil playlist: " + response.statusCode());
        }
    }

    public Map<String, Object> createPlaylist(Map<String, Object> playlistData) throws Exception {
        String json = objectMapper.writeValueAsString(playlistData);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists"))
            .header("Content-Type", "application/json");
        rb = withAuth(rb);
        HttpRequest request = rb.POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
        } else {
            throw new Exception("Gagal membuat playlist: " + response.body());
        }
    }

    public void deletePlaylist(Long id) throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists/" + id));
        rb = withAuth(rb);
        HttpRequest request = rb.DELETE().build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new Exception("Gagal menghapus playlist: " + response.statusCode());
        }
    }

    public Map<String, Object> updatePlaylist(Long id, Map<String, Object> data) throws Exception {
        String json = objectMapper.writeValueAsString(data);
        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists/" + id))
            .header("Content-Type", "application/json");
        rb = withAuth(rb);
        HttpRequest request = rb.PUT(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
        } else {
            throw new Exception("Gagal memperbarui playlist: " + response.body());
        }
    }

    // ========== SONG ENDPOINTS ==========

    public List<Map<String, Object>> getSongs(Long playlistId) throws Exception {
        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists/" + playlistId + "/songs"));
        rb = withAuth(rb);
        HttpRequest request = rb.GET().build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(),
                new TypeReference<List<Map<String, Object>>>() {});
        } else {
            throw new Exception("Gagal mengambil lagu: " + response.statusCode());
        }
    }

    public Map<String, Object> addSong(Long playlistId, Map<String, Object> songData) throws Exception {
        String json = objectMapper.writeValueAsString(songData);

        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists/" + playlistId + "/songs"))
            .header("Content-Type", "application/json");
        rb = withAuth(rb);
        HttpRequest request = rb.POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
        } else {
            throw new Exception("Gagal menambah lagu: " + response.body());
        }
    }

    public void deleteSong(Long playlistId, Long songId) throws Exception {
        String url = BASE_URL + "/api/playlists/" + playlistId + "/songs/" + songId;
        System.out.println("[ApiClient] DELETE " + url);
        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(url));
        rb = withAuth(rb);
        HttpRequest request = rb.DELETE().build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        System.out.println("[ApiClient] Response status: " + response.statusCode() + ", body: " + response.body());
        if (response.statusCode() != 204) {
            throw new Exception("Gagal menghapus lagu: " + response.statusCode() + " URL=" + url + " body=" + response.body());
        }
    }

    public void reorderSongs(Long playlistId, List<Long> songIds) throws Exception {
        Map<String, List<Long>> body = Map.of("songIds", songIds);
        String json = objectMapper.writeValueAsString(body);
        HttpRequest.Builder rb = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists/" + playlistId + "/songs/reorder"))
            .header("Content-Type", "application/json");
        rb = withAuth(rb);
        HttpRequest request = rb.PUT(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Gagal mengurutkan ulang lagu: " + response.statusCode());
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
