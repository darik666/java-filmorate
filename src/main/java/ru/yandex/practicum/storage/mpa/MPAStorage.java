package ru.yandex.practicum.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.MPA;

import java.util.List;

/**
 * Хранилище рейтингов Filmorate в базе данных
 */
@Slf4j
@Repository("mpaDbStorage")
@RequiredArgsConstructor
public class MPAStorage {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Получение рейтинга по id
     */
    public MPA getById(Integer id) {
        String sqlQuery = "SELECT * FROM MPA WHERE MPA_ID = " + id;
        List<MPA> mpaList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            MPA mpa = new MPA();
            mpa.setId(rs.getInt("MPA_ID"));
            mpa.setName(rs.getString("MPA_NAME"));
            return mpa;
        });
        if (mpaList.isEmpty()) {
            log.warn("Рейтинга с таким id не найдено");
            throw new RuntimeException(String.format("Рейтинг с id = %d не найден", id));
        }
        return mpaList.get(0);
    }

    /**
     * Получение всех рейтингов
     */
    public List<MPA> getAll() {
        String sqlQuery = "SELECT * FROM MPA";
        List<MPA> mpaList = jdbcTemplate.query(sqlQuery, (rs, rowNum) -> {
            MPA mpa = new MPA();
            mpa.setId(rs.getInt("MPA_ID"));
            mpa.setName(rs.getString("MPA_NAME"));
            return mpa;
        });
        log.debug("Текущее количество рейтингов: {}", mpaList.size());
        return mpaList;
    }
}
