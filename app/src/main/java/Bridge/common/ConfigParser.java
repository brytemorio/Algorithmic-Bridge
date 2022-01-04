package Bridge.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlParser;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
public final class ConfigParser {
  private static final Charset CHARSET = Charset.forName("ISO8859-1");

  public static CommentedConfig CONFIG;

  static {
    try {

      String manualConfigFile = System.getProperty("configfile");
      Path ConfigFile = Paths.get(Objects.requireNonNullElse(manualConfigFile, "config.toml"));
      CONFIG = new TomlParser().parse(ConfigFile, FileNotFoundAction.THROW_ERROR, CHARSET);
    } catch (Exception exp) {
      log.debug(String.valueOf(exp));
    }
  }

  private ConfigParser() {}


  private static InputStream readPropFile(String filename) {
    InputStream inputStream = ConfigParser.class.getClassLoader().getResourceAsStream(filename);
    if (inputStream == null) throw new NullPointerException("File: " + filename + " not found");
    return inputStream;
  }
}
