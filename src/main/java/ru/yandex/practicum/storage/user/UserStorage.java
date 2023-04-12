package ru.yandex.practicum.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.model.User;

import java.util.List;

/**
 * Интерфейс хранилища пользователей Filmorate
 */
@Component("userDbStorage")
public interface UserStorage {
    List<User> findAll();

    User create(User user);

    User update(User user);

    User findUserById(Integer id);

    User addFriend(User user, User friend);

    User deleteFriend(User user, User friend);

    List<User> getFriends(User user);

    List<User> getCommonFriends(User user1, User user2);
}
