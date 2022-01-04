package Bridge.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlParser;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/** @author: Bryte Morio */
@Slf4j
public final class ConfigParser {
  private static final Charset CHARSET = Charset.forName("ISO8859-1");

  public static CommentedConfig CONFIG;

  static {
    try {

      String manualConfigFilePath = System.getProperty("configfile");
      InputStream ConfigFileFromResources = readResourceConfigFile("config.toml");
      if (manualConfigFilePath == null) {
        CONFIG = new TomlParser().parse(ConfigFileFromResources, CHARSET);
      } else {
        Path manualConfigFile = Paths.get(manualConfigFilePath);
        CONFIG = new TomlParser().parse(manualConfigFile, FileNotFoundAction.THROW_ERROR, CHARSET);
      }
    } catch (Exception exp) {
      log.debug(String.valueOf(exp));
    }
  }

  private ConfigParser() {}

  private static InputStream readResourceConfigFile(String filename) {
    InputStream inputStream = ConfigParser.class.getClassLoader().getResourceAsStream(filename);
    if (inputStream == null) throw new NullPointerException("File: " + filename + " not found");
    return inputStream;
  }
}
