package eu.freme.broker.security.database.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.freme.broker.security.database.model.Dataset;
import eu.freme.broker.security.database.model.Template;
import eu.freme.broker.security.database.model.Token;
import eu.freme.broker.security.database.model.User;
import eu.freme.broker.security.database.repository.DatasetRepository;
import eu.freme.broker.security.database.repository.TemplateRepository;
import eu.freme.broker.security.database.repository.TokenRepository;
import eu.freme.broker.security.database.repository.UserRepository;

@Component
public class UserDAO {
	
	@Autowired
	TokenRepository tokenRepository;
	
	@Autowired
	DatasetRepository datasetRepository;
	
	@Autowired
	TemplateRepository templateRepository;
	
	@Autowired
	UserRepository userRepository;
	
	/**
	 * Delete a user and all its tokens, datasets and templates.
	 * 
	 * @param user
	 */
	public void deleteUser(User user){
		
		for( Token token : user.getTokens() ){
			tokenRepository.delete(token);
		}
		
		for( Dataset dataset : user.getDatasets() ){
			datasetRepository.delete(dataset);
		}
		
		userRepository.delete(user);		
	}
}
