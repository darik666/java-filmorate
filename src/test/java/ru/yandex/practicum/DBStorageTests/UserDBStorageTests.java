package ru.yandex.practicum.DBStorageTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.exception.UserNotFoundException;
import ru.yandex.practicum.model.User;
import ru.yandex.practicum.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserDBStorageTests {
    private final UserStorage storage;
    private final JdbcTemplate jdbcTemplate;

    public UserDBStorageTests(@Qualifier("userDbStorage") UserStorage storage,
                                     @Autowired JdbcTemplate jdbcTemplate) {
        this.storage = storage;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    public void createTestUser() {
        storage.create(giveTwister());
    }

    public User giveTwister() {
        User user = new User();
        user.setName("Mister Abraham");
        user.setLogin("Twister");
        user.setEmail("twisted@twist.com");
        user.setBirthday(LocalDate.of(1978, 10, 12));
        return user;
    }

    public User giveJohnny() {
        User user = new User();
        user.setName("John Smith");
        user.setLogin("johnny");
        user.setEmail("js666@blabla.com");
        user.setBirthday(LocalDate.of(1981, 06, 05));
        return user;
    }

    public User giveTerminator() {
        User user = new User();
        user.setName("Sarah Connor");
        user.setLogin("terminator");
        user.setEmail("illbeback@boom.com");
        user.setBirthday(LocalDate.of(1966, 11, 11));
        return user;
    }

    @Test
    public void createUserTest() {
        User user = giveTerminator();
        user.setName(null);
        storage.create(user);
        assertThat(user)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("name", "terminator")
                .hasFieldOrPropertyWithValue("email", "illbeback@boom.com");
    }

    @Test
    public void updateUserTest() {
        User user = giveTwister();
        user.setId(1);
        user.setName("Galileo");
        user.setEmail("testing@email.com");
        storage.update(user);
        assertThat(storage.findUserById(1))
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Galileo")
                .hasFieldOrPropertyWithValue("email", "testing@email.com");
    }

    @Test
    public void updateUserNotFoundTest() {
        User user = giveTwister();
        user.setId(10);
        Throwable thrown = catchThrowable(() -> {
            storage.update(user);
        });
        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
        assertThat(thrown.getMessage())
                .isEqualTo(String.format("Пользователя с id = %d не найдено", user.getId()));
    }

    @Test
    public void getAllUsersTest() {
        storage.create(giveJohnny());
        storage.create(giveTerminator());
        List<User> users = storage.findAll();

        assertEquals(users.size(), 3);
        assertThat(users.containsAll(List.of(giveTwister(), giveJohnny(), giveTerminator())));
    }

    @Test
    public void testFindUserById() {
        Optional<User> userOptional = Optional.ofNullable(storage.findUserById(1));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("name", "Mister Abraham")
                                .hasFieldOrPropertyWithValue("email", "twisted@twist.com")
                );
    }

    @Test
    public void testFindUserByWrongId() {
        Throwable thrown = catchThrowable(() -> {
            storage.findUserById(2);
        });
        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
        assertThat(thrown.getMessage()).isEqualTo("Пользователя с id = 2 не найдено");
    }

    @Test
    public void addFriendTest() {
        User user = storage.create(giveTwister());
        User friend = storage.create(giveTerminator());
        storage.addFriend(user, friend);
        List<User> friendList = storage.getFriends(user);

        assertEquals(1, friendList.size());
        assertEquals(3, friendList.get(0).getId());
    }

    @Test
    public void addAlreadyFriendTest() {
        User user = storage.create(giveTwister());
        User friend = storage.create(giveTerminator());
        storage.addFriend(user, friend);

        Throwable thrown = catchThrowable(() -> {
            storage.addFriend(user, friend);
        });
        assertThat(thrown).isInstanceOf(InvalidDataAccessApiUsageException.class);
        assertThat(thrown.getMessage()).contains("Заявка в друзья уже существует");
    }

    @Test
    public void deleteFriendTest() {
        User user = storage.create(giveTwister());
        User friend = storage.create(giveTerminator());
        storage.addFriend(user, friend);
        storage.deleteFriend(user, friend);
        List<User> friendList = storage.getFriends(user);

        assertTrue(friendList.isEmpty());

        Throwable thrown = catchThrowable(() -> {
            storage.deleteFriend(user, friend);
        });

        assertThat(thrown).isInstanceOf(RuntimeException.class);
        assertThat(thrown.getMessage()).contains("Ошибка удаления из друзей");
    }

    @Test
    public void getFriendsTest() {
        User user = storage.create(giveTwister());
        User friend = storage.create(giveTerminator());
        storage.addFriend(user, friend);
        List<User> friendList = storage.getFriends(user);

        assertThat(friendList.size()).isEqualTo(1);
        assertThat(friendList.get(0)).hasFieldOrPropertyWithValue("id", 3)
                .hasFieldOrPropertyWithValue("name", "Sarah Connor");
    }

    @Test
    public void getCommonFriendsTest() {
        User user = storage.create(giveTwister());
        User friend = storage.create(giveTerminator());
        storage.addFriend(user, storage.findUserById(1));
        storage.addFriend(friend, storage.findUserById(1));
        List<User> commonFriendList = storage.getCommonFriends(storage.findAll().get(1), friend);

        assertThat(commonFriendList.size()).isEqualTo(1);
        assertThat(commonFriendList.get(0)).hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Mister Abraham");
    }
}