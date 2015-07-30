package eu.freme.broker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import org.junit.Test;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

public class Swagger2MarkupTest  {

    @Test
    public void convertLocalSwaggerYamlToAsciiDoc() throws IOException {
        // Remote Swagger source
        // Default is AsciiDoc
        File swaggerYamlFile = new File("src/docs/swagger/swagger.yaml");
        ObjectMapper m = new ObjectMapper();
        ObjectMapper ym = new ObjectMapper(new YAMLFactory());
        JsonNode rootNode = ym.readValue(swaggerYamlFile, JsonNode.class);
        String swaggerJson = m.writeValueAsString(rootNode);

        Swagger2MarkupConverter.fromString(swaggerJson).build()
                .intoFolder("src/docs/asciidoc/generated");

        // Then validate that three AsciiDoc files have been created
        //String[] files = new File("src/docs/asciidoc/generated").list();
        //assertThat(files).hasSize(3)
        //        .containsAll(Arrays.asList("definitions.adoc", "overview.adoc", "paths.adoc"));
    }


}
