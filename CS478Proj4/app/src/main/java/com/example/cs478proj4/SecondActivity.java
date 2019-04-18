package com.example.cs478proj4;
//Mission Marcus Second Activity - This activity defines the main play for the app.
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    private Button myButton;
    private Button restart;
    private static TextView textView;
    private static int winningX;
    private static int winningY;
    private MasterThread masterThread;
    private static GridView gridView;
    private static ArrayList<ImageObj> images;
    private static ImageObj[][] grid;
    private static ImageAdapter adapter;
    private Button auto;
    private Boolean gameOver;
    private static Boolean isAuto;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private TextView toastMessage;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        winningX = (int)(Math.random() * 10); // make two random coordinate points for the gopher to go in the beginning.
        winningY = (int)(Math.random() * 10);

        gameOver = false; // gameover is false.
        isAuto = false; // set automatic to false in the beginning.
        toastMessage = findViewById(R.id.toastMessage);
        toastMessage.setText("Manual Mode");
        grid = new ImageObj[10][10]; // make a 10x10 grid of imageObjects.
        images = new ArrayList<>(); // make an arraylist and an adapter as well
        adapter = new ImageAdapter(this);
        setUpDialog(); // set up the dialog box.
        setUpGrid();



        myButton = findViewById(R.id.button); // set up fields
        auto = findViewById(R.id.auto);
        restart = findViewById(R.id.restart);
        textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod()); // make a scrolling method for the textview.
        gridView = findViewById(R.id.gridView);
        gridView.setAdapter(adapter); // set the adapter to the grid view.
        masterThread = new MasterThread();
        setUpButtons();


        masterThread.start();



    }

    private class MasterThread extends Thread
    {
            private PlayerOneThread p1; // have a player 1 and two.
            private PlayerTwoThread p2;
            private Handler masterHandler; // get our handler.
            private Thread[] threads; // have a list of threads.
            private int currIndex;
            private Boolean gameOver;
            private Boolean winnerFound;
            private Bundle transferData;

            public MasterThread()
            {

                winnerFound = false;
                gameOver = false;
                threads = new Thread[2];
                currIndex = 0; // set the index to 0 at first.
                PlayerTwoThread.resetCoords(); // need to manually reset the coordinates for the player two class.
                transferData = new Bundle();

            }

            @SuppressLint("HandlerLeak")
            public void run()
            {
                Looper.prepare();

                masterHandler = new Handler() {

                    public void handleMessage(Message msg) {

                       resetGrid();

                        if (gameOver) { // if we get a message, but we are in gameover.

                            try {

                                if (winnerFound) { // if we found a winner..
                                    runOnUiThread(new Runnable() {

                                        @Override // run the dialog box.
                                        public void run() { // update the image of threads

                                            String nameOfWinner = "";


                                            nameOfWinner = threads[currIndex - 1].getName(); // get the name of the last player (it had to be the last player if we are in gameover state).


                                            dialog.setTitle("Game Over! " + nameOfWinner + " Wins! ");
                                            dialog.show();
                                        }


                                    });
                                }

                                threads[0].join(); // join both threads.
                                threads[1].join();





                            } catch (java.lang.InterruptedException ex) {

                            }


                        }
                        else if (((String)msg.obj).equals("Done")) // when a specific thread is done, we can check this.
                        {

                            int XVal = msg.getData().getInt("XVal"); // get both the x and y coordinates from the last thread.
                            int YVal = msg.getData().getInt("YVal");


                            gameOver = winnerFound = checkStatus(threads[currIndex-1], XVal, YVal); // check the status (if disaster, success, near miss, etc.)

                            resetGrid(); // reset the grid and possibly add a point.


                            if (isAuto) // if we are in auto..
                            {
                                try
                                {
                                    Thread.sleep(500); // run sleep and then call run again to get the next thread.
                                    Message newMessage = new Message();

                                    newMessage.obj = "RunAgain";
                                    masterHandler.sendMessage(newMessage);

                                }
                                catch(java.lang.InterruptedException ex)
                                {

                                }
                            }

                                if (winnerFound) { // if we found a winner..
                                    runOnUiThread(new Runnable() {

                                        @Override // run the dialog box.
                                        public void run() { // update the image of threads

                                            gameOver = true;
                                            winnerFound = false;
                                            String nameOfWinner = "";


                                            nameOfWinner = threads[currIndex - 1].getName(); // get the name of the last player (it had to be the last player if we are in gameover state).


                                            dialog.setTitle("Game Over! " + nameOfWinner + " Wins! ");
                                            dialog.show();

                                        }


                                    });
                                }



                        }
                        else if (((String)msg.obj).equals("RunAgain")) { // if we get run again..
                            resetGrid(); // reset the grid.


                            if (currIndex == 2) { // Run next thread.
                                currIndex = 0;
                                //transferData = new Bundle(); // reset the status back to nothing.
                            }

                            threads[0] = new PlayerOneThread("Player 1", masterHandler, R.drawable.redcircle); // make two threads and set priorties.
                            threads[0].setPriority(1);
                            threads[1] = new PlayerTwoThread("Player 2", masterHandler, R.drawable.bluecircle, transferData);
                            threads[1].setPriority(2);

                            threads[currIndex].start(); // start whichever thread at the current index.
                            currIndex++; // increment the index up.
                        }
                        else if (((String)msg.obj).equals("Near miss")) // set bundle data to send to playertwo. We only update if its a near miss or close guess.
                        {
                            int lastX;
                            int lastY;

                            if (threads[currIndex - 1] instanceof PlayerOneThread)
                            {
                                lastX = ((PlayerOneThread)threads[currIndex-1]).getX();
                                lastY = ((PlayerOneThread)threads[currIndex-1]).getY();
                            }
                            else
                            {
                                lastX = ((PlayerTwoThread)threads[currIndex-1]).getX();
                                lastY = ((PlayerTwoThread)threads[currIndex-1]).getY();
                            }


                            transferData = new Bundle();
                            transferData.putString("Status", "Near miss"); // update near miss coordinates.
                            transferData.putInt("XVal", lastX);
                            transferData.putInt("YVal", lastY);
                        }
                        else if (((String)msg.obj).equals("Close guess") && (transferData.getString("Status") == null)) // set bundle data to send to player two only do this if the guess hasnt been recorded yet.
                        {
                            int lastX;
                            int lastY;

                            if (threads[currIndex - 1] instanceof PlayerOneThread)
                            {
                                lastX = ((PlayerOneThread)threads[currIndex-1]).getX();
                                lastY = ((PlayerOneThread)threads[currIndex-1]).getY();
                            }
                            else
                            {
                                lastX = ((PlayerTwoThread)threads[currIndex-1]).getX();
                                lastY = ((PlayerTwoThread)threads[currIndex-1]).getY();
                            }


                            transferData = new Bundle();
                            transferData.putString("Status", "Close guess");
                            transferData.putInt("XVal",lastX);
                            transferData.putInt("YVal", lastY);
                        }

                    }
                };



                if (!gameOver) { // if its not gameover, we keep looping.
                    Looper.loop();
                }

            }

            synchronized public void joinAll() // set gameover to true.
            {
                gameOver = true;
            }


            public Handler getMasterHandler() // get hte master handler.
            {

                return masterHandler;
            }



        }





    private class ImageAdapter extends BaseAdapter
    {

        private Context context;

        public ImageAdapter(Context c)
        {

            context = c;



        }

        public int getCount()
        {
            return 100;
        }

        public Object getItem(int x)
        {
            return grid[x];
        } // get an item at a position

        public long getItemId(int x)
        {
            return x;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) { //

            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(100,100));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // set at center.
            imageView.setPadding(2,2,2,2); // set padding
            imageView.setImageResource(images.get(position).getImage()); // set the image specifically found at that position in the arraylist using the imageobject.
            return imageView; // return the imageview.

        }


    }

    public static ImageObj[][] getGrid() // get the grid.
    {
        return grid;
    }

    synchronized public static TextView getTextView() // get the textview.
    {
        return textView;
    }

    synchronized public static void setCoord(int x, int y, ImageObj image) // set the coordinates of the spot in the grid to the new image and set it to be accessed.
    {
        grid[x][y] = image;
        grid[x][y].setAccessed();

    }

    synchronized public void resetGrid() // reset grid is a glorified updateUI() where it re-adds all the images to arraylist and then changes the gridview.
    {
        runOnUiThread(new Runnable() {

            @Override
            public void run() { // update the image of threads

                images.clear(); // clear everything first.

                for (int x = 0; x < 10; x++) {
                    for (int y = 0; y < 10; y++) {
                        images.add(grid[x][y]); // add them all back.
                    }
                }

                adapter.notifyDataSetChanged(); // notify adapter.
                gridView.invalidateViews(); // invalidate views..
                gridView.setAdapter(adapter); // set the adapter again.
            }



        });
    }


    synchronized public static boolean checkStatus(Thread thread, int xVal, int yVal) // checks status of each move called on by a thread.
    {

        int xCoord = xVal;
        int yCoord = yVal;
        String name = thread.getName(); // get the name of the calling thread.
        int image; //
        Handler handler;

        if (thread instanceof PlayerOneThread)
        {
            image = ((PlayerOneThread) thread).getImage(); // get the image based on if its PlayerOne or two thread.
            handler = ((PlayerOneThread) thread).getMyHandler();
        }
        else
        {
            image = ((PlayerTwoThread)thread).getImage();
            handler = ((PlayerTwoThread) thread).getMyHandler();
        }

        getTextView().append("\n" + name + " guessed (" + xCoord + ", " + yCoord +")."); // append the guess, first.

        if (!getGrid()[yCoord][xCoord].isAccessed()) // if it hasnt been accessed yet..
        {
            setCoord(yCoord, xCoord, new ImageObj(image, yCoord, xCoord)); // put the coordinate down.

            // We then check near misses, close guesses, complete misses, successes, or disasters.

            if ((xCoord == (winningX + 1) && yCoord == (winningY + 1)) || (xCoord == (winningX -1) && yCoord == (winningY - 1)) || (xCoord == (winningX + 1) && yCoord == (winningY - 1))
                    || (xCoord == (winningX - 1) && yCoord == (winningY + 1)) || ((xCoord == winningX) && (yCoord == (winningY + 1) || yCoord == (winningY - 1))) || ((yCoord == winningY) && (xCoord == (winningX + 1) || xCoord == (winningX - 1))))
            {
                getTextView().append( " near miss.");
                Message sender = new Message();
                sender.obj = "Near miss";
                handler.sendMessage(sender);
                return false;
            }
            else if ((xCoord == (winningX + 2) && yCoord == (winningY + 2)) || (xCoord == (winningX -2) && yCoord == (winningY - 2)) || (xCoord == (winningX + 2) && yCoord == (winningY - 2))
                    || (xCoord == (winningX - 2) && yCoord == (winningY + 2)) || ((xCoord == winningX) && (yCoord == (winningY + 2) || yCoord == (winningY - 2))) || ((yCoord == winningY) && (xCoord == (winningX + 2) || xCoord == (winningX - 2))) ||
                ((yCoord + 2 == winningY) && ((xCoord + 1 == winningX) || (xCoord - 1 == winningX) || (xCoord + 2 == winningX) || (xCoord - 2 == winningX))) || ((yCoord - 2 == winningY) && ((xCoord + 1 == winningX) || (xCoord - 1 == winningX) || (xCoord + 2 == winningX) || (xCoord - 2 == winningX))) ||
            ((yCoord - 1 == winningY) && ((xCoord + 1 == winningX) || (xCoord - 1 == winningX) || (xCoord + 2 == winningX) || (xCoord - 2 == winningX))) || ((yCoord + 1 == winningY) && ((xCoord + 1 == winningX) || (xCoord - 1 == winningX) || (xCoord + 2 == winningX) || (xCoord - 2 == winningX))))
            {
                getTextView().append( " close guess.");
                Message sender = new Message();
                sender.obj = "Close guess";
                handler.sendMessage(sender);
                return false;
            }
            else if ((xCoord != winningX || yCoord != winningY))
            {
                getTextView().append( " complete miss.");
                Message sender = new Message();
                sender.obj = "Complete miss";
                handler.sendMessage(sender);
                return false;
            }
            else if ((xCoord == winningX && yCoord == winningY))
            {
                getTextView().append("\n Success! " + name + " wins!");
                Message sender = new Message();
                sender.obj = "Success";
                handler.sendMessage(sender);
                return true;
            }

        }
        else // if it was already accessed.
        {
            Message sender = new Message();
            sender.obj = "Disaster";
            handler.sendMessage(sender);
            getTextView().append(" A disaster has occurred.");
            return false;
        }

        return false;
    }


    public void restartGame() // restart game will end the game on the master thread. finish this activity and start it again.
    {
        Intent i = getIntent();
        masterThread.joinAll();
        finish();
        startActivity(i);
    }

    public void setUpDialog()
    {
        builder = new AlertDialog.Builder(this); // make a dialog box to check when the game is won.

        builder.setMessage("Do you want to start a new game?");
        builder.setTitle("Game Over");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { // set positive button to start a new game.
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restartGame();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() { // negative button will do nothing.
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });


        dialog = builder.create(); // create the dialog box.
    }

    public void setUpGrid()
    {
        for ( int x = 0; x < 10; x++) // make the grid
        {
            for (int y = 0; y < 10; y++)
            {
                if (x == winningY && y == winningX) // if we are at the game-winning coordinates..
                { // gopher
                    grid[x][y] = new ImageObj(R.drawable.gopher, x, y); // set the gopher picture at that spot.
                }
                else {
                    grid[x][y] = new ImageObj(R.drawable.square, x, y); // other wise, we just add a square as the image.
                }
            }
        }

        for (int x = 0; x < 10; x++) // add the images to the arraylist in sequential order so we can convert the array adapter to this.
        {
            for (int y = 0; y < 10; y++)
            {
                images.add(grid[x][y]);
            }
        }
    }

    public void setUpButtons()
    {
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
                public void onClick(View v) {// set auto to false. and call run again to get the next message.

                synchronized (this) {

                   isAuto = !isAuto;

                   if (isAuto)
                   {
                       myButton.setText("Change to Manual "); // autoplay is turned on.
                       toastMessage.setText(R.string.ToastName2);
                       Toast.makeText(getApplicationContext(), "Autoplay enabled.", Toast.LENGTH_SHORT).show();
                       auto.setClickable(false);


                       Message msg = new Message();
                       msg.obj = "RunAgain"; // run again is specified to run the next player.

                       Handler handle = masterThread.getMasterHandler();

                       handle.sendMessage(msg);

                   }
                   else
                   {
                       myButton.setText("Change to Autoplay");
                       toastMessage.setText(R.string.ToastName1);
                       Toast.makeText(getApplicationContext(), "Move by Move enabled.", Toast.LENGTH_SHORT).show();
                       auto.setClickable(true); // set the button to be clickable again.
                   }


                }





                }
        }); // set up the button listener.

        restart.setOnClickListener(new View.OnClickListener() { // restart game when clicked.
            @Override
            public void onClick(View v) {
                restartGame();

            }
        });

        auto.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) { // when automatic is pressed, set auto to true and run the next message.

                { // send a run message.

                    if (!isAuto)
                    {
                        Message msg = new Message();
                        msg.obj = "RunAgain"; // run again is specified to run the next player.

                        Handler handle = masterThread.getMasterHandler();

                        handle.sendMessage(msg);
                    }
                }


            }
        });
    }



}
