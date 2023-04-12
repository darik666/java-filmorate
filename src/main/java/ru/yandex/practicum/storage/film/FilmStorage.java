package ru.yandex.practicum.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.Film;

import java.util.List;

/**
 * Интерфейс хранилища фильмов Filmorate
 */
@Component("filmDbStorage")
public interface FilmStorage {
    List<Film> findAll();

    Film add(Film film);

    Film update(Film film);

    Film findFilmById(Integer id);

    Film putLike(Film film, Integer id);

    Film deleteLike(Integer filmId, Integer id);

    List<Film> findBest(Integer count);
}