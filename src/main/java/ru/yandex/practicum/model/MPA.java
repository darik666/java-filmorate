package ru.yandex.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Класс рейтингов Filmorate
 */
@Data
@AllArgsConstructor
public class MPA {
    private int id;
    private String name;

    public MPA() {
    }
}