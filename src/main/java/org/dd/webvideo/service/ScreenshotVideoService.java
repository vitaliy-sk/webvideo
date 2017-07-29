package org.dd.webvideo.service;

import io.humble.video.Codec;
import io.humble.video.Encoder;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ScreenshotVideoService {

    private static final Logger LOG = Logger.getLogger(ScreenshotVideoService.class.getName());
    private static final PixelFormat.Type DEFAULT_PIXEL_FORMAT = PixelFormat.Type.PIX_FMT_YUV420P;
    private static final String DEFAULT_PRESET = "ultrafast";

    private final int width;
    private final int height;
    private final String formatName;
    private final int fps;

    public ScreenshotVideoService(int width, int height, String formatName, int fps) {
        this.width = width;
        this.height = height;
        this.formatName = formatName;
        this.fps = fps;
    }

    public File prepareVideo(File input, int duration) {
        LOG.info("Starting screenshot video encoding");

        File outputFile = new File("./output-" + System.currentTimeMillis() + "." + formatName);

        try {
            BufferedImage sourceImage = openSourceImage(input);
            recordVideo(sourceImage, outputFile, duration);
        } catch (InterruptedException | IOException e) {
            LOG.severe("Can't record page video");
            throw new IllegalStateException(e);
        }

        return outputFile;
    }

    private void recordVideo(BufferedImage sourceImage, final File outputFilename, final int duration) throws
            InterruptedException, IOException {

        int framesRequired = duration * fps;
        double frameOffset = getFrameOffset(sourceImage, framesRequired);

        final MediaPacket packet = MediaPacket.make();
        final Muxer muxer = Muxer.make(outputFilename.getAbsolutePath(), null, formatName);
        final MuxerFormat muxerFormat = muxer.getFormat();
        final Encoder encoder = createEncoder(muxerFormat);
        muxer.addNewStream(encoder);
        muxer.open(null, null);

        final MediaPicture frameMediaPicture = createMediaPicture(encoder);

        LOG.info(String.format("Start decode. Frames required: %s. Duration: %s sec. Frame offset: %s.", framesRequired, duration, frameOffset));
        MediaPictureConverter converter = null;

        long startTime = System.currentTimeMillis();

        for (int frameIndex = 0; frameIndex < framesRequired; frameIndex++) {
            logFps(frameIndex, startTime);

            int currentFrameOffset = (int) (frameIndex * frameOffset);
            final BufferedImage frameImage = cropImage(sourceImage, new Rectangle(0, currentFrameOffset, width, height));

            if (converter == null)
                converter = MediaPictureConverterFactory.createConverter(frameImage, frameMediaPicture);

            converter.toPicture(frameMediaPicture, frameImage, frameIndex);
            writeFrame(muxer, encoder, frameMediaPicture, packet);
        }

        flushCache(muxer, encoder, packet);
        muxer.close();

        LOG.info(String.format("Decoding done. Took %s msec", (System.currentTimeMillis() - startTime)));

    }

    private void writeFrame(Muxer muxer, Encoder encoder, MediaPicture picture, MediaPacket packet) {
        do {
            encoder.encode(packet, picture);
            if (packet.isComplete())
                muxer.write(packet, false);
        } while (packet.isComplete());
    }

    private void flushCache(Muxer muxer, Encoder encoder, MediaPacket packet) {
        writeFrame(muxer, encoder, null, packet);
    }

    private void logFps(int currentFrame, long startTime) {
        if (currentFrame > 0 && currentFrame % 100 == 0) {
            int fps = (int) (currentFrame / ((System.currentTimeMillis() - startTime) / 1000));
            LOG.info(String.format("Decode video. Frame [%s]. FPS [%s]", currentFrame, fps));
        }
    }

    private MediaPicture createMediaPicture(Encoder encoder) {
        final MediaPicture picture = MediaPicture.make(encoder.getWidth(), encoder.getHeight(), DEFAULT_PIXEL_FORMAT);
        picture.setTimeBase(getFrameRate());
        return picture;
    }

    private Encoder createEncoder(MuxerFormat muxerFormat) {
        final Codec codec = Codec.findEncodingCodec(muxerFormat.getDefaultVideoCodecId());

        Encoder encoder = Encoder.make(codec);
        encoder.setProperty("preset", DEFAULT_PRESET);

        encoder.setWidth(width);
        encoder.setHeight(height);
        encoder.setPixelFormat(DEFAULT_PIXEL_FORMAT);
        encoder.setTimeBase(getFrameRate());

        if (muxerFormat.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
            encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);

        encoder.open(null, null);
        return encoder;
    }

    private Rational getFrameRate() {
        return Rational.make(1, fps);
    }

    private BufferedImage openSourceImage(File input) throws IOException {
        return convertImageToBGR(ImageIO.read(input));
    }

    private double getFrameOffset(BufferedImage sourceImage, final double framesRequired) {
        return (double) (sourceImage.getHeight() - height) / framesRequired;
    }

    private BufferedImage convertImageToBGR(BufferedImage sourceImage) {
        BufferedImage image = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        image.getGraphics().drawImage(sourceImage, 0, 0, null);

        return image;
    }

    private BufferedImage cropImage(BufferedImage src, Rectangle rect) {
        BufferedImage clipped = src.getSubimage(rect.x, rect.y, rect.width, rect.height);
        BufferedImage out = new BufferedImage(clipped.getWidth(), clipped.getHeight(), clipped.getType());
        out.getGraphics().drawImage(clipped, 0, 0, null);
        return out;
    }

}
