package model;

public interface Constants {
    String OUTPUT_IS_TOO_LARGE = "Output is too large and cannot be displayed here. Save it to file to read it.";
    String CODE_PLACEHOLDER = "var n = 0\n" +
            "var m = 100000\n" +
            "var seq = {n, m}\n" +
            "var squares = map(seq, i -> i ^ 2)\n" +
            "out squares\n";

    String INTERPRETATION = "Interpretation...";
    String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    String MENU_TITLE = "File";
    String SAVE_OUTPUT_ITEM_TITLE = "Save output to file";
    String SAVE_OUTPUT_ITEM_DESCRIPTION = "Create file in user directory and save output content to this file";
}
