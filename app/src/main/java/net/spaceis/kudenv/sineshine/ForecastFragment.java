package net.spaceis.kudenv.sineshine;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kudenv on 4/21/15.
 */
public class ForecastFragment extends Fragment {

    public ArrayAdapter<String> mForecastAdapter;
    //String[] forecastArray;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment,menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_refresh) {
            FeatchWeatherTask weatherTask = new FeatchWeatherTask();
            weatherTask.execute("94043");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] forecastArray = {
                "Today - Sunny - 88/63",
                "Tomorrow - Foggy - 88/63",
                "Weds - Cloudy - 88/63",
                "Thurs - Asteroids - 88/63",
                "Fri - Heavy Rain - 88/63",
                "Sat - HELP TRAPPED - 88/63",
                "Sun - Sunny - 88/63",
        };

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));

        mForecastAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,weekForecast);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        listView.setOnClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String forecast = mForecastAdapter.getItem(position);
                Toast.makeText(getActivity(),forecast,Toast.LENGTH_SHORT).show()

            }
        });



        return rootView;
    }

   public class FeatchWeatherTask extends AsyncTask<String,Void,String[]> {

       private final String LOG_TAG = FeatchWeatherTask.class.getSimpleName();


       @Override
       protected String[] doInBackground(String... params) {

           if (params.length == 0) {return null;}
           // These two need to be declared outside the try/catch
           // so that they can be closed in the finally block.
           HttpURLConnection urlConnection = null;


           BufferedReader reader = null;

           // Will contain the raw JSON response as a string.
           String forecastJsonStr = null;
           String format = "json";
           String units = "metric";
           int numDays = 7;

           try {
               // Construct the URL for the OpenWeatherMap query
               // Possible parameters are avaiable at OWM's forecast API page, at
               // http://openweathermap.org/API#forecast
               //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

               final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
               final String QUERY_PARAM = "q";
               final String FORMAT_PARAM = "mode";
               final String UNITS_PARAM = "units";
               final String DAYS_PARAM = "cnt";

               Uri buildUrl  = Uri.parse(FORECAST_BASE_URL).buildUpon()
                       .appendQueryParameter(QUERY_PARAM,params[0])
                       .appendQueryParameter(FORMAT_PARAM, format)
                       .appendQueryParameter(UNITS_PARAM, units)
                       .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                       .build();

               URL url = new URL(buildUrl.toString());

               Log.v(LOG_TAG, "URI BUILDE FROM PARAMS " + buildUrl.toString());

               // Create the request to OpenWeatherMap, and open the connection
               urlConnection = (HttpURLConnection) url.openConnection();
               urlConnection.setRequestMethod("GET");
               urlConnection.setDoInput(true);
               urlConnection.setDoOutput(true);
               urlConnection.connect();

               // Read the input stream into a String
               InputStream inputStream = urlConnection.getInputStream();
               StringBuffer buffer = new StringBuffer();
               if (inputStream == null) {
                   // Nothing to do.
                   return null;
               }
               reader = new BufferedReader(new InputStreamReader(inputStream));

               String line;
               while ((line = reader.readLine()) != null) {
                   // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                   // But it does make debugging a *lot* easier if you print out the completed
                   // buffer for debugging.
                   buffer.append(line + "\n");
               }

               if (buffer.length() == 0) {
                   // Stream was empty.  No point in parsing.
                   return null;
               }
               forecastJsonStr = buffer.toString();
               Log.v(LOG_TAG, "Forecast JSON String " + forecastJsonStr);
           } catch (IOException e) {
               Log.e(LOG_TAG, "Error ", e);
               // If the code didn't successfully get the weather data, there's no point in attemping
               // to parse it.
               return null;
           } finally{
               if (urlConnection != null) {
                   urlConnection.disconnect();
               }
               if (reader != null) {
                   try {
                       reader.close();
                   } catch (final IOException e) {
                       Log.e("PlaceholderFragment", "Error closing stream", e);
                   }
               }
           }
           try {
                return Utils.getWeatherDataFromJson(forecastJsonStr,numDays);
           } catch (JSONException e) {
               Log.e(LOG_TAG, e.getMessage(),e);
               e.printStackTrace();
           }
           return null;
       }

       @Override
       protected void onPostExecute(String[] strings) {
           super.onPostExecute(strings);
           if (strings != null) {
               mForecastAdapter.clear();
               for(String dayFC :strings) {
                   mForecastAdapter.add(dayFC);
               }
           }
       }
   }
}

