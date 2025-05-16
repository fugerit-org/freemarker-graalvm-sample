package org.fugerit.java.demo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.helidon.config.Config;
import io.helidon.http.Status;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class FreeMarkerService implements HttpService {


    FreeMarkerService() {
        this(Config.global().get("app"));
    }

    FreeMarkerService(Config appConfig) {
    }

    /**
     * A service registers itself by updating the routing rules.
     *
     * @param rules the routing rules.
     */
    @Override
    public void routing(HttpRules rules) {
        rules
                .get("/sample.xml", this::runSample);
    }

    private void handleTemplate(Writer writer, String templatePath, Map<String, Object> dataModel) throws IOException, TemplateException {
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_34 );
        cfg.setClassForTemplateLoading( FreeMarkerService.class, "/templates" );
        Template template = cfg.getTemplate( templatePath );
        template.process( dataModel, writer );
    }

    /* data model */
    public class Data {
        private String description;
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * Return a worldly greeting message.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void runSample(ServerRequest request,
                                          ServerResponse response) {
        try ( StringWriter writer = new StringWriter() ) {
            Map<String, Object> dataModel = new HashMap<>();
            Data data = new Data();
            data.setDescription( "FreeMarkerGraalVMSample Helidon" );
            dataModel.put("data", data);
            handleTemplate( writer, "sample.ftl", dataModel );
            response.header( "Content-Type", "text/xml; charset=utf-8" );
            response.send( writer.toString() );
        } catch (Exception e) {
            String errorMessage = "error : %s".formatted( e );
            System.out.println( errorMessage );
            e.printStackTrace();
            response.status( Status.INTERNAL_SERVER_ERROR_500 );
        }

    }

}