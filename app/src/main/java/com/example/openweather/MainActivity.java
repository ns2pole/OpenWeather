package com.example.openweather;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTextView = findViewById(R.id.resultTextView);
    }

    public void callOpenWeatherApi(View view) {
        String requestUrl = "http://api.openweathermap.org/data/2.5/forecast?q=tsu,jp&APPID=6eebb97b538203a912ee4a0cfdad0299";
        try {
            httpRequest(requestUrl);
        } catch (Exception e) {
            Log.e("callPostalCodeApi", e.getMessage());
        }
    }

    private void httpRequest(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(new Callback() {
            //エラー
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("httpRequest", e.getMessage());
            }

            //正常
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //response取り出し
                final String xmlStr = response.body().string();
                final String str = parseResponseBody(xmlStr);
                resultTextView.setText(str);
            }
        });
    }

    private String parseResponseBody(String responseBody) {
        try {
            JSONObject ob = new JSONObject(responseBody);
            String result = "";
            for (int i = 0; i < ob.getJSONArray("list").length(); i++) {
                String dateStr = ob.getJSONArray("list")
                        .getJSONObject(i).getString("dt_txt");
                String list1 = ob.getJSONArray("list")
                        .getJSONObject(i).getJSONArray("weather")
                        .getJSONObject(0).getString("description");
                DateTimeFormatter formatter = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                }

                // UTCの日付・時間に変換
                ZonedDateTime utcDateTime = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    utcDateTime = ZonedDateTime.parse(dateStr, formatter.withZone(ZoneId.of("UTC")));
                }

                System.out.println("UTC DateTime: " + utcDateTime);

                // JSTに変換
                ZonedDateTime jstDateTime = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    jstDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Tokyo"));
                }

                LocalDate date = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    date = jstDateTime.toLocalDate();
                }

                LocalTime time = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    time = jstDateTime.toLocalTime();
                }

//                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH");
//                TimeZone tz = TimeZone.getTimeZone("JST");
//                dateFormat.setTimeZone(tz);
//                Log.d("tag", dateStr);
//                Date date = dateFormat.parse(dateStr);
//                String str = new SimpleDateFormat("M/d HH:mm").format(date).toString();
                result += date.toString() + "  " + time.toString() + " :  [ " + list1 + " ]\n\n";
            }

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "cannot get value";
    }
}