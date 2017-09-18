package com.coderszone.views;


import java.io.File;

import com.coderszone.model.Movie;
import com.coderszone.services.I18n;
import com.coderszone.services.MovieService;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;


@SuppressWarnings( "serial" )
public class ShowMovieView extends Window {

	private Movie movie;
	private String postersPath;
	private MovieService movieService;
	private I18n i18n;


	public ShowMovieView( Movie movie, MovieService movieService, I18n i18n, String postersPath ) {

		this.movie = movie;
		this.movieService = movieService;
		this.i18n = i18n;
		this.postersPath = postersPath;

		setIcon( FontAwesome.FILM );
		setCaptionAsHtml( true );
		setCaption( "&nbsp;<b>" + movie.getTitle() + "</b>" );
		setClosable( true );
		setResizable( false );
		setWidth( "75%" );
		setHeight( "75%" );
		setModal( true );
		center();

		initComponents();
	}


	private void initComponents() {

		File file = new File( postersPath + movie.getMovieId() + "f" );

		if ( !file.exists() ) {

			file = new File( postersPath + "notfound.jpg" );
		}

		Image poster = new Image( null, new FileResource( file ) );
		poster.setHeight( "100%" );

		Label title = new Label();
		title.setCaptionAsHtml( true );
		title.setCaption( "<h2><b>" + movie.getTitle() + "</b>" );
		title.setSizeFull();

		Label year = new Label();
		year.setCaption( "(" + movie.getYear() + ")" );
		year.setSizeFull();

		Label info = new Label();
		info.setCaptionAsHtml( true );
		//@formatter:off
		info.setCaption(
				FontAwesome.CLOCK_O.getHtml() + " " + movie.getDuration() +
				FontAwesome.STAR_O.getHtml() + " " + movie.getRating() );
		//@formatter:on
		info.setSizeFull();

		Label genres = new Label();
		genres.setCaptionAsHtml( true );
		genres.setCaption( FontAwesome.FILE_MOVIE_O.getHtml() + " " + movie.getGenres() );
		genres.setSizeFull();

		Label directors = new Label();
		directors.setCaptionAsHtml( true );
		directors.setCaption( FontAwesome.GROUP.getHtml() + " " + movie.getDirectors() );
		directors.setSizeFull();

		TextArea description = new TextArea();
		description.setValue( movie.getDescription() );
		description.setReadOnly( true );
		description.setSizeFull();

		Button saveButton = new Button( i18n.get( "save" ) );
		saveButton.addClickListener( clickEvent -> saveMovie() );
		saveButton.setWidthUndefined();

		Button deleteButton = new Button( i18n.get( "delete" ) );
		deleteButton.addClickListener( clickEvent -> deleteMovie() );
		deleteButton.setWidthUndefined();

		HorizontalLayout actions = new HorizontalLayout();
		actions.setSpacing( true );
		actions.setSizeFull();
		actions.addComponent( saveButton );
		actions.addComponent( deleteButton );
		actions.setComponentAlignment( saveButton, Alignment.MIDDLE_RIGHT );
		actions.setComponentAlignment( deleteButton, Alignment.MIDDLE_LEFT );

		VerticalLayout movieData = new VerticalLayout();
		movieData.setSizeFull();
		movieData.addComponent( title );
		movieData.addComponent( year );
		movieData.addComponent( info );
		movieData.addComponent( genres );
		movieData.addComponent( directors );
		movieData.addComponent( description );
		movieData.addComponent( actions );
		movieData.setExpandRatio( title, 0.7f );
		movieData.setExpandRatio( year, 0.5f );
		movieData.setExpandRatio( info, 0.5f );
		movieData.setExpandRatio( genres, 0.5f );
		movieData.setExpandRatio( directors, 0.5f );
		movieData.setExpandRatio( description, 8.0f );
		movieData.setExpandRatio( actions, 0.5f );

		HorizontalLayout layout = new HorizontalLayout();
		layout.setSizeFull();
		layout.setMargin( true );
		layout.addComponent( poster );
		layout.setComponentAlignment( poster, Alignment.MIDDLE_CENTER );
		layout.addComponent( movieData );

		if ( !movieService.exists( movie.getMovieId() ) ) {
			deleteButton.setEnabled( false );
		}
		else {
			saveButton.setEnabled( false );
		}

		setContent( layout );
	}


	private void saveMovie() {

		movieService.save( movie );

		Notification.show( i18n.get( "movie_saved" ), Type.HUMANIZED_MESSAGE );

		close();
	}


	private void deleteMovie() {

		movieService.delete( movie );

		Notification.show( i18n.get( "movie_deleted" ), Type.WARNING_MESSAGE );

		close();
	}
}
