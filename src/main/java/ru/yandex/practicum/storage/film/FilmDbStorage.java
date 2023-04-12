package ru.yandex.practicum.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.exception.FilmNotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.model.Genre;
import ru.yandex.practicum.model.MPA;

import java.time.LocalDate;
import java.util.*;

/**
 * Хранилище фильмов Filmorate в базе данных
 */
@Slf4j
@Repository("filmDbStorage")
@Primary
public class FilmDbStorage implements FilmStorage {
    private JdbcTemplate jdbcTemplate;
    private Integer idCounter = 0;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Получение всех фильмов
     */
    @Override
    public List<Film> findAll() {
        String sqlQuery = "SELECT * FROM FILMS";
        List<Film> filmList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("FILM_ID"));
            film.setName(rs.getString("FILM_NAME"));
            film.setDescription(rs.getString("DESCRIPTION"));
            film.setReleaseDate(rs.getDate("RELEASE_DATE").toLocalDate());
            film.setDuration(rs.getInt("DURATION"));
            loadLikesAndGenres(film);
            return film;
        });
        log.debug("Текущее количество фильмов: {}", filmList.size());
        return filmList;
    }

    /**
     * Добавление фильма
     */
    @Override
    public Film add(Film film) {
        Integer maxCounter = jdbcTemplate.queryForObject("SELECT MAX(FILM_ID) FROM FILMS", Integer.class);
        if (maxCounter != null) {
            idCounter = maxCounter;
        }
        film.setId(++idCounter);
        checkReleaseDate(film);
        String sqlQuery = "INSERT INTO FILMS(FILM_ID, FILM_NAME, DESCRIPTION, RELEASE_DATE, DURATION) " +
                    "values (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlQuery,
                    film.getId(),
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration());
        String mpaQuery = "INSERT INTO FILM_MPA(FILM_ID, MPA_ID) " +
                "values (?, ?)";
        jdbcTemplate.update(mpaQuery,
                film.getId(),
                film.getMpa().getId());
        SqlRowSet mparow = jdbcTemplate.queryForRowSet("SELECT * FROM MPA WHERE MPA_ID = ?",
                film.getMpa().getId());
        while (mparow.next()) {
            film.setMpa(new MPA(mparow.getInt("MPA_ID"), mparow.getString("MPA_NAME")));
        }
        String genreQuery = "INSERT INTO FILM_GENRES(FILM_ID, GENRE_ID) VALUES (?, ?)";
        List<Object[]> batchList = new ArrayList<>();
        for (Genre genre: film.getGenres()) {
            batchList.add(new Object[] {film.getId(), genre.getId()});
        }
        jdbcTemplate.batchUpdate(genreQuery, batchList);
        loadLikesAndGenres(film);
        log.debug("Фильм к сохранению: {}", film);
        return film;
    }

    /**
     * Обновление фильма
     */
    @Override
    public Film update(Film film) {
        checkReleaseDate(film);
        String sqlQuery1 = "SELECT COUNT(*) FROM FILMS WHERE FILM_ID = ?";
        jdbcTemplate.queryForObject(sqlQuery1, Integer.class, film.getId());
        String sqlQuery = "UPDATE FILMS SET FILM_ID = ?, FILM_NAME = ?, " +
                "DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ? WHERE FILM_ID = ?";
        int rowsUpdated = jdbcTemplate.update(sqlQuery,
                film.getId(),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate().toString(),
                film.getDuration(),
                film.getId());
        if (rowsUpdated == 0) {
            throw new FilmNotFoundException("Фильма с таким id не найдено");
        }
        String mpaQuery = "UPDATE FILM_MPA SET FILM_ID = ?, MPA_ID = ? WHERE FILM_ID = ?";
        jdbcTemplate.update(mpaQuery,
                film.getId(),
                film.getMpa().getId(),
                film.getId());
        SqlRowSet mparow = jdbcTemplate.queryForRowSet("SELECT * FROM MPA WHERE MPA_ID = ?",
                film.getMpa().getId());
        while (mparow.next()) {
            film.setMpa(new MPA(mparow.getInt("MPA_ID"), mparow.getString("MPA_NAME")));
        }
        jdbcTemplate.update("DELETE FROM FILM_GENRES WHERE FILM_ID = ?", film.getId());
        String genreQuery = "INSERT INTO FILM_GENRES(FILM_ID, GENRE_ID) VALUES (?, ?)";
        List<Object[]> batchList = new ArrayList<>();
        for (Genre genre: film.getGenres()) {
            batchList.add(new Object[] {film.getId(), genre.getId()});
        }
        jdbcTemplate.batchUpdate(genreQuery, batchList);
        log.debug("Фильм к обновлению: {}", film);
        return loadLikesAndGenres(film);
    }

    /**
     * Получение фильма по id
     */
    @Override
    public Film findFilmById(Integer id) {
        String sqlQuery = "SELECT * FROM FILMS WHERE FILM_ID = " + id;
        List<Film> filmList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("FILM_ID"));
            film.setName(rs.getString("FILM_NAME"));
            film.setDescription(rs.getString("DESCRIPTION"));
            film.setReleaseDate(rs.getDate("RELEASE_DATE").toLocalDate());
            film.setDuration(rs.getInt("DURATION"));
            loadLikesAndGenres(film);
            log.debug("Фильм по id: {}", film);
            return film;
        });
        if (filmList.isEmpty()) {
            log.warn("Фильма с таким id не найдено");
            throw new FilmNotFoundException(String.format("Фильм с id = %d не найден", id));
        }
        return filmList.get(0);
    }

    /**
     * Установка лайка фильму
     */
    @Override
    public Film putLike(Film film, Integer id) {
        String sqlCheck = "SELECT COUNT(*) FROM USERS WHERE USER_ID = ?";
        int count = jdbcTemplate.queryForObject(sqlCheck, Integer.class, id);
        if (count == 0) {
            throw new RuntimeException("Ошибка добавления лайка");
        }
        String sqlQuery = "INSERT INTO LIKES (FILM_ID, USER_ID) VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery, film.getId(), id);
        film.getLikes().add(id);
        log.debug("Установка лайка у фильма с id = {}", film.getId());
        return film;
    }

    /**
     * Удаление лайка фильма
     */
    @Override
    public Film deleteLike(Integer filmId, Integer id) {
        Film film = findFilmById(filmId);
        String sqlQuery = "DELETE FROM LIKES WHERE FILM_ID = ? AND USER_ID= ?";
        int deleted = jdbcTemplate.update(sqlQuery, filmId, id);
        if (deleted == 0) {
            throw new RuntimeException("Ошибка удаления лайка");
        }
        film.getLikes().remove(id);
        log.debug("Удаление лайка у фильма с id = {}", filmId);
        return film;
    }

    /**
     * Получение популярных N фильмов
     */
    @Override
    public List<Film> findBest(Integer count) {
        log.debug("Получение {} популярных фильмов", count);
        String sqlQuery = "SELECT " +
                "F.FILM_ID, " +
                "F.FILM_NAME, " +
                "F.DESCRIPTION, " +
                "F.RELEASE_DATE, " +
                "F.DURATION, " +
                "COUNT(L.FILM_ID) AS LIKES " +
                "FROM FILMS F LEFT JOIN LIKES L ON F.FILM_ID = L.FILM_ID " +
                "GROUP BY F.FILM_ID, F.FILM_NAME, F.DESCRIPTION, F.RELEASE_DATE, F.DURATION " +
                "ORDER BY LIKES DESC LIMIT " + count;
        List<Film> filmList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getInt("FILM_ID"));
            film.setName(rs.getString("FILM_NAME"));
            film.setDescription(rs.getString("DESCRIPTION"));
            film.setReleaseDate(rs.getDate("RELEASE_DATE").toLocalDate());
            film.setDuration(rs.getInt("DURATION"));
            loadLikesAndGenres(film);
            return film;
        });
        return filmList;
    }

    /**
     * Загрузка лайков и жанров фильма из базы данных
     */
    public Film loadLikesAndGenres(Film film) {
        Set<Integer> likes = new HashSet<>();
        Set<Genre> genres = new TreeSet<>();

        SqlRowSet likeRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM LIKES WHERE FILM_ID = ?", film.getId());
        while (likeRows.next()) {
            likes.add(likeRows.getInt("USER_ID"));
        }
        film.setLikes(likes);

        String genreQuery = "SELECT * FROM FILM_GENRES " +
                    "JOIN GENRES ON FILM_GENRES.GENRE_ID = GENRES.GENRE_ID WHERE FILM_ID = ?";
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(genreQuery, film.getId());
        while (genreRows.next()) {
            genres.add(new Genre(genreRows.getInt("GENRE_ID"),
                    genreRows.getString("GENRE_NAME")));
        }
        film.setGenres(genres);

        String mpaQuery = "SELECT * FROM (SELECT * FROM FILM_MPA WHERE FILM_ID = ?) AS M " +
                "JOIN MPA ON M.MPA_ID = MPA.MPA_ID;";
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet(mpaQuery, film.getId());
        while (mpaRows.next()) {
            film.setMpa(new MPA(mpaRows.getInt("MPA_ID"), mpaRows.getString("MPA_NAME")));
        }
        return film;
    }

    /**
     * Валидация даты релиза
     */
    public void checkReleaseDate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации даты релиза: {} ", film.getReleaseDate());
            throw new ValidationException("Ошибка валидации даты релиза");
        }
    }
}
