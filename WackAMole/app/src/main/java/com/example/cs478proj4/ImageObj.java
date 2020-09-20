package com.example.cs478proj4;
// Mission Marcus Image Obj Class
// This class identifies every object in the grid array. Each object has an image (either a red or blue circle, along with an xCoordinate and yCoordinate. We can also tell if this location has been accessed.

public class ImageObj
{

    private int image;
    private int xCoord;
    private int yCoord;
    private Boolean alreadyAccessed;

    public ImageObj(int i, int x, int y)
    {
        this.image = i;
        this.xCoord = x;
        this.yCoord = y;
        alreadyAccessed = false; //automatically set this accessed to false.

    }

    public int getImage()
    {
        return image;
    } // retrieve the image to be put on the grid.

    public void setAccessed()
    {
        alreadyAccessed = true;
    } // set accessed to true if this spot has been accessed already.

    public boolean isAccessed()
    {
        return alreadyAccessed;
    } // return if its been accessed.

}
