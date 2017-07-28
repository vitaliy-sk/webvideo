package org.dd.webvideo.service;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;

public class SeleniumService {

    private final int width;
    private final int height;

    public SeleniumService(int width, int height){
        this.width = width;
        this.height = height;
    }

    public File getUrlScreenshot(String url){
        DesiredCapabilities caps = new DesiredCapabilities();
        RemoteWebDriver driver = new PhantomJSDriver(caps);

        driver.manage().window().setSize(new Dimension(width, height));
        driver.get(url);

        File screenshotFile = driver.getScreenshotAs(OutputType.FILE);
        screenshotFile.deleteOnExit();

        driver.quit();

        return screenshotFile;
    }

}
