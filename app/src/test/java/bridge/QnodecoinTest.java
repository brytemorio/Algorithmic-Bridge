package bridge;

import static org.assertj.core.api.Assertions.*;

import bridge.blockchains.bitcoinchains.QnodecoinChainI;
import java.net.MalformedURLException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

@Slf4j
public class QnodecoinTest {
  public static QnodecoinChainI qnodecoinChainI;

  @BeforeAll
  public static void SetupContext() {
    try {
      qnodecoinChainI = new QnodecoinChainI();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }

  @Test
  public static void testBlockHeight() {
    assertThat(qnodecoinChainI.getBlockHeight()).isInstanceOf(Integer.class);
  }
}
