package model;

public interface Constants {
    public static final String OUTPUT_IS_TOO_LARGE = "Output is too large and cannot be displayed here. Save it to file to read it.";
    public static final String CODE_PLACEHOLDER = "var n = 0\n" +
            "var m = 100000\n" +
            "var seq = {n, m}\n" +
            "var squares = map(seq, i -> i ^ 2)\n" +
            "print DONE\n";

    public static final String INTERPRETATION = "Interpretation...";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String MENU_TITLE = "File";
    public static final String SAVE_OUTPUT_ITEM_TITLE = "Save output to file";
    public static final String SAVE_OUTPUT_ITEM_DESCRIPTION = "Create file in user directory and save output content to this file";
}
