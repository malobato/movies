package com.coderszone.model;


import org.springframework.data.annotation.Id;


public class Movie {

	private String movieId;
	private String title;
	private String year;
	private String duration;
	private String description;
	private String directors;
	private String genres;
	private String rating;
	private String image;
	private String poster;


	public Movie() {
		super();
	}


	@Id
	public String getMovieId() {

		return movieId;
	}


	public void setMovieId( String movieId ) {

		this.movieId = movieId;
	}


	public String getTitle() {

		return title;
	}


	public void setTitle( String title ) {

		this.title = title;
	}


	public String getYear() {

		return year;
	}


	public void setYear( String year ) {

		this.year = year;
	}


	public String getDuration() {

		return duration;
	}


	public void setDuration( String duration ) {

		this.duration = duration;
	}


	public String getDescription() {

		return description;
	}


	public void setDescription( String description ) {

		this.description = description;
	}


	public String getDirectors() {

		return directors;
	}


	public void setDirectors( String directors ) {

		this.directors = directors;
	}


	public String getGenres() {

		return genres;
	}


	public void setGenres( String genres ) {

		this.genres = genres;
	}


	public String getRating() {

		return rating;
	}


	public void setRating( String rating ) {

		this.rating = rating;
	}


	public String getImage() {

		return image;
	}


	public void setImage( String image ) {

		this.image = image;
	}


	public String getPoster() {

		return poster;
	}


	public void setPoster( String poster ) {

		this.poster = poster;
	}


	@Override
	public String toString() {

		return "Movie [movieId=" + movieId + ", title=" + title + "]";
	}

}
