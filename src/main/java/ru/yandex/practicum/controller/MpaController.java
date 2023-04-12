package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.model.MPA;
import ru.yandex.practicum.storage.mpa.MPAStorage;

import java.util.List;

/**
 * Контроллер рейтингов
 */
@RestController
@RequiredArgsConstructor
public class MpaController {
    private final MPAStorage storage;

    /**
     * Получение всех рейтингов
     */
    @GetMapping("/mpa")
    public List<MPA> findAll() {
        return storage.getAll();
    }

    /**
     * Получение рейтинга по id
     */
    @GetMapping("/mpa/{id}")
    public MPA findMpa(@PathVariable("id") Integer id) {
        return storage.getById(id);
    }
}
