package Bride.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlParser;
import lombok.extern.slf4j.Slf4j;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigCache;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ConfigParser {
  public static final Charset CHARSET = Charset.forName("ISO8859-1");
  ObjectConverter objectConverter = new ObjectConverter();

  static {
    try {

      Path ConfigFile = Paths.get("config.toml");
      CommentedConfig commentedConfigFileParser =
          new TomlParser().parse(ConfigFile.getFileName(), FileNotFoundAction.THROW_ERROR, CHARSET);

    } catch (Exception exp) {
      log.error(String.valueOf(exp));
    }
  }

  public static void main(String[] args) {
    ConfigParser configParser = new ConfigParser();
  }

  private static InputStream getConfigFile(String filename) {
    InputStream inputStream = ConfigParser.class.getClassLoader().getResourceAsStream(filename);
    if (inputStream == null)
      throw new NullPointerException(String.format("File: %s not found", filename));
    return inputStream;
  }
}