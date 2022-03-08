package bridge.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.bson.types.ObjectId;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

public class TransactionModels {

  private TransactionModels() {}

  @Data
  public static class MappedAddress {
    /* object used internally by the bridge in creating tunnels across blockchains*/

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

    public TransactionSender(String address) {
      this.address = address;
    }
  }

  @Data
  public static class TransactionReceiver {

    private String address;

    private double amount;

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
  }

  @Data
  public static class TransactionAttemptReceiver {

    @Setter(AccessLevel.NONE)
    private String address;

    @Setter(AccessLevel.NONE)
    private Double amount;

    TransactionAttemptReceiver(String address, Double amount) {
      this.address = address;
      this.amount = amount;
    }
  }

  @Data
  public static class TransactionAttempt {

    @Setter(AccessLevel.NONE)
    private String sender;

    @Setter(AccessLevel.NONE)
    private List<TransactionAttemptReceiver> receivers;

    @Setter(AccessLevel.NONE)
    private Double fee;

    @Setter(AccessLevel.NONE)
    private String currency;

    TransactionAttempt(
        String sender, List<TransactionAttemptReceiver> receivers, Double fee, String currency) {
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
  public static class TransactionAttemptListTrigger {

    @Setter(AccessLevel.NONE)
    private String transactionID;

    @Setter(AccessLevel.NONE)
    private Integer receiver; // number of receivers? maybe

    @Setter(AccessLevel.NONE)
    private List<TransactionSender> senders;

    @Setter(AccessLevel.NONE)
    private String currency;

    TransactionAttemptListTrigger(
        String transactionID, Integer receiver, List<TransactionSender> senders, String currency) {
      this.receiver = receiver;
      this.senders = senders;
      this.currency = currency;
      this.transactionID = transactionID;
    }

    TransactionAttemptListTrigger(String transactionID, Integer receiver, String currency) {
      this.receiver = receiver;
      this.currency = currency;
      this.transactionID = transactionID;
    }
  }

  @Data
  public static class TransactionAttemptList {

    @Setter(AccessLevel.NONE)
    private TransactionAttemptListTrigger trigger;

    @Setter(AccessLevel.NONE)
    private List<TransactionAttempt> attempts;

    @Setter(AccessLevel.NONE)
    private List<?> transactions;

    @Setter(AccessLevel.NONE)
    private ZonedDateTime createdOn;

    @Setter(AccessLevel.NONE)
    private ZonedDateTime lastModifiedOn;

    @Setter(AccessLevel.NONE)
    private Integer tries;

    @Setter(AccessLevel.NONE)
    private String transactionAttemptID;

    TransactionAttemptList(
        TransactionAttemptListTrigger trigger,
        List<TransactionAttempt> attempts,
        List<?> transactions,
        ZonedDateTime createdOn,
        ZonedDateTime lastModifiedOn,
        Integer tries,
        String transactionAttemptID) {
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
      this.trigger = trigger;
      this.attempts = attempts;
      this.tries = tries;
    }

    /**
     * marks the next transaction attempt in the list of transactions to be handled as completed,
     * once the transaction has been successful handled
     *
     * @param attempt The list of transactions to be handled
     * @param transactionID The corresponding transaction ID for each transaction in list
     */
    public void markAsCompleted(TransactionAttempt attempt, String transactionID) {
      // TODO: Implementation details later
    }

    /**
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

    /**
     * Retrieve the next transaction from the list of transactions to be handled.
     *
     * @return TransactionAttempt
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
