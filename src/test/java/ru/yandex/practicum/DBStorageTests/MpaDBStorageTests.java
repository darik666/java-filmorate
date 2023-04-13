package ru.yandex.practicum.DBStorageTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.model.MPA;
import ru.yandex.practicum.storage.mpa.MPAStorage;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MpaDBStorageTests {
    @Autowired
    private final MPAStorage storage;

    @Test
    public void getAllMpaTest() {
        List<MPA> mpaList = storage.getAll();

        assertEquals(5, mpaList.size());
        assertEquals("NC-17", mpaList.get(4).getName());
        assertEquals("PG-13", mpaList.get(2).getName());
    }

    @Test
    public void getMpaByIdTest() {
        MPA mpa = storage.getById(2);

        assertNotNull(mpa);
        assertEquals("PG", mpa.getName());
    }

    @Test
    public void getMpaByInvalidIdTest() {
        Throwable thrown = catchThrowable(() -> {
            storage.getById(9);
        });

        assertThat(thrown).isInstanceOf(RuntimeException.class);
        assertThat(thrown.getMessage()).isEqualTo(String.format("Рейтинг с id = %d не найден", 9));
    }
}
