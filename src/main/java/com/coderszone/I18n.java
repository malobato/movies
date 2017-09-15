package com.coderszone;


import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.StringUtils;

import com.vaadin.spring.annotation.SpringComponent;


@SpringComponent
public class I18n {

	private MessageSource messageSource;


	public String get( String key ) {

		String message = key;
		Object[] args = null;

		try {
			if ( messageSource == null ) {
				setupMessageSource();
			}

			message = messageSource.getMessage( key, args, Locale.getDefault() );

			if ( StringUtils.isEmpty( message ) ) {
				return key;
			}
		}
		catch ( Exception exception ) {
			return key;
		}

		return message;
	}


	private void setupMessageSource() {

		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

		messageSource.setBasename( "locale/messages" );
		messageSource.setDefaultEncoding( "UTF-8" );

		this.messageSource = messageSource;
	}
}
