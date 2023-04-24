package ru.yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Класс жанров Filmorate
 */
@Data
@AllArgsConstructor
public class Genre  implements Comparable<Genre> {
    private int id;
    private String name;

    public Genre() {
    }

    @Override
    public int compareTo(Genre o) {
        return this.getId() - o.getId();
    }
}