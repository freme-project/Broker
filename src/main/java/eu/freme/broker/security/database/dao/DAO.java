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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TransactionRequiredException;

/**
 * Created by Arne on 18.09.2015.
 */
public class DAO<Repository  extends CrudRepository<Entity, Long>, Entity> {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    Repository repository;

    Logger logger = Logger.getLogger(DAO.class);

    public void delete(Entity entity){
        repository.delete(entity);
        try {
            entityManager.flush();
            entityManager.clear();
        }catch(TransactionRequiredException e){
            logger.warn("Tried to flush and clear the entity manager, but didn't work! ("+e.getMessage()+")");
        }
    }

    public void save(Entity entity){
        repository.save(entity);
        //entityManager.flush();
        //entityManager.clear();
    }

    public long count(){
        return repository.count();
    }

    public Iterable<Entity> findAll(){
        return repository.findAll();
    }

    public Repository getRepository(){
        return repository;
    }


}
