package bridge;

import static bridge.common.BridgeUtils.readResourceAsStream;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;

class BridgeUtilsTest implements WithAssertions {
  public final String CONFIGFILE = "config.json";

  @Test
  void readResourceAsStreamTest() {
    assertThat(readResourceAsStream(CONFIGFILE)).isNotNull();
  }
}
