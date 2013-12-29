package net.culturematic.hallofpresidents;

public class Dialog {
    public Dialog(String commandName, String dialog) {
        mCommandName = commandName;
        mDialog = dialog;
    }

    public String getCommandName() {
        return mCommandName;
    }

    public String getDialog() {
        return mDialog;
    }

    private final String mCommandName;
    private final String mDialog;
}