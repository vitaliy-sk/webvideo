package org.dd.webvideo;

import org.dd.webvideo.service.ScreenshotVideoService;
import org.dd.webvideo.service.SeleniumService;

import java.io.File;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.toString());

    private static final int DEFAULT_WIDTH = 1920;
    private static final int DEFAULT_HEIGHT = 1080;
    private static final int DEFAULT_FPS = 30;
    private static final String DEFAULT_FORMAT = "mp4";

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
        ScreenshotVideoService screenshotVideoService = new ScreenshotVideoService(width, height, DEFAULT_FORMAT, DEFAULT_FPS);

        File screenshotFile = seleniumService.getUrlScreenshot(url);
        LOG.info(String.format("Screenshot captured to %s", screenshotFile.getAbsolutePath()));

        final File videoFile = screenshotVideoService.prepareVideo(screenshotFile, durationSeconds);
        LOG.info(String.format("Video saved to %s", videoFile.getAbsolutePath()));

    }

}
