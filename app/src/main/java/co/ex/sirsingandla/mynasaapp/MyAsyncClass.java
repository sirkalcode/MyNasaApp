package co.ex.sirsingandla.mynasaapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.transform.Result;

/**
 * Created by Sirsingandla on 10/29/2014.
 */
public class MyAsyncClass extends AsyncTask<Activity, String, Activity>
{
    private XmlPullParserFactory xmlFactoryObject;
    private InputStream stream;

    private String title;
    private String pubDate;
    private String description;
    private Bitmap bitmap;
    private Activity activity;
    ProgressDialog dialog;

    @Override
    protected Activity doInBackground(Activity... params)
    {
        try {
            Log.d(" URL", "Opening connection............");
            URL url = new URL("http://www.nasa.gov/rss/dyn/image_of_the_day.rss");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            stream = conn.getInputStream();

            xmlFactoryObject = XmlPullParserFactory.newInstance();
            XmlPullParser myparser = xmlFactoryObject.newPullParser();

            myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            myparser.setInput(stream, null);
            parseXMLAndStoreIt(myparser);

            //bitmap = BitmapFactory.decodeStream(stream);

            //Log.d("BITMAP", bitmap.toString());

            stream.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d("Error Retrieving URL", e.getMessage());
        }
        finally {
            Log.d("URL Retrieved", "Success");
        }

        activity = params[0];
        return params[0];
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //activity.setProgressBarIndeterminateVisibility(true);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        //dialog = ProgressDialog.show(activity, "Loading", "Loading the image of the day");
    }

    @Override
    protected void onPostExecute(Activity result) {
        super.onPostExecute(result);
        try {
            Log.d("ASYNC CLASS", "Begining Post Execute...");

            TextView titleTV = (TextView) result.findViewById(R.id.nasatitle);
            titleTV.setText(title);

            TextView descriptionTV = (TextView) result.findViewById(R.id.nasadescription);
            descriptionTV.setText(description);

            TextView timestampTV = (TextView) result.findViewById(R.id.nasatimestamp);
            timestampTV.setText(pubDate);

            Log.d("ASYNC CLASS", "setting bitmap...." );
            ImageView iv = (ImageView) result.findViewById(R.id.nasaimage);
            iv.setImageBitmap(bitmap);

            Log.d("ASYNC CLASS", "finished setting bitmap...." );

            //activity.setProgressBarIndeterminateVisibility(false);
        }
        catch(Exception e) {
        }
        finally {
            Log.d("ASYNC CLASS", "Finished executing Async class");
            //dialog.dismiss();
        }
    }

    public void parseXMLAndStoreIt(XmlPullParser myParser) {
        int event;
        String text=null;
        int i = 0;
        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name=myParser.getName();
                switch (event){
                    case XmlPullParser.START_TAG:
                        break;
                    case XmlPullParser.TEXT:
                        text = myParser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if(name.equals("title") ){
                            title = text;
                            Log.d("Title",text);
                        }
                        else if(name.equals("description") ){
                            description = text;
                            Log.d("description",text);
                        }
                        else if(name.equals("pubDate") ){

                                pubDate = text;
                                Log.d("Publish Date", text);

                        }
                        else if(name.equals("enclosure") ){
                            String relType = myParser.getAttributeValue(null, "url");
                            bitmap = downloadBitmap(relType);
                            Log.d("URL", relType);
                        }
                        else
                        {
                            Log.d("Event: ", name);
                        }
                        break;
                }
                event = myParser.next();
                Log.d("ITEM LOOP", Integer.toString(i));

            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Error Parsing ",e.getMessage());
        }

    }


    private Bitmap downloadBitmap(String url) throws IOException {
        HttpUriRequest request = new HttpGet(url.toString());
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);

        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            byte[] bytes = EntityUtils.toByteArray(entity);

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,  bytes.length);
            return bitmap;
        } else {
            throw new IOException("Download failed, HTTP response code "
                    + statusCode + " - " + statusLine.getReasonPhrase());
        }
    }

}
