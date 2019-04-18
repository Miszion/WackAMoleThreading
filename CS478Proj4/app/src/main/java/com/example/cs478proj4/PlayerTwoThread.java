package com.example.cs478proj4;
//Mission Marcus Player Two Thread - Player Two Thread will guess the next spot on the grid. UNLESS player one's last move was a close guess or near miss. In that case, our thread will random guess a direction and try to place a spot there.. The structure of this thread is similar to the playerone thread, however, we have static
// points here because the next run statement needs to memorize the last point to go to the next point.
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PlayerTwoThread extends Thread {

    private Handler myHandler;
    private String name;
    private int image;
    private static int xCoordinate = -1; // set to -1 at first because we want to hit the first point
    private static int yCoordinate = 0;
    private int otherXCoord;
    private int otherYCoord;
    private Bundle data;
    private Bundle bundle;
    private Message msg;

    public PlayerTwoThread(String name, Handler myHandler, int image, Bundle data)
    {
        super(name);
        super.setName(name);
        this.myHandler = myHandler; // just use the masterthread handler to send messages.
        this.name = name;
        this.image = image;
        this.data = data;
        this.bundle = new Bundle();
        this.msg = new Message();

        otherXCoord = -1;
        otherYCoord = -1;

    }


    public void run()
   {
       Looper.prepare();



       myHandler.post(new Runnable() {
           @Override
           public void run() {

               synchronized (this) {


                   if (data.getString("Status") != null) {

                       if (data.get("Status").equals("Near miss")) {
                           // make smart guess

                           makeMove(data.getInt("XVal"), data.getInt("YVal"), 1);


                       } else if (data.get("Status").equals("Close guess")) {
                           // make smart guess

                           makeMove(data.getInt("XVal"), data.getInt("YVal"), 2);

                       }

                   } else // if we havent had any thread hit near miss or close guess.
                   {
                       if (xCoordinate < 9) { // if we arent exceeding the xValue cap, we can go one more in the xDirection
                           xCoordinate = xCoordinate + 1;
                       } else { // if we are
                           xCoordinate = 0; // set our xCoordinate to 0 again and check the next y coordinate
                           if (yCoordinate < 9) { // if we arent exceeding the highest row..
                               yCoordinate = yCoordinate + 1; // we can keep going up the row..
                           } else { // if not..
                               yCoordinate = 0; // go back to the beginning of the grid.
                           }
                       }

                       sendMessageToMaster(xCoordinate, yCoordinate);
                   }


               }
           }


       });


       Looper.loop();

   }


   public int getImage() // retrieve the image for viewing purposes.
   {
       return image;
   }

   public static void resetCoords() // reset the coordinates.
   {
       xCoordinate = -1;
       yCoordinate = 0;
   }

   public Handler getMyHandler()
   {
       return myHandler;
   }

   public void makeMove(int x, int y, int spaces)
   {
       int randomGenerator = (int)(Math.random() * 8); // 8 possible directions to randomly go.
       boolean solutionFound = false;


       while (!solutionFound) {
           if (randomGenerator == 0) { // down direction (y + spaces)

               if (y + spaces <= 9) {
                   sendMessageToMaster(x, y + spaces);
                   xCoordinate = x;
                   yCoordinate = y + spaces;
                   solutionFound = true;
               }


           }
           else if (randomGenerator == 1) // up direction (y - spaces)
           {
               if (y - spaces >= 0)
               {
                   sendMessageToMaster(x, y - spaces);
                   xCoordinate = x;
                   yCoordinate = y - spaces;
                   solutionFound = true;
               }
           }
           else if (randomGenerator == 2) // left direction
           {
               if (x - spaces >= 0)
               {
                   sendMessageToMaster(x-spaces, y);
                   xCoordinate = x - spaces;
                   yCoordinate = y;
                   solutionFound = true;
               }
           }
           else if (randomGenerator == 3) // right direction
           {
               if (x+spaces <= 9)
               {
                   xCoordinate = x + spaces;
                   yCoordinate = y;
                   sendMessageToMaster(x+spaces, y);
                   solutionFound = true;
               }
           }
           else if (randomGenerator == 4) // diagonal up-right
           {
               if ((x+spaces) <= 9 && ((y-spaces) >= 0))
               {
                   xCoordinate = x + spaces;
                   yCoordinate = y-spaces;
                   sendMessageToMaster(x+spaces, y-spaces);
                   solutionFound = true;
               }
           }
           else if (randomGenerator == 5) // diagonal down-left
           {
               if ((x-spaces) >= 0 && ((y+spaces) <= 9))
               {
                   xCoordinate = x - spaces;
                   yCoordinate = y+spaces;
                   sendMessageToMaster(x-spaces, y+spaces);
                   solutionFound = true;
               }
           }
           else if (randomGenerator == 6) // diagonal up-left
           {
               if ((x-spaces) >= 0 && ((y-spaces) >= 0))
               {
                   xCoordinate = x - spaces;
                   yCoordinate = y-spaces;
                   sendMessageToMaster(x-spaces, y-spaces);
                   solutionFound = true;
               }
           }
           else if (randomGenerator == 7) // diagonal down-right
           {
               if ((x+spaces) <=9 && ((y+spaces) <= 9))
               {
                   xCoordinate = x + spaces;
                   yCoordinate = y + spaces;
                   sendMessageToMaster(x+spaces, y+spaces);
                   solutionFound = true;
               }
           }

           randomGenerator = (int)(Math.random() * 8);

       }



   }

   public void sendMessageToMaster(int x, int y)
   {
       bundle.putInt("XVal", x); // put xvalues and yvalues in the bundle for the message
       bundle.putInt("YVal", y);
       msg.obj = "Done"; // set done as the object to tell the master thread
       msg.setData(bundle);


       myHandler.sendMessage(msg); // send the message
   }

   public int getX()
   {
       return xCoordinate;
   }

   public int getY()
   {
       return yCoordinate;
   }



}
