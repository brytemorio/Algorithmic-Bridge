package bridge.common;

import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

public class TransactionModels {

  private TransactionModels() {}

  @Data
  public static class TransactionSender {
    @Setter(AccessLevel.NONE)
    private String address;

    TransactionSender(String address) {
      this.address = address;
    }
  }

  @Data
  public static class TransactionReceiver {

    @Setter(AccessLevel.NONE)
    private String address;

    @Setter(AccessLevel.NONE)
    private Double amount;

    TransactionReceiver(String address, Double amount) {
      this.address = address;
      this.amount = amount;
    }
  }

  @Data
  public static class Transaction {

    @Setter(AccessLevel.NONE)
    private String transactionID;

    @Setter(AccessLevel.NONE)
    private List<TransactionReceiver> receivers;

    @Setter(AccessLevel.NONE)
    private List<TransactionSender> senders;

    Transaction(
        String transactionID,
        List<TransactionReceiver> receivers,
        List<TransactionSender> senders) {
      this.transactionID = transactionID;
      this.receivers = receivers;
      this.senders = senders;
    }

    Transaction(String transactionID, List<TransactionReceiver> receivers) {
      this.transactionID = transactionID;
      this.receivers = receivers;
    }
  }
}
