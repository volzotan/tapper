package de.volzo.tapper;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by volzotan on 05.12.16.
 */

public class Support {

    private static final String TAG = Support.class.getName();

    Context context;
    StringBuilder stringbuilder = new StringBuilder();

    public Support(Context context) {
        this.context = context;
    }

    public double[][] convert(Double[] x, Double[] y, Double[] z) {
        double[][] combined = new double[x.length][3];

        for (int i=0; i<x.length; i++) {
            combined[i][0] = x[i];
            combined[i][1] = y[i];
            combined[i][2] = z[i];
        }

        return combined;
    }

    public void add(double[][] arr) {
        // append all values separated by comma, terminated by newlines

        for (int i=0; i<arr.length; i++) {
            for (int j=0; j<arr[i].length-1; j++) {
                stringbuilder.append(Double.toString(arr[i][j]));
                stringbuilder.append(",");
            }

            // append last value without trailing comma
            if (arr[i].length > 0) {
                stringbuilder.append(Double.toString(arr[i][arr[i].length-1]));
                stringbuilder.append("\n");
            }
        }
    }

    public void saveToFile(String filenameWithoutSuffix) {
        File dir = context.getFilesDir();
        File csv = new File(dir, filenameWithoutSuffix+".csv");

        try {
            FileOutputStream outputStream = new FileOutputStream(csv);
            outputStream.write(stringbuilder.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile(String filenameWithoutSuffix) {
        File dir = context.getFilesDir();
        File csv = new File(dir, filenameWithoutSuffix+".csv");

        try {

            FileInputStream inputStream = new FileInputStream(csv);

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            System.out.println(sb.toString());
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        stringbuilder = new StringBuilder();
    }


    public void print() {
        System.out.print(stringbuilder.toString());
    }

    public void send(String description) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = "https://api.github.com/gists";

        class CustomRequest extends Request<JSONObject> {

            private Response.Listener<JSONObject> listener;
            private Map<String, String> params;
            private JSONObject payload;

            public CustomRequest(String url, Map<String, String> params,
                                 Response.Listener<JSONObject> reponseListener, Response.ErrorListener errorListener) {
                super(Method.POST, url, errorListener);
                this.listener = reponseListener;
                this.params = params;
            }

            public CustomRequest(int method, String url, Map<String, String> params,
                                 Response.Listener<JSONObject> reponseListener, Response.ErrorListener errorListener) {
                super(method, url, errorListener);
                this.listener = reponseListener;
                this.params = params;
            }

            public CustomRequest(int method, String url, JSONObject payload, Map<String, String> params,
                                 Response.Listener<JSONObject> reponseListener, Response.ErrorListener errorListener) {
                super(method, url, errorListener);
                this.payload = payload;
                this.listener = reponseListener;
                this.params = params;
            }

            @Override
            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(
                        "Authorization",
                        String.format("Basic %s", Base64.encodeToString(
                                String.format("%s:%s",
                                        // if you get an error here, add your github credentials
                                        // the file Tapper/app/src/main/res/values/credentials.xml

                                        context.getResources().getString(R.string.github_username),
                                        context.getResources().getString(R.string.github_password)
                                ).getBytes(), Base64.DEFAULT)
                        )
                );
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                // usually you'd have a field with some values you'd want to escape, you need to do it yourself if overriding getBody. here's how you do it
//                try {
//                    httpPostBody=httpPostBody+"&randomFieldFilledWithAwkwardCharacters="+URLEncoder.encode("{{%stuffToBe Escaped/","UTF-8");
//                } catch (UnsupportedEncodingException exception) {
//                    Log.e("ERROR", "exception", exception);
//                    // return null and don't pass any POST string if you encounter encoding error
//                    return null;
//                }
                return payload.toString().getBytes();
            }

            @Override
            protected void deliverResponse(JSONObject response) {
                listener.onResponse(response);
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONObject(jsonString),
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    return Response.error(new ParseError(e));
                } catch (JSONException je) {
                    System.out.println(new String(response.data));
                    return Response.error(new ParseError(je));
                }
            }

        }

        JSONObject body = new JSONObject();
        JSONObject files = new JSONObject();
        JSONObject content = new JSONObject();

        try {
            content.put("content", stringbuilder.toString());
            files.put("log.txt", content);
            body.put("files", files);

            if (description != null && description.length() > 0){
                body.put("description", description);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }

        Map<String, String> params = new HashMap<String, String>();

        CustomRequest jsObjRequest = new CustomRequest(Request.Method.POST, url, body, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //Log.d(TAG, "Success Response: "+ response.toString());
                Toast.makeText(context, "Log sent", Toast.LENGTH_SHORT).show();
                clear();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError response) {
                Log.d("Error Response: ", response.toString());
                Toast.makeText(context, "sending Log failed", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsObjRequest);
    }
}
