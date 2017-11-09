package project.datos.tec.graphmessanger.logic.communication.facebook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by josea on 11/15/2016.
 */

public class GetImg extends AsyncTask<String,Integer, Bitmap> {

    private static String URL = "https://graph.facebook.com/";
    @Override
    protected Bitmap doInBackground(String ... params){
        URL url = null;
        InputStream inputStream = null;
        try {
            url = new URL(URL+params[0]+ "/picture?type=large");
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            inputStream = urlConnection.getInputStream();
        } catch (MalformedURLException e) {
            Log.d("inputstream", "doInBackground error: MalinformedUrlExeption"+"  url: "+url);
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("inputstream", "doInBackground error: IOException"+"  url: "+url);
            e.printStackTrace();
        }
        Log.d("inputstream", "doInBackground: "+inputStream);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap i) {
        super.onPostExecute(i);
    }
}