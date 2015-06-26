package eu.freme.broker.eservices;

import com.google.gson.Gson;
import com.google.gson.stream.MalformedJsonException;
import eu.freme.eservices.epublishing.EPublishingService;
import eu.freme.eservices.epublishing.exception.EPubCreationException;
import eu.freme.eservices.epublishing.exception.InvalidZipException;
import eu.freme.eservices.epublishing.webservice.Metadata;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.MultipartConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Pieter Heyvaert <pheyvaer.heyvaert@ugent.be>
 */
@RestController
@MultipartConfig(
       // fileSizeThreshold   = 1024 * 1024 * 1,  // 1 MB
        maxFileSize         = 1024 * 1024 * 200, // 10 MB
        maxRequestSize      = 1024 * 1024 * 200 // 15 MB
)
public class EPublishing {

    private static final Logger logger = Logger.getLogger(EPublishing.class.getName());

    @Autowired
    EPublishingService entityAPI;

    @RequestMapping(value = "/e-publishing/html", method = RequestMethod.POST)
    public ResponseEntity<byte[]> htmlToEPub(@RequestParam("htmlZip") MultipartFile file, @RequestParam("metadata") String jMetadata) {

        try {
            Gson gson = new Gson();
            Metadata metadata = gson.fromJson(jMetadata, Metadata.class);
            return new ResponseEntity<>(entityAPI.createEPUB(metadata, file.getInputStream()), HttpStatus.OK);
        } catch (MalformedJsonException | InvalidZipException | EPubCreationException ex) {
            logger.log(Level.SEVERE, null, ex);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
