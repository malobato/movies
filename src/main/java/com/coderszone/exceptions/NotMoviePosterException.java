package com.coderszone.exceptions;


@SuppressWarnings( "serial" )
public class NotMoviePosterException extends RuntimeException {

	public NotMoviePosterException( Exception exception ) {

		super( exception );
	}
}
