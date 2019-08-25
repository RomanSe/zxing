package ru.semenov;


import boofcv.gui.image.ShowImages;
import com.google.zxing.*;
import com.google.zxing.common.GlobalHistogramBinarizer;
import ru.semenov.binarizers.HybridBinarizer;
import ru.semenov.binarizers.SimpleBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Barcode {

  public static void main(String[] args) throws IOException {
    //String filePath = "/home/roman/projects/img/3504b0151383035383134_full.bmp";
    //String filePath = "/home/roman/projects/img/3504b0151383035383134_full.png";
    //String filePath = "/home/roman/projects/img/23.bmp";
    String filePath = "/home/roman/projects/img/3.bmp";
    //String filePath = "/home/roman/projects/img/2+.bmp";
    final FileInputStream inputStream = new FileInputStream(filePath);
    BufferedImage input0 = ImageIO.read(inputStream);
    Map<DecodeHintType, Object> hintMap = new HashMap<>();
    List<BarcodeFormat> formats = new ArrayList<>();
    //formats.add(BarcodeFormat.QR_CODE);
    formats.add(BarcodeFormat.DATA_MATRIX);
    hintMap.put(DecodeHintType.POSSIBLE_FORMATS, formats);
    hintMap.put(DecodeHintType.TRY_HARDER, true);

    for (int x = 0; x < 8; x++) {
      for (int y = 0; y < 8; y++) {
        BufferedImage input = input0.getSubimage(x, y, input0.getWidth() - x, input0.getHeight() - y);
        //ShowImages.showWindow(input1, "Initial", true);
        final BufferedImageLuminanceSource grayscaleImage = new BufferedImageLuminanceSource(input);
        //ShowImages.showWindow(grayscaleImage.getImage(), "Grayscale", true);
        HybridBinarizer binarizer;
//        try {
//          binarizer = new HybridBinarizer(grayscaleImage);
//          process(binarizer, hintMap, input, true, false);
//          System.out.println("Bingo! x = " + x + " y = " + y);
//        } catch (NotFoundException ex) {
//          //ex.printStackTrace();
//        }

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 255; i++) {
          try {
            binarizer = new SimpleBinarizer(grayscaleImage, i);
            process(binarizer, hintMap, input, false, false);
            str.append(i + " ");
          } catch (NotFoundException e) {
            //System.out.println("Error " + i);
            //e.printStackTrace();
          }
        }
        if (str.length() > 0)
          System.out.println("Bingo! x = " + x + " y = " + y + ":" + str);
      }
    }


  }

  public static void process(HybridBinarizer binarizer, Map<DecodeHintType, Object> hintMap, BufferedImage input, boolean showResults, boolean showImages) throws IOException, NotFoundException {
    BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
    Result result = new MultiFormatReader().decode(binaryBitmap, hintMap);

    System.out.println("====================================================================================");
    if (showResults) {
      System.out.println("Format: " + result.getBarcodeFormat());
      System.out.println("Text read from code: " + result.getText());
      StringBuilder byteString = new StringBuilder();
      System.out.println(result.getRawBytes().length);
      for (Byte b : result.getRawBytes()) {
        byteString.append(b.toString() + " ");
      }
      System.out.println("Bytes :" + byteString);

    }
    if (showImages) {
      WritableRaster raster;
      binarizer.getBlackMatrix();
      BufferedImage averageImage = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
      raster = averageImage.getRaster();
      for (int y = 0; y < input.getHeight(); y++) {
        for (int x = 0; x < input.getWidth(); x++) {
          int[] colour = {binarizer.getBlackPoints()[y / 8][x / 8]};
          raster.setPixel(x, y, colour);
        }
      }
      ShowImages.showWindow(averageImage, "Average", true);
      System.out.println("Average");
      BufferedImage bitmapImage = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
      raster = bitmapImage.getRaster();
      for (int y = 0; y < binaryBitmap.getHeight(); y++) {
        for (int x = 0; x < binaryBitmap.getWidth(); x++) {
          int[] colour = {binaryBitmap.getBlackMatrix().get(x, y) ? 0 : 0xFF};
          raster.setPixel(x, y, colour);
        }
      }
      ShowImages.showWindow(bitmapImage, "Bitmap", true);
    }

  }
}
