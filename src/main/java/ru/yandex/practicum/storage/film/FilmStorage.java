package ru.yandex.practicum.storage.film;

import ru.yandex.practicum.model.Film;

import java.util.List;

public interface FilmStorage {
    List<Film> findAll();
    Film add(Film film);
    Film update(Film film);
}
