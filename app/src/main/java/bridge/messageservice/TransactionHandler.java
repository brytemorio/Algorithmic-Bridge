package bridge.messageservice;

import com.lmax.disruptor.EventHandler;
import java.math.BigInteger;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.agrona.collections.Object2ObjectHashMap;

@Slf4j
public class TransactionHandler
    implements EventHandler<Object2ObjectHashMap<BigInteger, ArrayList<String>>> {

  private String chainIdentifier;

  public TransactionHandler() {}

  public TransactionHandler(String chainIdentifier) {
    this.chainIdentifier = chainIdentifier;
  }

  @Override
  public void onEvent(
      Object2ObjectHashMap<BigInteger, ArrayList<String>> event, long sequence, boolean endOfBatch)
      throws Exception {
    log.info(chainIdentifier + ":" + " List of transactions Id => " + event.toString());
  }
}
