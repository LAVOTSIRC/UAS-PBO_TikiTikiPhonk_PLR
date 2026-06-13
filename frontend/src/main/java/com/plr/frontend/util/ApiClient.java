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
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/tasks"))
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .GET()
            .build();

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

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/tasks"))
            .header("Content-Type", "application/json")
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

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

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/tasks/" + id))
            .header("Content-Type", "application/json")
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .PUT(HttpRequest.BodyPublishers.ofString(json))
            .build();

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
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/tasks/" + id))
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .DELETE()
            .build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new Exception("Gagal menghapus tugas: " + response.statusCode());
        }
    }

    // ========== POMODORO ENDPOINTS ==========

    public List<Map<String, Object>> getPomodoroSessions() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/pomodoro/sessions"))
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .GET()
            .build();

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
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/pomodoro/sessions/task/" + taskId))
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .GET()
            .build();

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

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/pomodoro/sessions"))
            .header("Content-Type", "application/json")
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            return objectMapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});
        } else {
            throw new Exception("Gagal menyimpan sesi: " + response.body());
        }
    }

    // ========== PLAYLIST ENDPOINTS ==========

    public List<Map<String, Object>> getPlaylists() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists"))
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .GET()
            .build();

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

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists"))
            .header("Content-Type", "application/json")
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

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
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists/" + id))
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .DELETE()
            .build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 204) {
            throw new Exception("Gagal menghapus playlist: " + response.statusCode());
        }
    }

    public Map<String, Object> updatePlaylist(Long id, Map<String, Object> data) throws Exception {
        String json = objectMapper.writeValueAsString(data);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists/" + id))
            .header("Content-Type", "application/json")
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .PUT(HttpRequest.BodyPublishers.ofString(json))
            .build();

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
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists/" + playlistId + "/songs"))
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .GET()
            .build();

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

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists/" + playlistId + "/songs"))
            .header("Content-Type", "application/json")
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

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
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .DELETE()
            .build();

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
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/playlists/" + playlistId + "/songs/reorder"))
            .header("Content-Type", "application/json")
            .header("Authorization", SessionManager.getInstance().getAuthorizationHeader())
            .PUT(HttpRequest.BodyPublishers.ofString(json))
            .build();

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
