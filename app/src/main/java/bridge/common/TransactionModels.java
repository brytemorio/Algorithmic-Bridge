package bridge.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

    public void hasCompleted(TransactionAttempt attempt, String transactionID) {
      // TODO: Implementation details later
    }

    public void incrementRetries() {
      tries = tries + 1;
      ZoneId zoneId = ZoneId.of("Etc/Zulu");
      lastModifiedOn.of(LocalDateTime.now(), zoneId).format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
  }
}
