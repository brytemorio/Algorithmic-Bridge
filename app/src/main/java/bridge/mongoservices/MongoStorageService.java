package bridge.mongoservices;

import static bridge.common.ConfigFileObj.CONFIG;
import java.util.Objects;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import lombok.Getter;

public class MongoStorageService {
  private static UnmodifiableConfig configfile = CONFIG;

  // the name of database to be used in MongoDB
  @Getter private String database;

  @Getter private String dbHostName;
  @Getter private int dbPort;
  @Getter private String username;
  @Getter private String password;

  @Getter private String connectionURL;

  public MongoStorageService() {
    this.database =
        Objects.requireNonNullElse(
            MongoStorageService.configfile.get("Bridge.database.name"), "Algo-bridge");
    this.dbHostName =
        Objects.requireNonNullElse(
            MongoStorageService.configfile.get("Bridge.database.hostname"), "localhost");
    this.dbPort =
        Objects.requireNonNullElse(
            MongoStorageService.configfile.get("Bridge.database.port"), 27017);
    this.username = MongoStorageService.configfile.get("Bridge.database.username");
    this.password = MongoStorageService.configfile.get("Bridge.database.password");

    if (this.username.equals(null) || this.password.equals(null)) {
      this.connectionURL = "mongodb://" + this.dbHostName + ":" + this.dbPort + "/" + this.database;
    } else {
      this.connectionURL =
          "mongodb://"
              + this.username
              + ":"
              + this.password
              + "@"
              + this.dbHostName
              + ":"
              + this.dbPort
              + "/"
              + this.database;
    }
  }
}
