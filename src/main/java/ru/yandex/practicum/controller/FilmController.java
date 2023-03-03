package ru.yandex.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер фильмов
 */
@Slf4j
@RestController
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 0;

    /**
     * Получение всех фильмов
     */
    @GetMapping("/films")
    public List<Film> findAll() {
        log.debug("Текущее количество фильмов: {}", films.size());
        return new ArrayList<>(films.values());
    }

    /**
     * Добавление фильма
     */
    @PostMapping(value = "/films")
    public Film add(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации даты релиза: {} ", film.getReleaseDate());
            throw new ValidationException("Ошибка валидации даты релиза");
        } else {
            film.setId(++id);
            films.put(film.getId(), film);
        }
        return film;
    }

    /**
     * Обновление фильма
     */
    @PutMapping(value = "/films")
    public Film update(@Valid @RequestBody Film film) {
        log.debug("Фильм к обновлению: {}", film);
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Дата релиза выходит за рамки допустимого");
            throw new ValidationException("Дата релиза выходит за рамки допустимого");
        } else if (films.containsKey(film.getId())) {
            films.replace(film.getId(), film);
            return film;
        } else {
            log.warn("Фильма с таким id не найдено");
            throw new ValidationException("Фильма с таким id не найдено");
        }
    }
}
