package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.model.Genre;
import ru.yandex.practicum.storage.genre.GenreStorage;

import java.util.List;

/**
 * Контроллер жанров
 */
@RestController
@RequiredArgsConstructor
public class GenreController {
    private final GenreStorage storage;

    /**
     * Получение всех жанров
     */
    @GetMapping("/genres")
    public List<Genre> findAll() {
        return storage.getAll();
    }

    /**
     * Получение жанра по id
     */
    @GetMapping("/genres/{id}")
    public Genre findGenre(@PathVariable("id") Integer id) {
        return storage.getById(id);
    }
}
