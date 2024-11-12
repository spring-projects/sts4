package org.test.jakarta;

import org.test.MovieFinder;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.stereotype.Component;

@Component
public class SimpleMovieLister {

	@Resource
	private MovieFinder movieFinder;

	@Resource
	public void setMovieFinderWithResource(MovieFinder movieFinder) {
		this.movieFinder = movieFinder;
	}
	
	@Resource(name="myMovieFinder") 
	public void setMovieFinderWithSpecificResource(MovieFinder movieFinder) {
		this.movieFinder = movieFinder;
	}

	@Inject
	public void setMovieFinderWithInject(@Named("specificFinder") MovieFinder movieFinder) {
		this.movieFinder = movieFinder;
	}
	
}
