package com.example.cs478proj4;
//Mission Marcus PlayerOneThread - This thread identifies what player one does. We have a name and a handler for the thread as well as an associated image to use when plotting.
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.logging.LogRecord;

public class PlayerOneThread extends Thread {

    private Handler myHandler;
    private String name;
    private int image;
    private static int xCoordinate;
    private static int yCoordinate;

    public PlayerOneThread(String name, Handler myHandler, int image)
    {
        super(name);
        super.setName(name);
        this.myHandler = myHandler;
        this.name = name;
        this.image = image;


        xCoordinate = 0; // set these to zero just to instantiate.
        yCoordinate = 0;

    }


    public void run()
   {
       Looper.prepare();



       myHandler.post(new Runnable() {
           @Override
           public void run() {

               synchronized (this) {

                   xCoordinate = (int) (Math.random() * 10); // find two random points on the map.
                   yCoordinate = (int) (Math.random() * 10);



                   Message msg = new Message();
                   Bundle bundle = new Bundle();
                   bundle.putInt("XVal", xCoordinate); // make a message and a bundle that gives the two points.
                   bundle.putInt("YVal", yCoordinate);
                   msg.setData(bundle); // set the data
                   msg.obj = "Done"; //give message state done to the main thread

                   myHandler.sendMessage(msg); // send the message to the main thread since this handler of this thread is the master thread handler also.


               }



           }
       });


       Looper.loop(); // loop back around.

   }


   public int getImage() // return the image for a thread.
   {
       return image;
   }


    public Handler getMyHandler()
    {
        return myHandler;
    }

    public static int getX()
    {
        return xCoordinate;
    }

    public static int getY()
    {
        return yCoordinate;
    }




}
