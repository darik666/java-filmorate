package ru.yandex.practicum.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Класс фильмов Filmorate
 */
@Data
@RequiredArgsConstructor
public class Film {
    private int id;
    @NotBlank
    private String name;
    @Size(min = 1, max = 200)
    private String description;
    private LocalDate releaseDate;
    @Min(1)
    private long duration;
    private Set<Integer> likes = new HashSet<>();
    private Set<Genre> genres = new TreeSet<>();
    private MPA mpa = new MPA();
}