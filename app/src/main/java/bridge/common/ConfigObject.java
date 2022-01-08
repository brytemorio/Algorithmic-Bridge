package bridge.common;

import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.json.JsonFormat;
import lombok.extern.slf4j.Slf4j;

/** @author Bryte Morio */
// TODO: Replace @Slf4j with a properly configured logger class;
@Slf4j
public final class ConfigObject {
  private static final Charset CHARSET = StandardCharsets.ISO_8859_1;
  public static UnmodifiableConfig CONFIG;

  static {
    try {
      String manualConfigFilePath = System.getProperty("configfile");
      InputStream configFileFromResources = readResourceConfigFile("config.json");
      ConfigFormat<?> jsonFormat = JsonFormat.fancyInstance();
      ConfigParser<?> jsonParser = jsonFormat.createParser();
      if (manualConfigFilePath == null) {
        CONFIG = jsonParser.parse(configFileFromResources, CHARSET);
      } else {
        Path manualConfigFile = Paths.get(manualConfigFilePath);
        CONFIG = jsonParser.parse(manualConfigFile, FileNotFoundAction.THROW_ERROR, CHARSET);
      }
    } catch (Exception exp) {
      // TODO: Replace with a logger
      System.out.println(exp);
    }
  }

  private ConfigObject() {}

  private static InputStream readResourceConfigFile(String filename) throws NullPointerException {
    InputStream configfileStream =
        ConfigObject.class.getClassLoader().getResourceAsStream(filename);
    if (configfileStream == null) throw new NullPointerException("File Not found");
    return configfileStream;
  }
}
