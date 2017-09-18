package com.coderszone.exceptions;


@SuppressWarnings( "serial" )
public class NotMoviePosterException extends Exception {

	public NotMoviePosterException( Exception exception ) {

		super( exception );
	}
}
