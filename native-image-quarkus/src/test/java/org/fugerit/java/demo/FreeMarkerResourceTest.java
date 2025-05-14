package org.fugerit.java.demo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class FreeMarkerResourceTest {
    @Test
    void testRunSample() {
        given()
          .when().get("/freemarker/sample.xml")
          .then()
             .statusCode(200)
             .body(is("<freemarker-graalvm-sample>\n" +
                     "    <freemarker-version>2.3.35-nightly</freemarker-version>\n" +
                     "    <description>FreeMarkerGraalVMSample Quarkus</description>\n" +
                     "</freemarker-graalvm-sample>"));
    }

}