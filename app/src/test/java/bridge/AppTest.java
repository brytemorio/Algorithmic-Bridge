package bridge;

import static org.junit.jupiter.api.Assertions.*;

import bridge.common.ConfigObject;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import org.assertj.core.api.WithAssertions;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class AppTest implements WithAssertions {

  @Test
  @DisplayName("ConfigObject: Null Test")
  public void configObjNotNull() {
    UnmodifiableConfig configObject = ConfigObject.CONFIG;
    assertThat(configObject).isNotNull();
  }
}
