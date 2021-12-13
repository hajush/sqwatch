package com.sshift.sqwatch

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DatabaseLoader implements CommandLineRunner {

	private final AuthorRepository repository

	@Autowired
	DatabaseLoader(AuthorRepository repository) {
		this.repository = repository
	}

	@Override
	void run(String... strings) throws Exception {
	}

}
