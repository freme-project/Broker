package eu.freme.broker.eservices;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.stream.MalformedJsonException;

import eu.freme.eservices.epublishing.EPublishingService;
import eu.freme.eservices.epublishing.exception.EPubCreationException;
import eu.freme.eservices.epublishing.exception.InvalidZipException;
import eu.freme.eservices.epublishing.exception.MissingMetadataException;
import eu.freme.eservices.epublishing.webservice.Metadata;

/**
 *
 * @author Pieter Heyvaert <pheyvaer.heyvaert@ugent.be>
 */
@RestController
public class EPublishing {

    private static final Logger logger = Logger.getLogger(EPublishing.class.getName());
    private static final long maxUploadSize = 1024 * 1024 * 200;

    @Autowired
    EPublishingService entityAPI;

    @RequestMapping(value = "/e-publishing/html", method = RequestMethod.POST)
    public ResponseEntity<byte[]> htmlToEPub(@RequestParam("htmlZip") MultipartFile file, @RequestParam("metadata") String jMetadata) throws InvalidZipException, EPubCreationException, IOException, MissingMetadataException {

        if (file.getSize() > maxUploadSize) {
            double size = maxUploadSize / (1024.0 * 1024);
            return new ResponseEntity<>(new byte[0], HttpStatus.BAD_REQUEST);
            //throw new BadRequestException(String.format("The uploaded file is too large. The maximum file size for uploads is %.2f MB", size));
        }

        Gson gson = new Gson();
        Metadata metadata = gson.fromJson(jMetadata, Metadata.class);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "application/epub+zip");
        try {
            return new ResponseEntity<>(entityAPI.createEPUB(metadata, file.getInputStream()), HttpStatus.OK);
        } catch (InvalidZipException | EPubCreationException | IOException | MissingMetadataException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
            throw ex;
        }
    }
}
