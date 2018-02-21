package org.test;

import reactor.core.publisher.Mono;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TradingUserRepository extends ReactiveMongoRepository<TradingUser, String> {

	Mono<TradingUser> findByUserName(String userName);

}
