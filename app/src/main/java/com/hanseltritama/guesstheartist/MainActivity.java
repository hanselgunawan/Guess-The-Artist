package com.hanseltritama.guesstheartist;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Pattern p;
    Matcher m;
    ArrayList<Artist> artist_arr = new ArrayList<Artist>();
    Random rand = new Random();
    LinearLayout linearLayout;
    ImageView imageView;
    View home_layout;
    View question_layout;

    int random_number;

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {

                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();//open the browser window
                InputStream in = urlConnection.getInputStream();//stream to hold the input of data
                InputStreamReader reader = new InputStreamReader(in);//to read the content of the URL
                int data = reader.read();

                while (data != -1) {

                    char current = (char) data;

                    result += current;//grab all HTML elements on a site

                    data = reader.read();

                }

                return result;
            }
            catch(Exception e) {

                e.printStackTrace();
                return "Failed";

            }
        }

    }

    class Artist {
        String artist_img;
        String artist_name;

        public Artist(String artist_img, String artist_name) {
            this.artist_img = artist_img;
            this.artist_name = artist_name;
        }
    }

    public void storeArtistImageIntoArray(String artist_page_source) {
        p = Pattern.compile("img\\ssrc=\"(.*?)\"\\sa");
        m = p.matcher(artist_page_source);

        while(m.find()) {

            Artist artist = new Artist(m.group(1), "");
            artist_arr.add(artist);

        }
    }

    public void storeArtistNameIntoArray(String artist_page_source) {
        int idx_counter = 0;
        p = Pattern.compile("alt=\"(.*?)\"");
        m = p.matcher(artist_page_source);

        while(m.find()) {

            artist_arr.get(idx_counter).artist_name = m.group(1);
            idx_counter++;

        }
    }

    public void shuffleArray() {
        Collections.shuffle(artist_arr);
    }

    public void displayQuestion() {
        random_number = rand.nextInt(3);
        shuffleArray();
        Picasso.get().load(artist_arr.get(random_number).artist_img).into(imageView);

        for(int i = 0; i < linearLayout.getChildCount(); i++) {
            Button button = (Button) linearLayout.getChildAt(i);
            button.setText(artist_arr.get(i).artist_name);
        }
    }

    public void onAnswerClick(View view) {
        Button button = (Button) view;
        if(button.getText() == artist_arr.get(random_number).artist_name)
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this,
                    "Incorrect! It's " + artist_arr.get(random_number).artist_name,
                    Toast.LENGTH_SHORT).show();

        artist_arr.remove(random_number);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                displayQuestion();
            }
        };

        Handler handler = new Handler();
        handler.postDelayed(runnable, 1500);
    }

    public void onPlayNowClick(View view) {

        home_layout.setVisibility(View.INVISIBLE);
        question_layout.setVisibility(View.VISIBLE);

    }

    public void onExitClick(View view) {

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);

    }

    public void displayArrayList() {
        for(int i = 0; i < artist_arr.size(); i++) {

            Log.i("Image & Artist", artist_arr.get(i).artist_img + " | " + artist_arr.get(i).artist_name);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask downloadTask = new DownloadTask();
        String artist_page_source = null;
        home_layout = findViewById(R.id.home_layout);
        question_layout = findViewById(R.id.question_layout);
        linearLayout = findViewById(R.id.linear_layout);
        imageView = findViewById(R.id.imageView);

        imageView.setVisibility(View.INVISIBLE);
        linearLayout.setVisibility(View.INVISIBLE);
        home_layout.setVisibility(View.VISIBLE);

        try {
            artist_page_source = downloadTask.execute("http://www.posh24.se/kandisar").get();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        storeArtistImageIntoArray(artist_page_source);
        storeArtistNameIntoArray(artist_page_source);
        displayArrayList();
        displayQuestion();
    }
}
