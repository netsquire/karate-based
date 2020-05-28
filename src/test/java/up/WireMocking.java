package up;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class WireMocking {

    private static int PORT = 9090;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    CloseableHttpClient httpClient;

    @Before
    public void init(){
        httpClient = HttpClients.createDefault();

        stubFor(get(urlEqualTo("/rest")).willReturn(aResponse().withBody("Welcome to Baeldung!")));

        stubFor(get(urlPathMatching("/rest/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("\"testing-library\": \"WireMock\"")));

        stubFor(post(urlEqualTo("/resr/wiremock"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(containing("\"testing-library\": \"WireMock\""))
                .withRequestBody(containing("\"creator\": \"Tom Akehurst\""))
                .withRequestBody(containing("\"website\": \"wiremock.org\""))
                .willReturn(aResponse()
                        .withStatus(200)));

        stubFor(get(urlPathEqualTo("/rest/wiremock"))
                .withHeader("Accept", matching("text/.*"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withHeader("Content-Type", "text/html")
                        .withBody("!!! Service Unavailable !!!")));

        stubFor(post(urlEqualTo("/rest/wiremock"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(containing("\"testing-library\": \"WireMock\""))
                .withRequestBody(containing("\"creator\": \"Tom Akehurst\""))
                .withRequestBody(containing("\"website\": \"wiremock.org\""))
                .willReturn(aResponse()
                        .withStatus(200)));
    }

    @Test
    public void zeroTest() throws IOException {
        HttpGet request = new HttpGet("http://localhost:9090/rest");
        HttpResponse httpResponse = httpClient.execute(request);

        verify(getRequestedFor(urlEqualTo("/rest")));
        assertEquals("Welcome to Baeldung!", convertResponseToString(httpResponse));
    }

    @Test
    public void anotherTest() throws IOException {
        HttpGet request = new HttpGet("http://localhost:9090/rest/wiremock");
        HttpResponse httpResponse = httpClient.execute(request);

        verify(getRequestedFor(urlEqualTo("/rest/wiremock")));
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());
        assertEquals("application/json", httpResponse.getFirstHeader("Content-Type").getValue());
        assertEquals("\"testing-library\": \"WireMock\"", convertResponseToString(httpResponse));
    }

    @Test
    public void serviceUnavailableTest() throws IOException {
        HttpGet request = new HttpGet("http://localhost:9090/rest/wiremock");
        request.addHeader("Accept", "text/html");
        HttpResponse httpResponse = httpClient.execute(request);

        verify(getRequestedFor(urlEqualTo("/rest/wiremock")));
        assertEquals(503, httpResponse.getStatusLine().getStatusCode());
        assertEquals("text/html", httpResponse.getFirstHeader("Content-Type").getValue());
        assertEquals("!!! Service Unavailable !!!", convertHttpResponseToString(httpResponse));
    }

    @Test
    public void bodyMatchingTest() throws IOException {
        StringEntity entity = new StringEntity(convertInputStreamToString(
                this.getClass()
                        .getClassLoader()
                        .getResourceAsStream("wiremock_intro.json")));

        HttpPost request = new HttpPost("http://localhost:9090/rest/wiremock");
        request.addHeader("Content-Type", "application/json");
        request.setEntity(entity);
        HttpResponse response = httpClient.execute(request);
        verify(postRequestedFor(urlEqualTo("/rest/wiremock"))
                .withHeader("Content-Type", equalTo("application/json")));
        assertEquals(200, response.getStatusLine().getStatusCode());
    }

    private String convertHttpResponseToString(HttpResponse httpResponse) throws IOException {
        InputStream inputStream = httpResponse.getEntity().getContent();
        return convertInputStreamToString(inputStream);
    }

    private String convertInputStreamToString(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream, "UTF-8");
        String string = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return string;
    }

    private String convertResponseToString(HttpResponse response) throws IOException {
        InputStream responseStream = response.getEntity().getContent();
        Scanner scanner = new Scanner(responseStream, "UTF-8");
        String responseString = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return responseString;
    }
}
