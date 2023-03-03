package ru.yandex.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Контроллер пользователей
 */
@Slf4j
@RestController
public class UserController {
    private final HashMap<Integer, User> users = new HashMap<>();
    private int id = 0;

    /**
     * Получение всех пользователей
     */
    @GetMapping("/users")
    public List<User> findAll() {
        log.debug("Текущее количество пользователей: {}", users.size());
        return new ArrayList<>(users.values());
    }

    /**
     * Создание пользователя
     */
    @PostMapping(value = "/users")
    public User create(@Valid @RequestBody User user) {
        if (user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации логина");
            throw new ValidationException("Ошибка валидации логина");
        } else {
            log.debug("Пользователь к сохранению: {}", user);
            if (user.getName() == null || user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            user.setId(++id);
            users.put(user.getId(), user);
        }
        return user;
    }

    /**
     * Обновление пользователя
     */
    @PutMapping(value = "/users")
    public User update(@Valid @RequestBody User user) {
        log.debug("Пользователь к обновлению: {}", user);
        if (user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации логина", user.getLogin());
            throw new ValidationException("Ошибка валидации логина");
        } else if (users.containsKey(user.getId())) {
            if (user.getName().isBlank()) {
                user.setName(user.getLogin());
            }
            users.put(user.getId(), user);
            return user;
        } else {
            log.warn("Пользователя с таким id не найдено");
            throw new ValidationException("Пользователя с таким id не найдено");
        }
    }
}
