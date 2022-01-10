package bridge;

import bridge.common.ConfigFileObj;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import org.assertj.core.api.WithAssertions;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class AppTest implements WithAssertions {

  @Test
  @DisplayName("ConfigObject: Null Test")
  public void configObjNotNull() {
    UnmodifiableConfig configObject = ConfigFileObj.CONFIG;
    assertThat(configObject).isNotNull();
  }
}
