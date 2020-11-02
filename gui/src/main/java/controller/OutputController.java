package controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import static model.Constants.DATE_FORMAT;

public class OutputController {

    private String output;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);

    public void setOutput(String output) {
        this.output = output;
    }

    public void saveToFile() {
        try {
            String home = System.getProperty("user.home");
            String time = simpleDateFormat.format(new Date());
            String path = home + File.separator + time;
            Files.write(Paths.get(path), output.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
