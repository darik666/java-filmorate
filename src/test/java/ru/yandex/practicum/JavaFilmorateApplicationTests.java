package ru.yandex.practicum;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import ru.yandex.practicum.controller.FilmController;
import ru.yandex.practicum.controller.UserController;
import ru.yandex.practicum.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.storage.user.InMemoryUserStorage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest
class JavaFilmorateApplicationTests {
    private FilmController filmController;
    private UserController userController;
    private HttpClient client;
    private ConfigurableApplicationContext ctx;

    @BeforeEach
    void setUp() {

        ctx = SpringApplication.run(JavaFilmorateApplication.class);
        client = HttpClient.newHttpClient();
        filmController = new FilmController(new InMemoryFilmStorage());
        userController = new UserController(new InMemoryUserStorage());
    }

    @AfterEach
    void exit() {
        SpringApplication.exit(ctx);
    }

    public HttpResponse<String> filmsResponse(String method, String s) {
        URI url = URI.create("http://localhost:8080/films");
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(s);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .method(method, body)
                .build();
        return sendText(request);
    }

    public HttpResponse<String> usersResponse(String method, String s) {
        URI url = URI.create("http://localhost:8080/users");
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(s);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .header("Accept", "*/*")
                .method(method, body)
                .build();
        return sendText(request);
    }

    @Test
    public void getAllFilmsTest() {
        String film = "{\"name\":\"nisieiusmod\",\"description\":\"adipisicing\"," +
                "\"releaseDate\":\"1967-03-25\",\"duration\":100}";
        String film2 = "{\"name\":\"nisieiusmod-Extended\",\"description\":\"adipisicing-Extended\"," +
                "\"releaseDate\":\"1968-07-28\",\"duration\":140}";
        filmsResponse("POST", film);
        filmsResponse("POST", film2);

        HttpResponse response = filmsResponse("GET", "");
        JsonArray jsonArray = JsonParser.parseString(response.body().toString()).getAsJsonArray();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(2, jsonArray.size());
    }

    @Test
    public void addFilmInvalidReleaseDataTest() {
        String invalidDateFilm = "{\"name\":\"Name\",\"description\":\"Description\"," +
                "\"releaseDate\":\"1890-03-25\",\"duration\":200}";

        HttpResponse response = filmsResponse("POST", invalidDateFilm);

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertTrue(filmController.findAll().isEmpty());
    }

    @Test
    void addFilmEmptyNameTest() {
        String emptyNameFilm = "{\"name\":\"\",\"description\":\"Description\"," +
                "\"releaseDate\":\"1900-03-25\",\"duration\":200}";

        HttpResponse response = filmsResponse("POST", emptyNameFilm);

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertTrue(filmController.findAll().isEmpty());
    }

    @Test
    public void addFilmMaxDescriptionTest() {
        String longDescriptionFilm = "{\"name\":\"Filmname\",\"description\":\"Пятеродрузей(комик-группа«Шарло»)," +
                "приезжаютвгородБризуль.ЗдесьонихотятразыскатьгосподинаОгюстаКуглова,которыйзадолжалимденьги," +
                "аименно20миллионов.оКуглов,которыйзавремя«своегоотсутствия»,сталкандидатомКоломбани.\"," +
                "\"releaseDate\":\"1900-03-25\",\"duration\":200}";

        HttpResponse response = filmsResponse("POST", longDescriptionFilm);

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertTrue(filmController.findAll().isEmpty());
    }

    @Test
    public void addFilmNegativeDurationTest() {
        String invalidDuration = "{\"name\":\"Name\",\"description\":\"Descrition\"," +
                "\"releaseDate\":\"1980-03-25\",\"duration\":-200}";

        HttpResponse response = filmsResponse("POST", invalidDuration);

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertTrue(filmController.findAll().isEmpty());
    }

    @Test
    void addFilmShouldAddTest() {
        String film = "{\"name\":\"nisieiusmod\",\"description\":\"adipisicing\"," +
                "\"releaseDate\":\"1967-03-25\",\"duration\":100}";
        HttpResponse response = filmsResponse("POST", film);
        JsonObject jsonObject = JsonParser.parseString(response.body().toString()).getAsJsonObject();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(1, jsonObject.get("id").getAsInt());
    }

    @Test
    public void updateFilmNotFoundTest() {
        String film = "{\"id\":9999,\"name\":\"FilmUpdated\",\"releaseDate\":\"1989-04-17\"," +
                "\"description\":\"Newfilmupdatedecription\",\"duration\":190,\"rate\":4}";

        HttpResponse response = filmsResponse("PUT", film);
        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    public void updateFilmInvalidReleaseDataTest() {
        String film = "{\"name\":\"Name\",\"description\":\"Description\"," +
                "\"releaseDate\":\"1904-08-20\",\"duration\":200}";
        String invalidDateFilm = "{\"name\":\"Name\",\"description\":\"Description\"," +
                "\"releaseDate\":\"1890-03-25\",\"duration\":200}";

        filmsResponse("POST", film);
        HttpResponse response = filmsResponse("PUT", invalidDateFilm);
        JsonObject jsonObject = JsonParser.parseString(response.body().toString()).getAsJsonObject();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertFalse(jsonObject.has("releaseDate"));
    }

    @Test
    public void updateFilmShouldWorkTest() {
        String film = "{\"name\":\"Name\",\"description\":\"Description\"," +
                "\"releaseDate\":\"1904-08-20\",\"duration\":200}";
        String updateFilm = "{\"id\":1,\"name\":\"FilmUpdated\",\"releaseDate\":\"1906-11-25\"," +
                "\"description\":\"Newfilmupdatedecription\",\"duration\":190,\"rate\":4}";

        filmsResponse("POST", film);
        HttpResponse response = filmsResponse("PUT", updateFilm);
        JsonObject jsonObject = JsonParser.parseString(response.body().toString()).getAsJsonObject();
        String updatedDate = jsonObject.get("releaseDate").toString();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertTrue(jsonObject.has("releaseDate"));
        Assertions.assertEquals("\"1906-11-25\"", updatedDate.toString());
    }

    @Test
    public void createUserBlankLoginTest() {
        String user = "{\n  \"login\": \"\",\n  \"name\": \"Nick Name\"," +
                "\n  \"email\": \"mail@mail.ru\",\n  \"birthday\": \"1996-08-20\"\n}";

        HttpResponse response = usersResponse("POST", user);

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertTrue(userController.findAll().isEmpty());
    }

    @Test
    public void createUserIncorrectLoginTest() {
        String user = "{\n  \"login\": \"dolore ullamco\"," +
                "\n  \"email\": \"yandex@mail.ru\",\n  \"birthday\": \"1995-08-20\"\n}";

        HttpResponse response = usersResponse("POST", user);

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertTrue(userController.findAll().isEmpty());
    }

    @Test
    public void createUserWithEmptyNameTest() {
        String user = "{\n  \"login\": \"common\"," +
                "\n  \"email\": \"friend@common.ru\",\n  \"birthday\": \"2000-08-20\"\n}";

        HttpResponse response = usersResponse("POST", user);
        JsonObject jsonObject = JsonParser.parseString(response.body().toString()).getAsJsonObject();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(1, jsonObject.get("id").getAsInt());
        Assertions.assertEquals("\"common\"", jsonObject.get("name").toString());
    }

    @Test
    public void createUserIncorrectEmailTest() {
        String user = "{\"login\":\"doloreullamco\",\"name\":\"\",\"email\":\"mail.ru\"," +
                "\"birthday\":\"1980-08-20\"}";

        HttpResponse response = usersResponse("POST", user);

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertTrue(userController.findAll().isEmpty());
    }

    @Test
    public void createUserWithIncorrectBirthdayTest() {
        String user = "{\n  \"login\": \"dolore\",\n  \"name\": \"\"," +
                "\n  \"email\": \"test@mail.ru\",\n  \"birthday\": \"2446-08-20\"\n}";

        HttpResponse response = usersResponse("POST", user);

        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertTrue(userController.findAll().isEmpty());
    }

    @Test
    public void updateUserWithIncorrectLoginTest() {
        String user = "{\n  \"login\": \"dolore\",\n  \"name\": \"Nick Name\"," +
                "\n  \"email\": \"mail@mail.ru\",\n  \"birthday\": \"1946-08-20\"\n}";
        String updatedUser = "{\n  \"login\": \"dolore Update\",\n  \"name\": \"est adipisicing\"," +
                "\n  \"id\": 1,\n  \"email\": \"mail@yandex.ru\",\n  \"birthday\": \"1976-09-20\"\n}";

        usersResponse("POST", user);
        HttpResponse response = usersResponse("PUT", updatedUser);
        JsonObject jsonObject = JsonParser.parseString(response.body().toString()).getAsJsonObject();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertFalse(jsonObject.has("birthDay"));
    }

    @Test
    public void updateUserWithoutNameTest() {
        String user = "{\n  \"login\": \"dolore\",\n  \"name\": \"Nick Name\"," +
                "\n  \"email\": \"mail@mail.ru\",\n  \"birthday\": \"1946-08-20\"\n}";
        String updatedUser = "{\n  \"login\": \"doloreUpdate\",\n  \"name\": \"\"," +
                "\n  \"id\": 1,\n  \"email\": \"mail@yandex.ru\",\n  \"birthday\": \"1976-09-20\"\n}";

        usersResponse("POST", user);
        HttpResponse response = usersResponse("PUT", updatedUser);
        JsonObject jsonObject = JsonParser.parseString(response.body().toString()).getAsJsonObject();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("\"doloreUpdate\"", jsonObject.get("name").toString());
    }

    @Test
    public void updateUserNotFoundTest() {
        String user = "{\n  \"login\": \"dolore\",\n  \"name\": \"Nick Name\"," +
                "\n  \"email\": \"mail@mail.ru\",\n  \"birthday\": \"1946-08-20\"\n}";
        String updatedUser = "{\n  \"login\": \"doloreUpdate\",\n  \"name\": \"est adipisicing\"," +
                "\n  \"id\": 9999,\n  \"email\": \"mail@yandex.ru\",\n  \"birthday\": \"1976-09-20\"\n}";

        usersResponse("POST", user);
        HttpResponse response = usersResponse("PUT", updatedUser);

        Assertions.assertEquals(500, response.statusCode());
    }

    @Test
    public void getAllUsersTest() {
        String user = "{\n  \"login\": \"dolore\",\n  \"name\": \"Nick Name\"," +
                "\n  \"email\": \"mail@mail.ru\",\n  \"birthday\": \"1946-08-20\"\n}";
        String user2 = "{\n  \"login\": \"Mark\",\n  \"name\": \"markus\"," +
                "\n  \"email\": \"markus@mail.ru\",\n  \"birthday\": \"1952-10-10\"\n}";

        usersResponse("POST", user);
        usersResponse("POST", user2);
        HttpResponse response = usersResponse("GET", "");
        JsonArray jsonArray = JsonParser.parseString(response.body().toString()).getAsJsonArray();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(2, jsonArray.size());
    }

    public HttpResponse<String> sendText(HttpRequest request) {
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}