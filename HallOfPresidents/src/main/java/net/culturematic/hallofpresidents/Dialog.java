package net.culturematic.hallofpresidents;

public class Dialog {
    public Dialog(String commandName, String dialog, UIControls.Direction facing) {
        mCommandName = commandName;
        mDialog = dialog;
        mFacing = facing;
    }

    public String getCommandName() {
        return mCommandName;
    }

    public String getDialog() {
        return mDialog;
    }

    public UIControls.Direction getFacing() { return mFacing; }

    private final String mCommandName;
    private final String mDialog;
    private final UIControls.Direction mFacing;
}