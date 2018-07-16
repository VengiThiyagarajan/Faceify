package com.example.vengithiyagarajan.faceapi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Splash extends AppCompatActivity {

     ImageView imageView,imageView1;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        Animation animation= AnimationUtils.loadAnimation(this,R.anim.animation);

        final Intent i = new Intent(this,MainActivity.class);
        Thread timer =new Thread()
        {
            public void run(){

                try{  
                    sleep(5000);


                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                finally {
                    startActivity(i);
                    finish();
                }
            }
        };
        timer.start();
    }

}

