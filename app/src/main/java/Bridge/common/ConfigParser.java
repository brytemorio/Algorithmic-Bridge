package Bridge.common;

import Bridge.BlockChains.QnodeCoin.QnodecoinChainParams;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.toml.TomlParser;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Slf4j
public final class ConfigParser {
  private static final Charset CHARSET = Charset.forName("ISO8859-1");
  private static Properties PropFile = new Properties();

  public static CommentedConfig CONFIG;


  static {
    try {

      //InputStream inputStreamFromPropFile = readPropFile("main/config.toml");
      //PropFile.load(inputStreamFromPropFile);
      //System.out.println(PropFile.getProperty("configfile"));
      //Path tomlConfigFile = Paths.get(PropFile.getProperty("configfile"));
      Path ConfigFile = Paths.get("config.toml");
      CONFIG = new TomlParser().parse(ConfigFile, FileNotFoundAction.THROW_ERROR, CHARSET);
    } catch (Exception exp) {
      log.debug(String.valueOf(exp));
    }
  }

  private ConfigParser(){}


  private static InputStream readPropFile(String filename){
    InputStream inputStream = ConfigParser.class.getClassLoader().getResourceAsStream(filename);
    if (inputStream == null)
      throw new NullPointerException("File: " + filename + " not found");
    return inputStream;
  }
}
