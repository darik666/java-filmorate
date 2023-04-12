package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.FriendStatus;
import ru.yandex.practicum.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервисный класс пользователей Filmorate
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    /**
     * Добавление пользователя в друзья
     */
    public User addFriend(User user, User friend) {
        user.getFriends().put(friend.getId(), FriendStatus.НЕПОДТВЕРЖДЕННАЯ);
        friend.getFriends().put(user.getId(), FriendStatus.НЕПОДТВЕРЖДЕННАЯ);
        log.debug("Добавление в друзья пользователя с id {}", friend);
        return user;
    }

    /**
     * Удаление пользователей из друзей
     */
    public User deleteFriend(User user, User friend) {
        log.debug("Удаление из друзей пользователя c id {}", friend);
        user.getFriends().remove(friend.getId());
        friend.getFriends().remove(user.getId());
        return user;
    }

    /**
     * Получение списка друзей
     */
    public List<User> getFriends(User user, List<User> all) {
        List<User> friends = new ArrayList<>();
            for (User u : all) {
                if (u.getFriends().keySet().contains(user.getId())) {
                    friends.add(u);
                }
            }
        log.debug("Получение списка друзей {}", friends);
        return friends;
    }

    /**
     * Получение списка общих друзей
     */
    public List<Integer> getCommonFriends(User user1, User user2) {
        log.debug("Получение списка общих друзей пользователей {} и ", user1, " {}", user2);
        return user1.getFriends().keySet()
                .stream()
                .filter(user2.getFriends().keySet()::contains)
                .collect(Collectors.toList());
    }
}
