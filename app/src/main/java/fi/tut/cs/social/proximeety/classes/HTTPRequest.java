package fi.tut.cs.social.proximeety.classes;

import org.json.JSONObject;

/**
 * Created by Aris on 01/09/15.
 */
public class HTTPRequest {
    public String type;
    public String url;
    public int method;
    public JSONObject reqJSON;

    public HTTPRequest() {

    }

    public HTTPRequest(String type, String url, int method, JSONObject reqJSON) {
        this.type = type;
        this.url = url;
        this.method = method;
        this.reqJSON = reqJSON;
    }
}
