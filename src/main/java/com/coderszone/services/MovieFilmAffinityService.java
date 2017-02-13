package com.coderszone.services;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.coderszone.exceptions.NotMoviePosterException;
import com.coderszone.model.Movie;
import com.coderszone.repository.MovieRepository;
import com.vaadin.spring.annotation.UIScope;


@Service
@UIScope
public class MovieFilmAffinityService implements MovieService {

	@Value( "${POSTERS_PATH}" )
	private String postersPath;

	@Autowired
	private MovieRepository movieRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger( MovieFilmAffinityService.class );

	private static final String SEARCH_URL = "http://www.filmaffinity.com/es/search.php?stype=all&stext=";
	private static final String NO_IMAGE_FOUND = "/imgs/movies/noimgfull.jpg";
	private static final String IMAGE_NOT_AVAILABLE = "notfound.jpg";


	@Override
	public List<Movie> searchMovie( String searchWords ) {

		List<Movie> moviesLst = null;

		Document search = null;

		try {

			search = Jsoup.connect( SEARCH_URL + searchWords.replace( " ", "+" ) ).get();
		}
		catch ( IOException ioException ) {

			LOGGER.error( "Cannot connect to filmaffinity server {}", ioException );
		}

		if ( search != null ) {

			Elements moviesFound = search.select( "div.z-search" ).select( "div.se-it" );

			// Not found anything?
			if ( moviesFound.size() == 0 ) {

				// Check for a single movie
				moviesLst = getMovie( search );
			}
			else {

				moviesLst = getMoviesSearchInfo( moviesFound );
			}
		}

		return moviesLst;
	}


	private List<Movie> getMovie( Document document ) {

		List<Movie> moviesLst = new ArrayList<>();

		try {

			Movie movie = parseMovie( document );

			if ( movie != null ) {

				moviesLst.add( movie );
			}
		}
		catch ( IOException ioException ) {

			LOGGER.error( "Error parsing movie info. {}", ioException );
		}

		return moviesLst;
	}


	private List<Movie> getMoviesSearchInfo( Elements moviesFound ) {

		List<Movie> moviesLst = new ArrayList<>();

		String lastYear = "";

		for ( Element movieToParse : moviesFound ) {

			Movie movie = parseMovieSearchData( movieToParse );

			// The query returns several films grouped by year so we need to store the year of the first
			if ( movie.getYear().isEmpty() ) {

				movie.setYear( lastYear );
			}
			else {

				lastYear = movie.getYear();
			}

			movie.setImage( getPoster( movie.getImage(), movie.getMovieId() ) );

			moviesLst.add( movie );
		}

		return moviesLst;
	}


	private Movie parseMovieSearchData( Element movieToParse ) {

		String movieId = getElementFirstAttr( movieToParse, "div.movie-card", "data-movie-id" );

		Movie movie = movieRepository.findOne( movieId );

		if ( movie == null ) {

			movie = new Movie();

			movie.setMovieId( movieId );
			movie.setTitle( getElementText( movieToParse, "div.mc-title" ) );
			movie.setPoster( getElementFirstAttr( movieToParse, "div.mc-title, a", "href" ) );
			movie.setYear( getElementText( movieToParse, "div.ye-w" ) );
			movie.setImage( getElementFirstAttr( movieToParse, "div.mc-poster, a, img", "src" ) );

			Elements elements = movieToParse.select( "div.mr-rating" );
			if ( !elements.isEmpty() ) {

				elements = movieToParse.select( "div.avgrat-box" );

				movie.setRating( elements.text() );
			}
		}

		return movie;
	}


	private String getPoster( String url, String movieId ) {

		String poster = IMAGE_NOT_AVAILABLE;

		if ( !url.isEmpty() && url.compareTo( NO_IMAGE_FOUND ) != 0 && FileUtils.getMoviePoster( url, postersPath + movieId ) ) {

			poster = movieId;
		}

		return poster;
	}


	@Override
	public Movie parseMovieData( String url ) {

		Movie movie = new Movie();

		try {

			Document document = Jsoup.connect( url ).get();

			movie = parseMovie( document );
		}
		catch ( IOException ioException ) {

			LOGGER.error( "Error parsing movie info {}{}", ioException.getMessage(), ioException );
		}

		return movie;
	}


	private Movie parseMovie( Document document ) throws IOException {

		Movie movie = null;

		Element movieInfo = document.select( "dl.movie-info" ).first();

		if ( movieInfo != null ) {

			String movieId = getElementFirstAttr( document, "div.rate-movie-box", "data-movie-id" );

			movie = movieRepository.findOne( movieId );

			if ( movie == null ) {

				movie = new Movie();

				movie.setMovieId( movieId );
				movie.setTitle( getElementFirstText( document, "span[itemprop='name']" ) );
				movie.setYear( getElementText( document, "dd[itemprop='datePublished']" ) );
				movie.setDuration( getElementText( document, "dd[itemprop='duration']" ) );
				movie.setRating( getElementFirstText( document, "div#movie-rat-avg" ) );
				movie.setDescription( getElementText( movieInfo, "dd[itemprop='description']" ) );
				movie.setDirectors( getElementText( movieInfo, "dd.directors" ) );
				movie.setGenres( getElementText( movieInfo, "span[itemprop='genre']" ) );

				Elements elements = document.select( "div#movie-main-image-container" );
				if ( !elements.isEmpty() ) {

					parseMovieImages( movie, elements );
				}

				try {

					FileUtils.getMoviePoster( movie.getPoster(), postersPath + movie.getMovieId() );
					FileUtils.getMoviePoster( movie.getImage(), postersPath + movie.getMovieId() + "f" );
				}
				catch ( NotMoviePosterException notMoviePosterException ) {

					LOGGER.info( "{}", notMoviePosterException );
				}

				movie.setPoster( postersPath + movie.getMovieId() + "f" );
			}
		}

		return movie;
	}


	private void parseMovieImages( Movie movie, Elements elements ) {

		Element element = elements.first();
		if ( element != null ) {

			movie.setImage( getElementFirstAttr( element, "a", "href" ) );
			movie.setPoster( getElementFirstAttr( element, "a, img", "src" ) );
		}
	}


	private String getElementText( Element element, String cssQuery ) {

		String result = "";

		Elements elements = element.select( cssQuery );

		if ( !elements.isEmpty() ) {

			result = elements.text();
		}

		return result;
	}


	private String getElementFirstText( Element element, String cssQuery ) {

		String result = "";

		Elements elements = element.select( cssQuery );

		if ( !elements.isEmpty() ) {

			Element firstElement = elements.first();
			if ( firstElement != null ) {

				result = firstElement.text();
			}
		}

		return result;
	}


	private String getElementFirstAttr( Element element, String cssQuery, String attr ) {

		String result = "";

		Elements elements = element.select( cssQuery );

		if ( !elements.isEmpty() ) {

			result = elements.attr( attr );
		}

		return result;
	}


	public Movie getMovieById( String id ) {

		String reference = "/es/film" + id + ".html";

		Movie movie = findOne( id );

		if ( movie == null ) {

			movie = parseMovieData( "http://www.filmaffinity.com" + reference );
		}

		return movie;
	}


	@Override
	public boolean exists( String movieId ) {

		return movieRepository.exists( movieId );
	}
	
	
	@Override
	public void save( Movie movie ) {

		movieRepository.save( movie );
	}


	@Override
	public void delete( Movie movie ) {

		movieRepository.delete( movie );
	}


	@Override
	public Movie findOne( String movieId ) {

		return movieRepository.findOne( movieId );
	}


	@Override
	public List<Movie> findAll() {

		return movieRepository.findAll();
	}
}
