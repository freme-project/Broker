
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
