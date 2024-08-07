package com.chary.bhaumik.ratingsdata.controller;

import com.chary.bhaumik.ratingsdata.model.Rating;
import com.chary.bhaumik.ratingsdata.model.UserRating;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping ("/ratings-data")
public class RatingsDataController {
    @GetMapping ("/{movieId}")
    public Rating getMovieInfo(@PathVariable ("movieId") String movieId) {
        return new Rating("One", 9);
    }

    @GetMapping ("/{userId}")
    public List<Rating> getRatingOne(@PathVariable ("userId") String userId) {
        return Arrays.asList(new Rating("Two", 2), new Rating("Three", 3));
    }

    @GetMapping ("/users/{userId}")
    public UserRating getUserRating(@PathVariable ("userId") String userId) {
        UserRating userRating = new UserRating();
        userRating.setUserRatings(Arrays.asList(new Rating("Two", 2), new Rating("Three", 3)));
        return userRating;
    }

}
