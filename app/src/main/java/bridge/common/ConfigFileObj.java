package bridge.common;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
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
      InputStream configFileFromResources = BridgeUtils.readResourceAsStream("config.json");
      if (manualConfigFilePath == null) {
        CONFIG = BridgeUtils.getJsonDeserializer().parse(configFileFromResources, CHARSET);
      } else {
        Path manualConfigFile = Paths.get(manualConfigFilePath);
        CONFIG =
            BridgeUtils.getJsonDeserializer()
                .parse(manualConfigFile, FileNotFoundAction.THROW_ERROR, CHARSET);
      }
    } catch (Exception exp) {
      log.trace(String.valueOf(exp));
    }
  }

  private ConfigFileObj() {}
}
