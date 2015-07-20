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
@Api(value= "e-Publishing")
@RestController
public class EPublishing {

    private static final Logger logger = Logger.getLogger(EPublishing.class.getName());
    private static final long maxUploadSize = 1024 * 1024 * 200;

    @Autowired
    EPublishingService entityAPI;


    @ApiOperation(value = "Create eBooks in the EPUB3 format",
            notes = "Creates an eBook in the EPUB3 format from a zip file containing HTML Files and more (images etc.) and a json file containing all necessary metadata for the creation of the eBook.\n" +
                    "\n" +
                    "**Example Call**\n" +
                    "\n" +
                    "`curl --form \"htmlZip=@alice.zip\" --form metadata='json-string' http://api-dev.freme-project.eu/current/e-publishing/html`\n" +
                    "\n" +
                    "**Metadata JSON may include the following**\n" +
                    "* `titles` - a list of Strings where each String represents one title\n" +
                    "* `authors` - a list of Strings where each String represents one author\n" +
                    "* `illustrators` - a list of Strings where each String represents one illustrator\n" +
                    "* `creators` - a list of Strings where each String represents one creator\n" +
                    "* `subjects` - a list of Strings where each String represents one subject\n" +
                    "* `coverImage` - the location of the cover image inside the zip file\n" +
                    "* `language` - the language of the content (e.g., 'en')\n" +
                    "* `source` - the original source of the content\n" +
                    "* `description` - the description of the EPUB\n" +
                    "* `rights` - the rights applicable to the EPUB\n" +
                    "* `identifier` - the identifier is represented by 'value', 'scheme' is optional and represents the used scheme corresponding with the value.\n" +
                    "* `tableOfContents` - it is an ordered list of the chapters/sections in the EPUB. For each you provide the title and the corresponding HTML file (= resource). If no tableOfContents is provided, the service will do a best effort at creating one. However, when no (x)html file is found in the root of the zip, the service will return an invalid EPUB.\n" +
                    "\n" +
                    "**Sample .zips**\n" +
                    "* [Alice in wonderland](https://drive.google.com/open?id=0B-qMtkPK-unYbVROT1J2TTRycDg&authuser=0)\n" +
                    "* [A shared culture](https://drive.google.com/open?id=0B-qMtkPK-unYdWlHTWMyS2VaV28&authuser=0)")
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
            @RequestParam(value="metadata") String jMetadata) {
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
