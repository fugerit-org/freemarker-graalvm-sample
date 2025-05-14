import freemarker.log.Logger;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class FreeMarkerGraalVMSample {

    private final static Logger LOG = Logger.getLogger(FreeMarkerGraalVMSample.class.getName());

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

    private void handleTemplate(Writer writer, String templatePath, Map<String, Object> dataModel) throws IOException, TemplateException {
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_34 );
        cfg.setClassForTemplateLoading( FreeMarkerGraalVMSample.class, "/templates" );
        Template template = cfg.getTemplate( templatePath );
        template.process( dataModel, writer );
    }

    public void runSample() {
        try ( StringWriter writer = new StringWriter() ) {
            Map<String, Object> dataModel = new HashMap<>();
            Data data = new Data();
            data.setDescription( "FreeMarkerGraalVMSample" );
            dataModel.put("data", data);
            handleTemplate( writer, "sample.ftl", dataModel );
            LOG.info( writer.toString() );
        } catch (Exception e) {
            LOG.error( e.getMessage(), e );
        }
    }

    public static void main(String[] args) {
        FreeMarkerGraalVMSample sample = new FreeMarkerGraalVMSample();
        sample.runSample();
    }

}