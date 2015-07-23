package eu.freme.broker.eservices;

import com.google.gson.Gson;
import com.google.gson.stream.MalformedJsonException;
import eu.freme.broker.exception.BadRequestException;
import eu.freme.eservices.epublishing.EPublishingService;
import eu.freme.eservices.epublishing.exception.EPubCreationException;
import eu.freme.eservices.epublishing.exception.InvalidZipException;
import eu.freme.eservices.epublishing.webservice.Metadata;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.MultipartConfig;

import io.swagger.annotations.*;
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
@Api("e-Publishing")
@RestController
public class EPublishing {

    private static final Logger logger = Logger.getLogger(EPublishing.class.getName());
    private static final long maxUploadSize = 1024 * 1024 * 200;

    @Autowired
    EPublishingService entityAPI;


    @ApiOperation(value = "Create eBooks in the EPUB3 format")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful response"),
            @ApiResponse(code = 400, message = "Bad request - input validation failed") })
    @RequestMapping(value = "/e-publishing/html",
            method = RequestMethod.POST,
           // consumes = "application/zip",
            consumes = "multipart/form-data",
            produces = "application/epub+zip")
    public ResponseEntity<byte[]> htmlToEPub(
            @ApiParam(name="htmlZip", value="The html zip file to generate the epub from.") @RequestParam(value="htmlZip") MultipartFile file,
            @ApiParam(name="metadata", value= "The Metadata JSON to build the EPUB as specified above") @RequestParam(value="metadata") String jMetadata) {
        if (file.getSize() > maxUploadSize) {
            double size = maxUploadSize / (1024.0 * 1024);
            return new ResponseEntity<>(new byte[0], HttpStatus.BAD_REQUEST);
            //throw new BadRequestException(String.format("The uploaded file is too large. The maximum file size for uploads is %.2f MB", size));
        }

        try {
            Gson gson = new Gson();
            Metadata metadata = gson.fromJson(jMetadata, Metadata.class);
            return new ResponseEntity<>(entityAPI.createEPUB(metadata, file.getInputStream()), HttpStatus.OK);
        } catch (MalformedJsonException | InvalidZipException | EPubCreationException ex) {
            logger.log(Level.SEVERE, null, ex);
            return new ResponseEntity<>(new byte[0], HttpStatus.BAD_REQUEST);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return new ResponseEntity<>(new byte[0], HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
