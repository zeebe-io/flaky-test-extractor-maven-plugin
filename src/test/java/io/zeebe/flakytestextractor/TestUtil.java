package io.zeebe.flakytestextractor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

public class TestUtil {

  private static final Logger LOGGER = Logger.getLogger("io.zeebe.flakytestextracto");

  public static void copyClassPatHResourcesToFolder(
      String[] ressourcesInClassPath, File destinationFolder) throws IOException {
    for (String resource : ressourcesInClassPath) {
      URL url = ClassLoader.getSystemResource(resource);
      String urlString = url.toExternalForm();
      String targetName = urlString.substring(urlString.lastIndexOf("/"));
      File destination = new File(destinationFolder, targetName);
      FileUtils.copyURLToFile(url, destination);
      LOGGER.info("Copied " + url + " to " + destination.getAbsolutePath());
    }
  }
}
