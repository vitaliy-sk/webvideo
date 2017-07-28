package org.dd.webvideo;

import org.dd.webvideo.service.SeleniumService;

import java.io.File;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.toString());

    private static final int DEFAULT_WIDTH = 1920;
    private static final int DEFAULT_HEIGHT = 1080;

    public static void main(String[] args) {

        if (args.length != 2){
            LOG.severe("Please provide argument. Example: java -jar webvideo.jar <URL> <DURATION_SECONDS>");
            System.exit(1);
        }

        String url = args[0];

        int durationSeconds = Integer.valueOf(args[1]);
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;

        SeleniumService seleniumService = new SeleniumService(width, height);
        File screenshotFile = seleniumService.getUrlScreenshot(url);

        LOG.info(String.format("Screenshot captured to %s", screenshotFile.getAbsolutePath()));

    }

}
