package ru.yandex.practicum.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.exception.FilmNotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.service.FilmService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Хранилище фильмов Filmorate в памяти
 */
@Slf4j
@Repository("filmMemoryStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private final FilmService service;
    private final Map<Integer, Film> films;
    private int id = 0;

    @Autowired
    public InMemoryFilmStorage(FilmService service) {
        this.films = new HashMap<>();
        this.service = service;
    }

    /**
     * Получение всех фильмов
     */
    public List<Film> findAll() {
        log.debug("Текущее количество фильмов: {}", films.size());
        return new ArrayList<>(films.values());
    }

    /**
     * Добавление фильма
     */
    public Film add(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации даты релиза: {} ", film.getReleaseDate());
            throw new ValidationException("Ошибка валидации даты релиза");
        } else {
            film.setId(++id);
            films.put(film.getId(), film);
        }
        log.debug("Фильм к сохранению: {}", film);
        return film;
    }

    /**
     * Обновление фильма
     */
    public Film update(Film film) {
        log.debug("Фильм к обновлению: {}", film);
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Дата релиза выходит за рамки допустимого");
            throw new ValidationException("Дата релиза выходит за рамки допустимого");
        } else if (films.containsKey(film.getId())) {
            films.replace(film.getId(), film);
            return film;
        } else {
            log.warn("Фильма с таким id не найдено");
            throw new FilmNotFoundException(String.format("Фильм № %d не найден", id));
        }
    }

    /**
     * Получение фильма по id
     */
    public Film findFilmById(Integer id) {
        Film film = films.get(id);
        if (film != null) {
            log.debug("Фильм по id: {}", film);
            return film;
        } else {
            log.warn("Фильма с таким id не найдено");
            throw new FilmNotFoundException(String.format("Фильм № %d не найден", id));
        }
    }

    /**
     * Установка лайка фильму
     */
    @Override
    public Film putLike(Film film, Integer id) {
        return null;
    }

    /**
     * Удаление лайка фильма
     */
    @Override
    public Film deleteLike(Integer filmId, Integer id) {
        return service.deleteLike(findFilmById(filmId), id);
    }

    /**
     * Получение популярных N фильмов
     */
    @Override
    public List<Film> findBest(Integer count) {
        return service.findBest(count, findAll());
    }
}
