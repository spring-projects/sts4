package org.test.javax;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.test.MovieFinder;

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
