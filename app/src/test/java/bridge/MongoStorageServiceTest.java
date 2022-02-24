package bridge;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import bridge.common.MongoStorageService;

class MongoStorageServiceTest implements WithAssertions {
  public MongoStorageService db = new MongoStorageService();

  @Test
  @DisplayName("Username and Password Checks")
  void checkConfigValues() {
    assertThat(db.getPassword()).isEqualTo(null);
    assertThat(db.getUsername()).isEqualTo(null);
  }
}
