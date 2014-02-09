package net.culturematic.hallofpresidents;

public interface Screen {
    public String getHelpMessage();

    public void update(final long milliTime, InputEvents.TouchSpot[] touchSpots);

    public Screen nextScreen();

    /**
     * @return True if the game is over, and control should return to the Game Menu
     */
    public boolean done();
}
