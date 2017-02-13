package com.coderszone.services;


import java.util.List;

import com.coderszone.model.Movie;


public interface MovieService {

	public List<Movie> searchMovie( String searchWords );


	public Movie getMovieById( String id );


	public Movie parseMovieData( String url );

	
	public boolean exists( String movieId );
	

	public void save( Movie movie );


	public void delete( Movie movie );


	public List<Movie> findAll();


	public Movie findOne( String movieId );
}
