/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * 					Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * 					Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
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
package eu.freme.broker.tools;

import eu.freme.broker.exception.InvalidTemplateEndpointException;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
@Controller
public class TemplateValidator {
    
    public TemplateValidator(){
    }
    
    public void validateTemplateEndpoint(String uri) {
        UrlValidator urlValidator = new UrlValidator();
        if(!urlValidator.isValid(uri)) {
            throw new InvalidTemplateEndpointException("The SPARQL endpoint URL \""+uri+"\" is invalid.");
        }
    }
    
}
