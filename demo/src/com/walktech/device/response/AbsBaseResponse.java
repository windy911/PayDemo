package com.walktech.device.response;


import com.walktech.device.data.Feed;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by windy on 15/8/20.
 */
public class AbsBaseResponse {

    protected Feed parseBaseResponse(Feed feed, String resp) {

        try {
            JSONObject json = (JSONObject) new JSONTokener(resp).nextValue();
            feed.resCode = json.getString("resCode");
            feed.resMsg = json.getString("resMsg");
            feed.resData = json.getString("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return feed;
    }
}
