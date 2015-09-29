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

import eu.freme.broker.security.database.model.Dataset;
import org.springframework.stereotype.Component;

/**
 * Created by Arne on 18.09.2015.
 */
@Component
public class DatasetDAO extends OwnedResourceDAO<Dataset> {
    @Override
    public String className() {
        return Dataset.class.getSimpleName();
    }
}
