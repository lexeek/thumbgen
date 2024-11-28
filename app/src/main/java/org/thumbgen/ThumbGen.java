package org.thumbgen;// Converting the slides of a PPT into Images using Java

import java.util.List;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.imgscalr.Scalr;

public class ThumbGen {
    public static Logger LOG = LogManager.getLogger(ThumbGen.class);

    public static Integer IMAGE_RESIZE_WIDTH = 400;
    public static String GENERATED_IMAGE_EXTENSION = "png";
    public static String[] INPUT_FILE_EXTENSION = new String[]{"ppt", "pptx"};

    public static void main(String args[]) throws IOException {
        IOUtils.setByteArrayMaxOverride(1000000000);
        if (args.length == 0) {
            LOG.warn("File path is not provided");
            System.exit(1);
        }
        String filePath = args[0];
        File file = new File(filePath);

        if (FilenameUtils.getExtension(filePath.toLowerCase(Locale.ROOT)).equals(INPUT_FILE_EXTENSION[0])) {
            LOG.warn("ppt filepath: " + filePath);

            HSLFSlideShow pptFile = new HSLFSlideShow(new FileInputStream(file));
            List<HSLFSlide> fileSlides = pptFile.getSlides();
            createThumbnail(file, pptFile, fileSlides, 1);
        }

        if (FilenameUtils.getExtension(filePath.toLowerCase()).equals(INPUT_FILE_EXTENSION[1])) {
            LOG.warn("pptx filepath: " + filePath);

            XMLSlideShow pptxFile
                    = new XMLSlideShow(new FileInputStream(file));
            List<XSLFSlide> fileSlides = pptxFile.getSlides();
            createThumbnail(file, pptxFile, fileSlides, 1);
        }
    }

    private static void createThumbnail(File file, HSLFSlideShow pptFile, List<HSLFSlide> fileSlides, int imageCount) throws IOException {
        Dimension pgSize = pptFile.getPageSize();
        createImage(file, pgSize, fileSlides, imageCount);
    }

    private static void createThumbnail(File file, XMLSlideShow pptFile, List<XSLFSlide> fileSlides, int imageCount) throws IOException {
        Dimension pgSize = pptFile.getPageSize();
        createImage(file, pgSize, fileSlides, imageCount);
    }

    private static void createImage(File file, Dimension pgSize, List fileSlides, int imageCount) throws IOException {

        BufferedImage img = null;
        int documentSize = fileSlides.size();
        String parentPath = file.getAbsoluteFile().getParent();
        String fileName = file.getName();

        LOG.info("Slides count: " + documentSize);
        if (documentSize > 0) {
            for (int i = 0; i < imageCount; i++) {

                img = new BufferedImage(
                        pgSize.width, pgSize.height,
                        BufferedImage.SCALE_DEFAULT);

                Graphics2D graphics = img.createGraphics();

                graphics.setPaint(Color.white);
                graphics.fill(new Rectangle2D.Float(0, 0, pgSize.width, pgSize.height));


                // draw the images
                if (fileSlides.get(i) instanceof HSLFSlide) {
                    HSLFSlide slide = (HSLFSlide) fileSlides.get(i);
                    slide.draw(graphics);
                }

                if (fileSlides.get(i) instanceof XSLFSlide) {
                    XSLFSlide slide = (XSLFSlide) fileSlides.get(i);
                    slide.draw(graphics);
                }


                String thumbnailPath = parentPath +
                        "/" +
                        fileName +
                        "-" +
                        "thumbnail" +
                        "." +
                        GENERATED_IMAGE_EXTENSION;

                BufferedImage newBufferedImage = new BufferedImage(
                        pgSize.width, pgSize.height,
                        BufferedImage.TYPE_INT_BGR);

                newBufferedImage.createGraphics()
                        .drawImage(newBufferedImage, 0, 0, Color.white, null);

                FileOutputStream out = new FileOutputStream(new File(thumbnailPath));
                javax.imageio.ImageIO.write(Scalr.resize(img, IMAGE_RESIZE_WIDTH), "png", out);
                out.close();
                LOG.info("Thumbnail successfully created: " + thumbnailPath);
            }
        }
    }
}