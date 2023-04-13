package ru.yandex.practicum.DBStorageTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.exception.FilmNotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.model.Genre;
import ru.yandex.practicum.model.MPA;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FilmDBStorageTests {
    private final FilmStorage storage;
    private final JdbcTemplate jdbcTemplate;

    public FilmDBStorageTests(@Qualifier("filmDbStorage") FilmStorage storage,
                              @Autowired JdbcTemplate jdbcTemplate) {
        this.storage = storage;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    public void createTestFilm() {
        storage.add(giveTerminator());
    }

    public Film giveTerminator() {
        Film film = new Film();
        film.setName("Terminator");
        film.setDescription("Full termination");
        film.setReleaseDate(LocalDate.of(2023,05,05));
        film.setDuration(90);
        film.setMpa(new MPA(5, "NC-17"));
        film.getGenres().add(new Genre(1, "Комедия"));
        return film;
    }

    public Film giveAvatar() {
        Film film = new Film();
        film.setName("Avatar");
        film.setDescription("Avatars planet fantasy");
        film.setReleaseDate(LocalDate.of(2022,12,12));
        film.setDuration(150);
        film.setMpa(new MPA(2, "PG"));
        return film;
    }

    public Film giveTitanic() {
        Film film = new Film();
        film.setName("Titanic");
        film.setDescription("Titanic is sinking");
        film.setReleaseDate(LocalDate.of(2000,02,02));
        film.setDuration(180);
        film.setMpa(new MPA(2, "PG"));
        return film;
    }

    public void createUserWithId(int id) {
        User user = new User();
        user.setId(id);
        user.setEmail("1@1.com");
        user.setLogin("lgn");
        user.setName("nm");
        user.setBirthday(LocalDate.of(1978, 10, 10));
        String sqlQuery = "INSERT INTO USERS(" +
                "USER_ID, " +
                "USER_EMAIL, " +
                "USER_LOGIN, " +
                "USER_NAME, " +
                "BIRTHDAY) " +
                "values (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlQuery,
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());
    }

    @Test
    public void addFilmTest() {
        Film film = giveTerminator();
        storage.add(film);
        film = storage.findFilmById(1);

        assertThat(film)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Terminator")
                .hasFieldOrPropertyWithValue("duration", 90L);
        assertEquals(5, film.getMpa().getId());
    }

    @Test
    public void addFilmEReleaseDateErrorTest() {
        Film film = giveTerminator();
        film.setReleaseDate(LocalDate.of(1777, 7, 7));
        Throwable thrown = catchThrowable(() -> {
            storage.add(film);
        });

        assertThat(thrown).isInstanceOf(ValidationException.class)
                .hasMessage("Ошибка валидации даты релиза");
    }

    @Test
    public void updateFilmTest() {
        Film film = giveTerminator();
        film.setId(1);
        film.setName("Galileo");
        film.setDuration(120L);
        storage.update(film);
        assertThat(storage.findFilmById(1))
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Galileo")
                .hasFieldOrPropertyWithValue("duration", 120L);
    }

    @Test
    public void updateFilmError() {
        Film film = new Film();
        film.setId(-2);
        film.setReleaseDate(LocalDate.of(1990, 9, 9));
        Throwable thrown = catchThrowable(() -> {
            storage.update(film);
        });

        assertThat(thrown).isInstanceOf(FilmNotFoundException.class);
        assertThat(thrown.getMessage()).isEqualTo(String.format("Фильма с таким id не найдено"));
    }

    @Test
    public void getAllFilmsTest() {
        storage.add(giveAvatar());
        List<Film> users = storage.findAll();

        assertEquals(users.size(), 2);
        assertThat(users.containsAll(List.of(giveTerminator(), giveAvatar())));
    }

    @Test
    public void testFindFilmById() {
        Optional<Film> userOptional = Optional.ofNullable(storage.findFilmById(1));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("name", "Terminator")
                                .hasFieldOrPropertyWithValue("duration", 90L)
                );
    }

    @Test
    public void testFindFilmByWrongId() {
        Throwable thrown = catchThrowable(() -> {
            storage.findFilmById(2);
        });

        assertThat(thrown).isInstanceOf(FilmNotFoundException.class);
        assertThat(thrown.getMessage()).isEqualTo(String.format("Фильм с id = %d не найден", 2));
    }

    @Test
    public void putLikeTest() {
        createUserWithId(1);
        storage.putLike(storage.findFilmById(1), 1);
        Set<Integer> likes = storage.findFilmById(1).getLikes();

        assertNotNull(likes);
        assertTrue(likes.contains(1));
    }

    @Test
    public void putLikeErrorTest() {
        Film film = new Film();
        film.setId(-2);
        film.setReleaseDate(LocalDate.of(1990, 9, 9));
        Throwable thrown = catchThrowable(() -> {
            storage.putLike(film, 1);
        });

        assertThat(thrown).isInstanceOf(RuntimeException.class);
        assertThat(thrown.getMessage()).isEqualTo(String.format("Ошибка добавления лайка"));
    }

    @Test
    public void deleteLikeTest() {
        createUserWithId(1);
        Film film = storage.findFilmById(1);
        storage.putLike(film, 1);

        assertTrue(film.getLikes().size() == 1);

        storage.deleteLike(1, 1);

        assertTrue(storage.findFilmById(1).getLikes().size() == 0);
    }

    @Test
    public void deleteLikeErrorTest() {
        Throwable thrown = catchThrowable(() -> {
            storage.deleteLike(1, 1);
        });

        assertThat(thrown).isInstanceOf(RuntimeException.class);
        assertThat(thrown.getMessage()).isEqualTo(String.format("Ошибка удаления лайка"));
    }

    @Test
    public void getPopularTest() {
        createUserWithId(1);
        createUserWithId(2);
        createUserWithId(3);
        createUserWithId(4);
        storage.add(giveAvatar());
        storage.add(giveTitanic());
        storage.putLike(storage.findFilmById(2), 4);
        storage.putLike(storage.findFilmById(2), 1);
        storage.putLike(storage.findFilmById(2), 3);
        storage.putLike(storage.findFilmById(3), 2);
        storage.putLike(storage.findFilmById(3), 1);
        storage.putLike(storage.findFilmById(1), 3);

        List<Film> popular = storage.findBest(2);

        assertTrue(popular.size() == 2);
        assertEquals(2, popular.get(0).getId());
        assertEquals(3, popular.get(1).getId());
    }
}