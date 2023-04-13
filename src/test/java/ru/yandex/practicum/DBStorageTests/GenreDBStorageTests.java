package ru.yandex.practicum.DBStorageTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.model.Genre;
import ru.yandex.practicum.storage.genre.GenreStorage;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GenreDBStorageTests {
    @Autowired
    private final GenreStorage storage;

    @Test
    public void getAllMpaTest() {
        List<Genre> genreList = storage.getAll();

        assertEquals(6, genreList.size());
        assertEquals("Документальный", genreList.get(4).getName());
        assertEquals("Мультфильм", genreList.get(2).getName());
    }

    @Test
    public void getMpaByIdTest() {
        Genre genre = storage.getById(6);

        assertNotNull(genre);
        assertEquals("Боевик", genre.getName());
    }

    @Test
    public void getMpaByInvalidIdTest() {
        Throwable thrown = catchThrowable(() -> {
            storage.getById(9);
        });

        assertThat(thrown).isInstanceOf(RuntimeException.class);
        assertThat(thrown.getMessage()).isEqualTo(String.format("Жанр с id = %d не найден", 9));
    }
}
