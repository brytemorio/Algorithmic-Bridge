package bridge.common;

import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.json.JsonFormat;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

// TODO: Replace @Slf4j with a properly configured logger class;
@Slf4j
public final class ConfigFileObj {
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
      log.trace(String.valueOf(exp));
    }
  }

  private ConfigFileObj() {}

  private static InputStream readResourceConfigFile(String filename) throws NullPointerException {
    InputStream configfileStream =
        ConfigFileObj.class.getClassLoader().getResourceAsStream(filename);
    if (configfileStream == null) throw new NullPointerException("File Not found");
    return configfileStream;
  }
}
