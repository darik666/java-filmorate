package ru.yandex.practicum.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.Film;
import ru.yandex.practicum.storage.film.InMemoryFilmStorage;

import javax.validation.Valid;
import java.util.List;

/**
 * Контроллер фильмов
 */
@RestController
public class FilmController {
    private final InMemoryFilmStorage storage;

    public FilmController(InMemoryFilmStorage storage) {
        this.storage = storage;
    }

    /**
     * Получение всех фильмов
     */
    @GetMapping("/films")
    public List<Film> findAll() {
        return storage.findAll();
    }

    /**
     * Добавление фильма
     */
    @PostMapping(value = "/films")
    public Film add(@Valid @RequestBody Film film) {
        return storage.add(film);
    }

    /**
     * Обновление фильма
     */
    @PutMapping(value = "/films")
    public Film update(@Valid @RequestBody Film film) {
        return storage.update(film);
    }
}
