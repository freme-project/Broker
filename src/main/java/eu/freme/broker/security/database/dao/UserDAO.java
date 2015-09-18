/**
 * Copyright (C) 2015 Deutsches Forschungszentrum für Künstliche Intelligenz (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public class UserDAO extends DAO<UserRepository, User> {
	
	@Autowired
	TokenRepository tokenRepository;
	
	@Autowired
	DatasetRepository datasetRepository;
	
	@Autowired
	TemplateRepository templateRepository;

	public void delete(User entity){
		for( Token token : entity.getTokens() ){
			tokenRepository.delete(token);
		}

		for( Dataset dataset : entity.getDatasets() ){
			datasetRepository.delete(dataset);
		}

		for( Template template : entity.getTemplates()){
			templateRepository.delete(template);
		}
		super.delete(entity);
	}
}
