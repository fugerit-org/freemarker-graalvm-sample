package org.fugerit.java.demo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@Path("/freemarker")
public class FreeMarkerResource {

    private static final Logger log = LoggerFactory.getLogger( FreeMarkerResource.class );

    /* data model */
    @RegisterForReflection
    public class Data {
        private String description;
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
    }

    private void handleTemplate(Writer writer, String templatePath, Map<String, Object> dataModel) throws IOException, TemplateException {
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_34 );
        cfg.setClassForTemplateLoading( FreeMarkerResource.class, "/templates" );
        Template template = cfg.getTemplate( templatePath );
        template.process( dataModel, writer );
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    @Path("/sample.xml")
    public String runSample() {
        try ( StringWriter writer = new StringWriter() ) {
            Map<String, Object> dataModel = new HashMap<>();
            Data data = new Data();
            data.setDescription( "FreeMarkerGraalVMSample Quarkus" );
            dataModel.put("data", data);
            handleTemplate( writer, "sample.ftl", dataModel );
            return writer.toString();
        } catch (Exception e) {
            String errorMessage = "error : %s".formatted( e );
            log.error(errorMessage, e );
            throw new WebApplicationException( errorMessage );
        }
    }
}
