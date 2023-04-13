package ru.yandex.practicum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.storage.user.UserStorage;

import javax.validation.Valid;
import java.util.List;

/**
 * Контроллер пользователей
 */
@RestController
public class UserController {
    @Qualifier("userDbStorage")
    private final UserStorage storage;

    @Autowired
    public UserController(@Qualifier("userDbStorage") UserStorage storage) {
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

    /**
     * Получение пользователя по id
     */
    @GetMapping("/users/{id}")
    public User findUser(@PathVariable("id") Integer id) {
        return storage.findUserById(id);
    }

    /**
     * Добавление в друзья
     */
    @PutMapping("/users/{id}/friends/{friendId}")
    public User addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        return storage.addFriend(storage.findUserById(id), storage.findUserById(friendId));
    }

    /**
     * Удаление из друзей
     */
    @DeleteMapping("/users/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        return storage.deleteFriend(storage.findUserById(id), storage.findUserById(friendId));
    }

    /**
     * Получение друзей пользователя
     */
    @GetMapping("/users/{id}/friends")
    public List<User> getFriends(@PathVariable Integer id) {
        return storage.getFriends(storage.findUserById(id));
    }

    /**
     * Получение общих друзей
     */
    @GetMapping("/users/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        return storage.getCommonFriends(storage.findUserById(id), storage.findUserById(otherId));
    }
}