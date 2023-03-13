package ru.yandex.practicum.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.exception.ValidationException;
import ru.yandex.practicum.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final HashMap<Integer, User> users;
    private int id = 0;

    @Autowired
    public InMemoryUserStorage() {
        this.users = new HashMap<>();
    }

    public List<User> findAll() {
        log.debug("Текущее количество пользователей: {}", users.size());
        return new ArrayList<>(users.values());
    }

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
}
