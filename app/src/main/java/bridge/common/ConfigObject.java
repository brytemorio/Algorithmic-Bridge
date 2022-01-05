package bridge.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlParser;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/** @author Bryte Morio */
@Slf4j
public final class ConfigObject {
  private static final Charset CHARSET = StandardCharsets.ISO_8859_1;

  public static CommentedConfig CONFIG;

  static {
    try {

      String manualConfigFilePath = System.getProperty("configfile");
      InputStream configFileFromResources = readResourceConfigFile("config.toml");
      if (manualConfigFilePath == null) {
        CONFIG = new TomlParser().parse(configFileFromResources, CHARSET);
      } else {
        Path manualConfigFile = Paths.get(manualConfigFilePath);
        CONFIG = new TomlParser().parse(manualConfigFile, FileNotFoundAction.THROW_ERROR, CHARSET);
      }
    } catch (Exception exp) {
      log.trace(String.valueOf(exp));
    }
  }

  private ConfigObject() {}

  private static InputStream readResourceConfigFile(String filename) {
    InputStream inputStream = ConfigObject.class.getClassLoader().getResourceAsStream(filename);
    if (inputStream == null) throw new NullPointerException("File: " + filename + " not found");
    return inputStream;
  }
}
