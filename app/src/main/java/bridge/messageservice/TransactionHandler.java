package bridge.messageservice;

import com.lmax.disruptor.EventHandler;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionHandler implements EventHandler<ArrayList<String>> {

  @Override
  public void onEvent(ArrayList<String> event, long sequence, boolean endOfBatch) throws Exception {
    // implementation for later
  }
}
