package ru.yandex.practicum.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.exception.UserNotFoundException;
import ru.yandex.practicum.model.FriendStatus;
import ru.yandex.practicum.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Хранилище пользователей Filmorate в базе данных
 */
@Slf4j
@Repository("userDbStorage")
@Primary
public class UserDbStorage implements UserStorage {
    private JdbcTemplate jdbcTemplate;
    private Integer idCounter = 0;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Получение всех пользователей
     */
    public List<User> findAll() {
        String sqlQuery = "SELECT * FROM USERS";
        List<User> userList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            User user = new User();
            Map<Integer, FriendStatus> friends = new HashMap<>();
            user.setId(rs.getInt("USER_ID"));
            user.setEmail(rs.getString("USER_EMAIL"));
            user.setLogin(rs.getString("USER_LOGIN"));
            user.setName(rs.getString("USER_NAME"));
            user.setBirthday(rs.getDate("BIRTHDAY").toLocalDate());
            SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                    "SELECT * FROM FRIENDS WHERE USER_ID = ?", user.getId());
            while (userRows.next()) {
                friends.put(userRows.getInt("FRIEND_ID"),
                        FriendStatus.valueOf(userRows.getString("FRIEND_STATUS")));
            }
            user.setFriends(friends);
            return user;
        });
        log.debug("Текущее количество пользователей: {}", userList.size());
        return userList;
    }

    /**
     * Создание пользователя
     */
    public User create(User user) {
        Integer maxCounter = jdbcTemplate.queryForObject("SELECT MAX(USER_ID) FROM USERS", Integer.class);
        if (maxCounter != null) {
            idCounter = maxCounter;
        }
        user.setId(++idCounter);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        String sqlQuery = "INSERT INTO USERS(USER_ID, USER_EMAIL, USER_LOGIN, USER_NAME, BIRTHDAY) " +
                "values (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sqlQuery,
                user.getId(),
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());
        log.debug("Пользователь к сохранению: {}", user);
        return user;
    }

    /**
     * Обновление пользователя
     */
    public User update(User user) {
        String sqlQuery1 = "SELECT COUNT(*) FROM USERS WHERE USER_ID = ?";
        int count = jdbcTemplate.queryForObject(sqlQuery1, Integer.class, user.getId());
        if (count == 0) {
            throw new UserNotFoundException(
                    String.format("Пользователя с id = %d не найдено", user.getId()));
        } else {
            String sqlQuery = "UPDATE USERS SET " +
                    "USER_EMAIL = ?, USER_LOGIN = ?, USER_NAME = ?, BIRTHDAY = ? " +
                    "WHERE USER_ID = ?";
            jdbcTemplate.update(sqlQuery,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday().toString(),
                    user.getId());
            log.debug("Пользователь к обновлению: {}", user);
        }
        return user;
    }

    /**
     * Получение пользователя по ID
     */
    public User findUserById(Integer id) {
        String sqlQuery = "SELECT * FROM USERS WHERE USER_ID = " + id;
        List<User> userList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getInt("USER_ID"));
            user.setEmail(rs.getString("USER_EMAIL"));
            user.setLogin(rs.getString("USER_LOGIN"));
            user.setName(rs.getString("USER_NAME"));
            user.setBirthday(rs.getDate("BIRTHDAY").toLocalDate());
            return user;
        });
        if (userList.isEmpty()) {
            throw new UserNotFoundException(String.format("Пользователя с id = %d не найдено", id));
        } else {
            return userList.get(0);
        }
    }

    /**
     * Добавление в друзья
     */
    public User addFriend(User user, User friend) {
        String sqlQuery1 = "SELECT COUNT(*) FROM FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?";
        Integer count = jdbcTemplate.queryForObject(
                sqlQuery1, Integer.class, user.getId(), friend.getId());
        if (count > 0) {
            throw new IllegalStateException("Заявка в друзья уже существует");
        }
        String sqlQuery = "INSERT INTO FRIENDS(USER_ID, FRIEND_ID, FRIEND_STATUS) " +
                "VALUES (?, ?, ?)";
        jdbcTemplate.update(sqlQuery, user.getId(),
                friend.getId(), FriendStatus.НЕПОДТВЕРЖДЕННАЯ.toString());
        log.debug("Добавление в друзья пользователя с id {}", friend);
        friend.getFriends().put(user.getId(), FriendStatus.НЕПОДТВЕРЖДЕННАЯ);
        return user;
    }

    /**
     * Удаление из друзей
     */
    public User deleteFriend(User user, User friend) {
        String sqlQuery = "DELETE FROM FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?";
        int rowsUpdated = jdbcTemplate.update(sqlQuery, user.getId(), friend.getId());
        jdbcTemplate.update(sqlQuery, friend.getId(), user.getId());
        if (rowsUpdated != 1) {
            throw new RuntimeException("Ошибка удаления из друзей");
        }
        log.debug("Удаление из друзей пользователя c id {}", friend);
        user.getFriends().remove(friend.getId());
        return user;
    }

    /**
     * Получение всех друзей
     */
    public List<User> getFriends(User user) {
        String sqlQuery = "SELECT FRIEND_ID FROM FRIENDS WHERE USER_ID = ?";
        SqlRowSet userRows = jdbcTemplate.queryForRowSet(sqlQuery, user.getId());
        List<User> friends = new ArrayList<>();
        while (userRows.next()) {
            friends.add(findUserById(userRows.getInt("FRIEND_ID")));
        }
        log.debug("Получение списка друзей {}", friends);
        return friends;
    }

    /**
     * Получение общих друзей
     */
    public List<User> getCommonFriends(User user1, User user2) {
        List<User> commonFriends = new ArrayList<>();
        String sqlQuery = "SELECT F.FRIEND_ID, F.FRIEND_STATUS " +
                "FROM FRIENDS F " +
                "INNER JOIN FRIENDS FW ON F.FRIEND_ID = FW.FRIEND_ID " +
                "WHERE F.USER_ID = ? AND FW.USER_ID = ?";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sqlQuery, user1.getId(), user2.getId());
        while (rowSet.next()) {
            commonFriends.add(findUserById(rowSet.getInt("FRIEND_ID")));
        }
        log.debug("Список общих друзей пользователей {} и ", user1, " {}", user2, commonFriends);
        return commonFriends;
    }
}