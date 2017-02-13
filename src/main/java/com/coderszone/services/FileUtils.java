package com.coderszone.services;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.coderszone.exceptions.NotMoviePosterException;


public class FileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger( FileUtils.class );


	private FileUtils() {
	}


	public static boolean getMoviePoster( String url, String imagePath ) throws NotMoviePosterException {

		boolean result = false;
		URL urlImage;

		try {

			File image = new File( imagePath );

			if ( !image.exists() ) {

				urlImage = new URL( url );

				downloadMoviePoster( urlImage, imagePath );
			}

			result = true;
		}
		catch ( MalformedURLException malformedURLException ) {

			LOGGER.error( "Error bad movie poster URL {}", url );

			throw new NotMoviePosterException( malformedURLException );
		}
		catch ( IOException ioException ) {

			LOGGER.error( "Error downloading image {}", url );

			throw new NotMoviePosterException( ioException );
		}

		return result;
	}


	private static void downloadMoviePoster( URL url, String imagePath ) throws IOException {

		try ( InputStream in = new BufferedInputStream( url.openStream() ); OutputStream out = new BufferedOutputStream( new FileOutputStream( imagePath ) ); ) {

			IOUtils.copy( in, out );
		}
		catch ( IOException ioException ) {

			throw ioException;
		}
	}
}
