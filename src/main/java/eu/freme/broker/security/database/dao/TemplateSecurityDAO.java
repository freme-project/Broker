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

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import eu.freme.broker.security.database.model.Template;
import eu.freme.broker.security.database.repository.TemplateRepository;
import eu.freme.eservices.elink.exceptions.TemplateNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Created by Arne on 18.09.2015.
 */
@Component
public class TemplateSecurityDAO extends OwnedResourceDAO<Template> {

    private int maxId = 0;

    public void refreshMaxId(){
        for(Template template: repository.findAll()){
            int currentId = Integer.parseInt(template.getId());
            if(currentId > maxId)
                maxId = currentId;
        }
    }

    public String getMaxId(){
        return maxId+"";
    }

    public void save(Template template){
        // is it a new one?
        if(template.getId()== null){
            refreshMaxId();
            maxId++;
            template.setId(maxId+"");
        }
        super.save(template);
    }

}
