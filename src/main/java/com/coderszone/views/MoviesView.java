package com.coderszone.views;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Transient;

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

	@Value( "${POSTERS_PATH}" )
	private String postersPath;

	@Autowired
	@Transient
	private MovieService movieService;


	@Override
	protected void init( VaadinRequest request ) {

		Label title = new Label();
		title.setCaptionAsHtml( true );
		title.setCaption( "<h1>" + FontAwesome.VIDEO_CAMERA.getHtml() + " Movies</h1>" );

		TextField searchText = new TextField();

		Grid grid = new Grid();
		loadMovies( searchText, grid );
		grid.setSizeFull();
		grid.removeColumn( "description" );
		grid.removeColumn( "poster" );
		grid.removeColumn( "image" );
		grid.removeColumn( "movieId" );
		grid.setColumnOrder( "title", "year", "rating", "duration" );
		grid.getColumn( "year" ).setWidth( 100 );
		grid.getColumn( "rating" ).setWidth( 100 );
		grid.getColumn( "duration" ).setWidth( 100 );
		grid.getColumn( "directors" ).setWidth( 500 );
		grid.setSortOrder( Sort.by( "title", SortDirection.ASCENDING ).build() );
		grid.setSelectionMode( SelectionMode.SINGLE );
		grid.addSelectionListener( selectionEvent -> movieClick( searchText, grid ) );

		searchText.setInputPrompt( "Filter movie" );
		searchText.setWidth( "100%" );
		searchText.focus();
		searchText.addTextChangeListener( textChangeEvent -> filterMovieList( textChangeEvent.getText(), grid ) );

		Button searchBtn = new Button( "Search" );
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

		container.removeContainerFilters( "title" );

		if ( !text.isEmpty() ) {

			container.addContainerFilter( new SimpleStringFilter( "title", text, true, false ) );
		}
	}


	private void searchMovie( TextField searchText, Grid grid ) {

		SearchMovieView searchMovieView = new SearchMovieView();

		searchMovieView.setPostersPath( postersPath );
		searchMovieView.setMovieService( movieService );
		searchMovieView.addCloseListener( event -> loadMovies( searchText, grid ) );

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

			Window window = new ShowMovieView( movie, movieService, postersPath );

			window.addCloseListener( event -> loadMovies( searchText, grid ) );

			UI.getCurrent().addWindow( window );
		}

		grid.deselectAll();
	}
}
