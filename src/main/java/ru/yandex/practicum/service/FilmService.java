package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.Film;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервисный класс фильмов Filmorate
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    /**
     * Установка лайка фильму
     */
    public Film putLike(Film film, Integer id) {
        log.debug("Добавление лайка фильму {} ", film);
        film.getLikes().add(id);
        return film;
    }

    /**
     * Удаление лайка фильма
     */
    public Film deleteLike(Film film, Integer id) {
        log.debug("Удаление лайка у фильма {}", film);
        Set<Integer> likes = film.getLikes();
        if (film != null && likes.contains(id)) {
            film.getLikes().remove(id);
        } else {
            log.warn("Лайк с таким id не найден");
            throw new RuntimeException("Лайк с таким id не найден");
        }
        return film;
    }

    /**
     * Получение популярных фильмов
     */
    public List<Film> findBest(Integer count, List<Film> all) {
        log.debug("Получение {} популярных фильмов", count);
        return all.stream()
                .sorted((a, b) -> b.getLikes().size() - a.getLikes().size())
                .limit(count)
                .collect(Collectors.toList());
    }
}
