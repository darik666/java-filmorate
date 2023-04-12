package ru.yandex.practicum.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.exception.UserNotFoundException;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Хранилище пользователей Filmorate в памяти
 */
@Slf4j
@Repository("userMemoryStorage")
public class InMemoryUserStorage implements UserStorage {
    private final UserService service;
    private final HashMap<Integer, User> users;
    private int id = 0;

    @Autowired
    public InMemoryUserStorage(UserService service) {
        this.users = new HashMap<>();
        this.service = service;
    }

    /**
     * Получение всех пользователей
     */
    public List<User> findAll() {
        log.debug("Текущее количество пользователей: {}", users.size());
        return new ArrayList<>(users.values());
    }

    /**
     * Создание пользователя
     */
    public User create(User user) {
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
    public User update(User user) {
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

    /**
     * Получение пользователя по id
     */
    public User findUserById(Integer id) {
        log.debug("Пользователь по id: {}", users.get(id));
        return users.values().stream()
                .filter(u -> u.getId() == (id))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователь № %d не найден", id)));
    }

    /**
     * Добавление в друзья
     */
    @Override
    public User addFriend(User user, User friend) {
        return service.addFriend(user, friend);
    }

    /**
     * Удаление из друзей
     */
    @Override
    public User deleteFriend(User user, User friend) {
        return service.deleteFriend(user, friend);
    }

    /**
     * Получение всех друзей
     */
    @Override
    public List<User> getFriends(User user) {
        return service.getFriends(user, findAll());
    }

    /**
     * Получение общих друзей
     */
    @Override
    public List<User> getCommonFriends(User user1, User user2) {
        List<Integer> commonIds = service.getCommonFriends(user1, user2);
        List<User> commonFriends = new ArrayList<>();
        for (Integer i : commonIds) {
            commonFriends.add(findUserById(i));
        }
        return commonFriends;
    }
}