package com.coderszone.views;


import java.io.File;
import java.util.List;

import org.springframework.data.annotation.Transient;

import com.coderszone.model.Movie;
import com.coderszone.services.MovieService;
import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.Align;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;


@SuppressWarnings( "serial" )
public class SearchMovieView extends Window {

	private static final String COLUMN_ID = "Id";
	private static final String COLUMN_IMAGE = "Image";
	private static final String COLUMN_TITLE = "Title";
	private static final String COLUMN_RATING = "Rating";
	private static final String COLUMN_YEAR = "Year";
	private static final String COLUMN_POSTER = "Poster";

	private String postersPath;
	@Transient
	private MovieService movieService;


	public SearchMovieView() {

		setIcon( FontAwesome.VIDEO_CAMERA );
		setCaption( " Search movies" );
		setClosable( true );
		setResizable( false );
		setWidth( "75%" );
		setHeight( "75%" );
		setModal( true );
		center();

		Container container = new IndexedContainer();
		container.addContainerProperty( COLUMN_ID, String.class, "" );
		container.addContainerProperty( COLUMN_IMAGE, Image.class, null );
		container.addContainerProperty( COLUMN_TITLE, String.class, "" );
		container.addContainerProperty( COLUMN_RATING, String.class, "" );
		container.addContainerProperty( COLUMN_YEAR, String.class, "" );
		container.addContainerProperty( COLUMN_POSTER, String.class, "" );

		Table moviesTbl = new Table();
		moviesTbl.setSizeFull();
		moviesTbl.setContainerDataSource( container );
		moviesTbl.setColumnWidth( COLUMN_IMAGE, 110 );
		moviesTbl.setColumnWidth( COLUMN_RATING, 110 );
		moviesTbl.setColumnWidth( COLUMN_YEAR, 110 );
		moviesTbl.setColumnAlignments( Align.CENTER, Align.CENTER, Align.LEFT, Align.CENTER, Align.CENTER, Align.LEFT );
		moviesTbl.setSelectable( true );
		moviesTbl.setImmediate( true );
		moviesTbl.setColumnCollapsingAllowed( true );
		moviesTbl.setColumnCollapsed( COLUMN_ID, true );
		moviesTbl.setColumnCollapsed( COLUMN_POSTER, true );
		moviesTbl.addValueChangeListener( event -> movieClick( moviesTbl ) );
		
		TextField searchTxt = new TextField();
		searchTxt.setInputPrompt( "Enter a movie to search" );
		searchTxt.setWidth( "100%" );
		searchTxt.setRequired( true );
		searchTxt.addValidator( new StringLengthValidator( "Cannot be empty", 1, null, false ) );
		searchTxt.focus();

		Button searchBtn = new Button( "Search" );
		searchBtn.setWidth( "100%" );
		searchBtn.setDescription( "Search a movie with the words" );
		searchBtn.addStyleName( ValoTheme.BUTTON_PRIMARY );
		searchBtn.setClickShortcut( KeyCode.ENTER, null );
		searchBtn.addClickListener( event -> loadMovies( moviesTbl, searchTxt ) );
		searchBtn.setErrorHandler( event -> Notification.show( "Searching error" ) );

		Button closeBtn = new Button( "Close" );
		closeBtn.setWidth( "100%" );
		closeBtn.setClickShortcut( KeyCode.ESCAPE, null );
		closeBtn.addClickListener( event -> close() );

		HorizontalLayout searchBar = new HorizontalLayout();
		searchBar.setWidth( "100%" );
		searchBar.setSpacing( true );
		searchBar.setMargin( false );
		searchBar.addComponent( searchTxt );
		searchBar.addComponent( searchBtn );
		searchBar.addComponent( closeBtn );
		searchBar.setExpandRatio( searchTxt, 4f );
		searchBar.setExpandRatio( searchBtn, 1f );
		searchBar.setExpandRatio( closeBtn, 1f );

		VerticalLayout layout = new VerticalLayout();
		layout.setMargin( true );
		layout.setSpacing( true );
		layout.setWidth( "100%" );
		layout.setHeight( "100%" );
		layout.addComponent( searchBar );
		layout.addComponent( moviesTbl );
		layout.setExpandRatio( moviesTbl, 1f );

		setContent( layout );
	}


	private void movieClick( Table moviesTbl ) {

		Integer selected = (Integer) moviesTbl.getValue();

		if ( selected != null ) {

			String id = (String) moviesTbl.getContainerProperty( selected, COLUMN_ID ).getValue();

			Movie movie = movieService.getMovieById( id );

			UI.getCurrent().addWindow( new ShowMovieView( movie, movieService, postersPath ) );
		}

		moviesTbl.select( null );
		moviesTbl.setValue( null );
	}


	private void loadMovies( Table moviesTbl, TextField searchTxt ) {

		moviesTbl.removeAllItems();

		List<Movie> movies = movieService.searchMovie( searchTxt.getValue() );

		if ( !movies.isEmpty() ) {

			int index = 1;

			for ( Movie movie : movies ) {

				File file = new File( postersPath + movie.getMovieId() );

				if ( !file.exists() ) {

					file = new File( postersPath + "notfound.jpg" );
				}

				Image poster = new Image( null, new FileResource( file ) );

				poster.setWidth( "100px" );
				poster.setHeight( "148px" );

				// @formatter:off
				moviesTbl.addItem( new Object[] {
						movie.getMovieId(),
						poster,
						movie.getTitle(),
						movie.getRating(),
						movie.getYear(),
						movie.getPoster() }, index++ );
				// @formatter:on
			}
		}
		else {

			Notification.show( "Not movies found" );
		}
	}


	public void setPostersPath( String postersPath ) {

		this.postersPath = postersPath;
	}


	public void setMovieService( MovieService movieService ) {

		this.movieService = movieService;
	}
}
