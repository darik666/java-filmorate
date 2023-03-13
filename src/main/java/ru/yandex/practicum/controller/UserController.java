package ru.yandex.practicum.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.storage.user.InMemoryUserStorage;

import javax.validation.Valid;
import java.util.List;

/**
 * Контроллер пользователей
 */
@RestController
public class UserController {
    private final InMemoryUserStorage storage;

    public UserController(InMemoryUserStorage storage) {
        this.storage = storage;
    }

    /**
     * Получение всех пользователей
     */
    @GetMapping("/users")
    public List<User> findAll() {
        return storage.findAll();
    }

    /**
     * Создание пользователя
     */
    @PostMapping(value = "/users")
    public User create(@Valid @RequestBody User user) {
        return storage.create(user);
    }

    /**
     * Обновление пользователя
     */
    @PutMapping(value = "/users")
    public User update(@Valid @RequestBody User user) {
        return storage.update(user);
    }
}