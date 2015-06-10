package eu.freme.broker.eservices;

import com.google.gson.Gson;
import eu.freme.eservices.epublishing.EPublishingService;
import eu.freme.eservices.epublishing.webservice.Metadata;
import java.io.IOException;
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
public class EPublishing {

    @Autowired
    EPublishingService entityAPI;

    @RequestMapping(value = "/e-publishing/html", method = RequestMethod.POST)
    public ResponseEntity<byte[]> htmlToEPub(@RequestParam("htmlZip") MultipartFile file, @RequestParam("metadata") String jMetadata) throws IOException {
        Gson gson = new Gson();
        Metadata metadata = gson.fromJson(jMetadata, Metadata.class);
        return new ResponseEntity<>(entityAPI.createEPUB(metadata, file.getInputStream()), HttpStatus.OK);
    }

}