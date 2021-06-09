package com.example.blecentral3;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyAsyncTask extends AsyncTask<String, Void, String> {
    private showInfo mActivity;
    String result = null;
    UUID muuid;
    int mnum;

    public MyAsyncTask(showInfo activity, UUID uuid, int num) {
        this.mActivity = activity;
        muuid = uuid;
        mnum = num;
    }

    @Override
    protected String doInBackground(String... params) {
        String urlSt = "https://test-mfapp.herokuapp.com/connect_db3.php";
        int mNum = mnum;
        String uuid = muuid.toString();
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject("{\"num\":\""+ mNum +"\",\"uuid\":\"" + uuid + "\"}");
            Log.i("json", jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{
            URL url = new URL(urlSt);
            OkHttpClient client = new OkHttpClient.Builder().build();
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
            Request request = new Request.Builder().url(urlSt).post(requestBody).build();

            Thread.sleep(3000);

            Response response = client.newCall(request).execute();
            result = response.body().string();
            response.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String param) {
        mActivity.returnNum(param);
        return;
    }
}
