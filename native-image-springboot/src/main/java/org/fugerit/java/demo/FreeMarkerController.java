package org.fugerit.java.demo;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/freemarker")
public class FreeMarkerController {

    private static final Logger log = LoggerFactory.getLogger(FreeMarkerController.class);

    // data model
    public class Data {
        private String description;
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    private void handleTemplate(Writer writer, String templatePath, Map<String, Object> dataModel) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassForTemplateLoading(FreeMarkerController.class, "/templates");
        Template template = cfg.getTemplate(templatePath);
        template.process(dataModel, writer);
    }

    @GetMapping(value = "/sample.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public String runSample() {
        try (StringWriter writer = new StringWriter()) {
            Map<String, Object> dataModel = new HashMap<>();
            Data data = new Data();
            data.setDescription("FreeMarkerGraalVMSample SpringBoot");
            dataModel.put("data", data);
            handleTemplate(writer, "sample.ftl", dataModel);
            return writer.toString();
        } catch (Exception e) {
            String errorMessage = "error : %s".formatted(e.getMessage());
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

}
