package project.comp3004.hyggelig.bitcoin;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class HttpQueue {
    /**
     * An implementation of the Singleton pattern to ensure that the Crypto activity is always
     * making use of the same, single Volley RequestQueue
     */
    private static Context context;
    private static RequestQueue volleyRequestQueue;
    private static HttpQueue singletonQueue;

    private HttpQueue(Context context){
        this.context = context;
        volleyRequestQueue = getRequestQueue();
    }

    private RequestQueue getRequestQueue(){
        // More lazy initialization
        if(volleyRequestQueue==null) {
            volleyRequestQueue = Volley.newRequestQueue(context);
        }
        return volleyRequestQueue;

    }

    public static HttpQueue getHttpQueue(Context cxt){
        //Lazy Initialization
        if (singletonQueue == null){
            singletonQueue = new HttpQueue(cxt.getApplicationContext());
        }

        return singletonQueue;
    }

    public void queueRequest(JsonObjectRequest request){
        getRequestQueue().add(request);
    }

    public void startQueue(){
        getRequestQueue().start();
    }

    public void dequeueRequests(String tag){
        getRequestQueue().cancelAll(tag);
    }

}
