package bridge.common;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/** @author Bryte Morio */
@Slf4j
public final class ConfigObject {
  private static final Charset CHARSET = StandardCharsets.ISO_8859_1;

  public static UnmodifiableConfig CONFIG;

  static {
    try {

      String manualConfigFilePath = System.getProperty("configfile");
      String configFileFromResources = readResourceConfigFile("config.json");

      if (manualConfigFilePath == null) {
        log.info("Resource Json Config file: " + configFileFromResources);
        Path configFile = Paths.get(configFileFromResources);
        System.out.println(configFile);
        FileConfig temp = FileConfig.of(configFile);
        assert (temp == null);
        temp.load();
        log.info(String.valueOf(temp));

      } else {
        Path manualConfigFile = Paths.get(manualConfigFilePath);
        FileConfig temp = FileConfig.of(manualConfigFile);
        temp.load();
        CONFIG = temp;
      }
    } catch (Exception exp) {
      log.trace(String.valueOf(exp));
    }
  }

  private ConfigObject() {}

  private static String readResourceConfigFile(String filename) throws URISyntaxException {
    return Objects.requireNonNull(
            ConfigObject.class.getClassLoader().getResource(filename), "File not Found")
        .getPath();
  }
}
