package com.coderszone.views;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Transient;

import com.coderszone.I18n;
import com.coderszone.model.Movie;
import com.coderszone.services.MovieService;
import com.vaadin.annotations.Theme;
import com.vaadin.data.sort.Sort;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Grid.SingleSelectionModel;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;


@SuppressWarnings( "serial" )
@SpringUI
@Theme( "valo" )
public class MoviesView extends UI {

	private static final String TITLE = "title";
	private static final String YEAR = "year";
	private static final String RATING = "rating";
	private static final String DURATION = "duration";
	private static final String DIRECTORS = "directors";
	private static final String GENRES = "genres";

	@Value( "${POSTERS_PATH}" )
	private String postersPath;

	@Autowired
	@Transient
	private MovieService movieService;

	@Autowired
	private I18n i18n;


	@Override
	protected void init( VaadinRequest request ) {

		Label title = new Label();
		title.setCaptionAsHtml( true );
		title.setCaption( "<h1>" + FontAwesome.VIDEO_CAMERA.getHtml() + " " + i18n.get( "movies" ) + "</h1>" );

		TextField searchText = new TextField();

		Grid grid = new Grid();

		loadMovies( searchText, grid );

		grid.setSizeFull();

		grid.removeColumn( "description" );
		grid.removeColumn( "poster" );
		grid.removeColumn( "image" );
		grid.removeColumn( "movieId" );

		grid.setColumnOrder( TITLE, YEAR, RATING, DURATION );

		grid.getColumn( TITLE ).setHeaderCaption( i18n.get( TITLE ) );
		grid.getColumn( YEAR ).setWidth( 100 ).setHeaderCaption( i18n.get( YEAR ) );
		grid.getColumn( RATING ).setWidth( 110 ).setHeaderCaption( i18n.get( RATING ) );
		grid.getColumn( DURATION ).setWidth( 100 ).setHeaderCaption( i18n.get( DURATION ) );
		grid.getColumn( DIRECTORS ).setWidth( 500 ).setHeaderCaption( i18n.get( DIRECTORS ) );
		grid.getColumn( GENRES ).setHeaderCaption( i18n.get( GENRES ) );

		grid.setSortOrder( Sort.by( TITLE, SortDirection.ASCENDING ).build() );

		grid.setSelectionMode( SelectionMode.SINGLE );

		grid.addSelectionListener( selectionEvent -> movieClick( searchText, grid ) );

		searchText.setInputPrompt( i18n.get( "filter_movie" ) );
		searchText.setWidth( "100%" );
		searchText.focus();
		searchText.addTextChangeListener( textChangeEvent -> filterMovieList( textChangeEvent.getText(), grid ) );

		Button searchBtn = new Button( i18n.get( "search" ) );
		searchBtn.setWidthUndefined();
		searchBtn.setStyleName( ValoTheme.BUTTON_PRIMARY );
		searchBtn.setClickShortcut( KeyCode.ENTER, null );
		searchBtn.addClickListener( clickEvent -> searchMovie( searchText, grid ) );

		HorizontalLayout toolBar = new HorizontalLayout();
		toolBar.setWidth( "100%" );
		toolBar.setMargin( false );
		toolBar.setSpacing( true );
		toolBar.addComponent( title );
		toolBar.addComponent( searchText );
		toolBar.addComponent( searchBtn );
		toolBar.setComponentAlignment( title, Alignment.MIDDLE_LEFT );
		toolBar.setComponentAlignment( searchText, Alignment.MIDDLE_CENTER );
		toolBar.setComponentAlignment( searchBtn, Alignment.MIDDLE_RIGHT );

		VerticalLayout container = new VerticalLayout();
		container.setSizeFull();
		container.setMargin( true );
		container.setSpacing( true );
		container.addComponent( toolBar );
		container.addComponent( grid );
		container.setExpandRatio( grid, 1.0f );

		setContent( container );
	}


	private void filterMovieList( String text, Grid grid ) {

		@SuppressWarnings( "unchecked" )
		BeanItemContainer<Movie> container = (BeanItemContainer<Movie>) grid.getContainerDataSource();

		container.removeContainerFilters( TITLE );

		if ( !text.isEmpty() ) {

			container.addContainerFilter( new SimpleStringFilter( TITLE, text, true, false ) );
		}
	}


	private void searchMovie( TextField searchText, Grid grid ) {

		SearchMovieView searchMovieView = new SearchMovieView();

		searchMovieView.setPostersPath( postersPath );
		searchMovieView.setMovieService( movieService );
		searchMovieView.setI18n( i18n );
		searchMovieView.addCloseListener( event -> loadMovies( searchText, grid ) );

		searchMovieView.initComponents();

		UI.getCurrent().addWindow( searchMovieView );
	}


	private void loadMovies( TextField searchText, Grid grid ) {

		List<Movie> movies = movieService.findAll();

		final BeanItemContainer<Movie> dataSource = new BeanItemContainer<>( Movie.class, movies );

		grid.setContainerDataSource( dataSource );

		if ( !searchText.getValue().isEmpty() ) {
			filterMovieList( searchText.getValue(), grid );
		}
	}


	private void movieClick( TextField searchText, Grid grid ) {

		Object selected = ((SingleSelectionModel) grid.getSelectionModel()).getSelectedRow();

		if ( selected != null ) {

			String id = (String) grid.getContainerDataSource().getItem( selected ).getItemProperty( "movieId" ).getValue();

			Movie movie = movieService.getMovieById( id );

			Window window = new ShowMovieView( movie, movieService, i18n, postersPath );

			window.addCloseListener( event -> loadMovies( searchText, grid ) );

			UI.getCurrent().addWindow( window );
		}

		grid.deselectAll();
	}
}
