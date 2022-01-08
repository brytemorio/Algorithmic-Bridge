package bridge.common;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;

@DisplayName("ConfigObject Class Test")
public class ConfigObjectTest implements WithAssertions{
    public static UnmodifiableConfig configObject;

    @BeforeAll
    static void getConfigObject(){
        configObject = bridge.common.ConfigObject.CONFIG;
    }

    @Test
    @DisplayName("Objects of")
    public void configObjectNotNull(){
    assertThat(configObject).isEqualTo(null);
    }

}