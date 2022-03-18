package bridge.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.bson.types.ObjectId;
import lombok.Data;

public class TransactionModels {

  private TransactionModels() {}

  @Data
  public static class MappedAddress {
    /* object used internally by the bridge in creating tunnels across blockchains */

    ObjectId id;
    private String address;
    private String assetName;

    public MappedAddress() {}

    public MappedAddress(final String Address, final String assetName) {
      this.address = Address;
      this.assetName = assetName;
    }
  }

  @Data
  public static class TransactionSender {

    private String address;

    public TransactionSender() {}

    public TransactionSender(String address) {
      this.address = address;
    }
  }

  @Data
  public static class TransactionReceiver {

    private String address;
    private double amount;

    public TransactionReceiver() {}

    public TransactionReceiver(String address, double amount) {
      this.address = address;
      this.amount = amount;
    }
  }

  @Data
  public static class Transaction {

    private String transactionID;
    private List<TransactionReceiver> receivers;
    private List<TransactionSender> senders;

    private boolean afffirmTransaction;
    private int retries;

    public Transaction() {}

    public Transaction(
        String transactionID,
        List<TransactionReceiver> receivers,
        List<TransactionSender> senders) {
      this.transactionID = transactionID;
      this.receivers = receivers;
      this.senders = senders;
    }

    public Transaction(String transactionID, List<TransactionReceiver> receivers) {
      this.transactionID = transactionID;
      this.receivers = receivers;
    }

    public void markAsDone() {
      setAfffirmTransaction(true);
    }

    public void incrementRetries() {
      this.retries += 1;
    }
  }

  @Data
  public static class TransactionAttemptListTrigger {

    private String trxId;
    private int receiver;
    private String currency;
    private List<TransactionSender> senders;

    public TransactionAttemptListTrigger() {}

    public TransactionAttemptListTrigger(
        String transactionId, int receiver, String currency, List<TransactionSender> senders) {
      this.trxId = transactionId;
      this.receiver = receiver;
      this.currency = currency;
      this.senders = senders;
    }

    public TransactionAttemptListTrigger(String transactionId, int receiver, String currency) {
      this.trxId = transactionId;
      this.receiver = receiver;
      this.currency = currency;
    }
  }

  @Data
  public static class TransactionAttemptReceiver {

    private String address;
    private Double amount;

    public TransactionAttemptReceiver() {}

    TransactionAttemptReceiver(String address, Double amount) {
      this.address = address;
      this.amount = amount;
    }
  }

  @Data
  public static class TransactionAttempt {

    private String sender;
    private List<TransactionAttemptReceiver> receivers;
    private double fee;
    private String currency;

    public TransactionAttempt() {}

    TransactionAttempt(
        String sender, List<TransactionAttemptReceiver> receivers, double fee, String currency) {
      this.sender = sender;
      this.receivers = receivers;
      this.fee = fee;
      this.currency = currency;
    }

    public Double overallAmount() {
      // TODO: Implement overallAmount() if turns out to be necessary
      return null;
    }
  }

  @Data
  public static class TransactionAttemptList {

    private String Id;
    private TransactionAttemptListTrigger trigger;
    private List<TransactionAttempt> attempts;
    private List<?> transactions;
    private ZonedDateTime createdOn;
    private ZonedDateTime lastModifiedOn;
    private Integer tries;
    private String transactionAttemptID;

    public TransactionAttemptList() {}

    TransactionAttemptList(
        TransactionAttemptListTrigger trigger,
        List<TransactionAttempt> attempts,
        List<?> transactions,
        ZonedDateTime createdOn,
        ZonedDateTime lastModifiedOn,
        Integer tries,
        String transactionAttemptID) {
      this.Id = UUID.randomUUID().toString();
      this.trigger = trigger;
      this.attempts = attempts;
      this.transactions = transactions;
      this.createdOn = createdOn;
      this.lastModifiedOn = lastModifiedOn;
      this.tries = tries;
      this.transactionAttemptID = transactionAttemptID;
    }

    TransactionAttemptList(
        TransactionAttemptListTrigger trigger, List<TransactionAttempt> attempts, Integer tries) {
      this.Id = UUID.randomUUID().toString();
      this.trigger = trigger;
      this.attempts = attempts;
      this.tries = tries;
    }

    /*
     * marks the next transaction attempt in the list of transactions to be handled as completed,
     * once the transaction has been successful handled
     *
     * @param attempt The list of transactions to be handled
     *
     * @param transactionID The corresponding transaction ID for each transaction in list
     */
    public void markAsCompleted(TransactionAttempt attempt, String transactionID) {
      // TODO: Implementation details later
    }

    /*
     * Increments the retry counter for a failing transaction until the maximum number of retries is
     * reached, at which point the transaction is abandoned and stored as a failed transaction in
     * the transaction history.
     */
    public void incrementRetries() {
      tries = tries + 1;
      ZoneId zoneId = ZoneId.of("Etc/Zulu");
      lastModifiedOn
          .of(LocalDateTime.now(), zoneId)
          .format(DateTimeFormatter.RFC_1123_DATE_TIME); // Todo:
    }

    /*
     * Retrieve the next transaction from the list of transactions to be handled.
     *
     *
     */
    public TransactionAttempt nextIncompleteAttempt() {
      // : TODO: Implementation detatils for later
      return null;
    }

    /**
     * checks if the current batch of transactions has all been handled
     *
     * @return true|false
     */
    public boolean hasCompleted() {
      // : Todo: Implementation details for later
      return true;
    }
  }
}
