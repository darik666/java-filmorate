package ru.yandex.practicum.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.Genre;

import java.util.List;

/**
 * Хранилище жанров Filmorate в базе данных
 */
@Slf4j
@Repository("genreDbStorage")
@RequiredArgsConstructor
public class GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Получение жанра по ID
     */
    public Genre getById(Integer id) {
        String sqlQuery = "SELECT * FROM GENRES WHERE GENRE_ID = " + id;
        List<Genre> genreList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("GENRE_ID"));
            genre.setName(rs.getString("GENRE_NAME"));
            return genre;
        });
        if (genreList.isEmpty()) {
            log.warn("Жанра с таким id не найдено");
            throw new RuntimeException(String.format("Жанр с id = %d не найден", id));
        }
        return genreList.get(0);
    }

    /**
     * Получение всех жанров
     */
    public List<Genre> getAll() {
        String sqlQuery = "SELECT * FROM GENRES";
        List<Genre> genreList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("GENRE_ID"));
            genre.setName(rs.getString("GENRE_NAME"));
            return genre;
        });
        log.debug("Текущее количество фильмов: {}", genreList.size());
        return genreList;
    }
}
