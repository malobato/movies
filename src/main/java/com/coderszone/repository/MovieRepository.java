package com.coderszone.repository;


import org.springframework.data.mongodb.repository.MongoRepository;

import com.coderszone.model.Movie;


public interface MovieRepository extends MongoRepository<Movie, String> {

}
