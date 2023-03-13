package ru.yandex.practicum.storage.user;

import ru.yandex.practicum.model.User;

import java.util.List;

public interface UserStorage {
    List<User> findAll();
    User create(User user);
    User update(User user);
}
