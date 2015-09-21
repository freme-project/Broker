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

import eu.freme.conversion.rdf.RDFConstants;

import javax.persistence.*;

/**
 * Created by Jonathan Sauder (jsauder@campus.tu-berlin.de) on 13.08.15.
 */
@Entity
@Table(name = "template")
public class Template extends OwnedResource {
    public Template(String id, User owner, Visibility visibility) {
        super(id, owner, visibility);
    }
    public Template(String id, Visibility visibility) {
        super(id, visibility);
    }
    public Template(){super();}

    private RDFConstants.RDFSerialization serializationtype;

    public RDFConstants.RDFSerialization getSerializationtype() {
        return serializationtype;
    }

    public void setSerializationtype(RDFConstants.RDFSerialization serializationtype) {
        this.serializationtype = serializationtype;
    }
}
