package com.dato.deploy;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ning.http.client.*;
import com.ning.http.client.uri.Uri;

/**
 * PredictiveServiceClientResponse is a wrapper for accessing the response
 * returned from the Predictive Service.
 *
 */
public class PredictiveServiceClientResponse {
    private Future<Response> future;
    private JSONObject validResponse;
    private String errorMessage;
    private Response response;

    public PredictiveServiceClientResponse(Future<Response> response) {
        this.future = response;
        this.response = null;
    }

    /*
     * Get the response from Future and parse the response.
     */
    private void getParsedResponse() {
        if (this.response == null) {
            try {
                this.response = this.future.get();
            } catch (InterruptedException e) {
                throw new PredictiveServiceClientException(e.getMessage(), e);
            } catch (ExecutionException e) {
                throw new PredictiveServiceClientException(e.getMessage(), e);
            }
            parseResponse(); // parse the returned response
        }
    }

    /*
     * Parses the response of the Predictive Service into an JSON object.
     */
    private void parseResponse() {
        int code = this.response.getStatusCode();
        JSONParser parser = new JSONParser();
        if (code == 404) { // not found
            setError(this.response.getStatusText());
        } else {
            if (this.response.hasResponseBody()) {
                try {
                    String responseBody = this.response.getResponseBody();
                    if (code == 200) {
                        this.validResponse =
                                (JSONObject) parser.parse(responseBody);
                    } else {
                        // valid response but contains error
                        setError("Error: " + responseBody);
                    }
                } catch (IOException e) {
                    // bad response body
                    setError("Error: Bad response body." + e.getMessage());
                } catch (ParseException e) {
                    // cannot parse response body
                    setError("Error: Cannot parser response body."
                            + e.getMessage());
                }
            } else { // no response body
                setError("Error: Cannot find response body.");
            }
        }
    }

    /*
     * Sets the error message and invalidate the response.
     */
    private void setError(String msg) {
        this.validResponse = null;
        this.errorMessage = msg;
    }

    /**
     * Returns the JSONObject that contains the valid response from the
     * Predictive Service. If there is any error, the returned JSONObject
     * would be null.
     *
     * @return
     *      JSONObject containing the valid response from Predictive Service.
     */
    public JSONObject getResult() {
        getParsedResponse();
        return this.validResponse;
    }

    /**
     * Returns the error message, if any, returned from the Predictive Service.
     *
     * @return
     *      String representation of the error message.
     */
    public String getErrorMessage() {
        getParsedResponse();
        return this.errorMessage;
    }

    /**
     * Returns the content type of the response from Predictive Service.
     *
     * @return
     *      String representation of the content type.
     */
    public String getContentType() {
        getParsedResponse();
        return response.getContentType();
    }

    /**
     * Returns the status code of the response from Predictive Service.
     *
     * @return
     *      int representation of status code.
     */
    public int getStatusCode() {
        getParsedResponse();
        return this.response.getStatusCode();
    }

    /**
     * Returns the status text of the response from Predictive Service.
     *
     * @return
     *      String representation of the status text.
     */
    public String getStatusText() {
        getParsedResponse();
        return this.response.getStatusText();
    }

    /**
     * Returns the URI of the response form Predictive Service.
     *
     * @return
     *      URI of the response.
     */
    public Uri getUri() {
        getParsedResponse();
        return this.response.getUri();
    }

    /**
     * Returns the raw Response from the Predictive Service generated by
     * async http client.
     *
     * @return
     *      Response that captured what was returned from Predictive Service.
     */
    public Response getResponse() {
        getParsedResponse();
        return this.response;
    }

    /**
     * Returns the Future that was returned from querying the Predictive
     * Service with async http client.
     *
     * @return
     *      Future
     */
    public Future<Response> getRawResponse() {
        return this.future;
    }
}
