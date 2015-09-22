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
package eu.freme.broker.security.database.model;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import eu.freme.conversion.rdf.RDFConstants;

import javax.persistence.*;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 13.08.15.
 */
@Entity
@Table(name = "template")
public class Template extends OwnedResource {

    @Lob
    private String endpoint;
    @Lob
    private String query;
    @Lob
    private String label;
    @Lob
    private String description;

    // NOTE: constructor without was implemented before...

    public Template(String id, User owner, Visibility visibility, String endpoint, String query, String label, String description) {
        super(id, owner, visibility);

        this.endpoint = endpoint;
        this.query = query;
        this.label = label;
        this.description = description;
    }
    public Template(String id, Visibility visibility, String endpoint, String query, String label, String description) {
        super(id, visibility);
        this.endpoint = endpoint;
        this.query = query;
        this.label = label;
        this.description = description;
    }

    public Template(String id, User owner, Visibility visibility, Model model){
        super(id, owner, visibility);
        setTemplateWithModel(model);
    }

    public Template(String id,Visibility visibility, Model model){
        super(id, visibility);
        setTemplateWithModel(model);
    }

    public Template(){super();}


    public void setTemplateWithModel(Model model){
        model.enterCriticalSection(false);
        try {
            StmtIterator iter = model.listStatements((Resource)null, RDF.type, model.getResource("http://www.freme-project.eu/ns#Template"));

            // take first instance
            if(iter.hasNext()){
                Statement templateRes = iter.nextStatement();
                Resource templRes = templateRes.getSubject();
                endpoint = templRes.getProperty(model.getProperty("http://www.freme-project.eu/ns#endpoint")).getObject().asLiteral().toString();
                query = templRes.getProperty(model.getProperty("http://www.freme-project.eu/ns#query")).getObject().asLiteral().toString();
                label = templRes.getProperty(RDFS.label).getObject().asLiteral().toString();
                description = templRes.getProperty(DCTerms.description).getObject().asLiteral().toString();
            }

        } finally {
            model.leaveCriticalSection();
        }
    }

    public Model getRDF(){
        Model result = ModelFactory.createDefaultModel();
        result.enterCriticalSection(false);

        try {
            Resource resource = result.createResource("http://www.freme-project.eu/data/templates/" + this.getId());
            result.add(resource, RDF.type, result.getResource("http://www.freme-project.eu/ns#Template"));
            result.add(resource, result.getProperty("http://www.freme-project.eu/ns#templateId"), this.getId());
            result.add(resource, result.getProperty("http://www.freme-project.eu/ns#query"), this.getQuery());
            result.add(resource, result.getProperty("http://www.freme-project.eu/ns#endpoint"), this.getEndpoint());
            result.add(resource, RDFS.label, this.getLabel());
            result.add(resource, DCTerms.description, this.getDescription());
        } finally {
            result.leaveCriticalSection();
        }

        return result;
    }

    private RDFConstants.RDFSerialization serializationtype;

    public RDFConstants.RDFSerialization getSerializationtype() {
        return serializationtype;
    }

    public void setSerializationtype(RDFConstants.RDFSerialization serializationtype) {
        this.serializationtype = serializationtype;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
