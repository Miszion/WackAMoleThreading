package com.example.cs478proj4;
// Mission Marcus Main Activity - This class is just a beginning stage to start a game.
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {


    private Button startButton; // just a start button and a gopher picture.
    private ImageView gopherPic;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        startButton = findViewById(R.id.startActivity);
        gopherPic = findViewById(R.id.gopher);
        gopherPic.setImageResource(R.drawable.gopher);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inte = new Intent(MainActivity.this, SecondActivity.class); // all we do when a button is clicked, is we start the new game activity.
                startActivity(inte);

            }
        });


    }

}
