package net.culturematic.hallofpresidents;

public interface Screen {
    public void update(final long milliTime, InputEvents.TouchSpot[] touchSpots);

    public Screen nextScreen();

    public void recycle();
}
