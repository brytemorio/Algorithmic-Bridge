package bridge.controller;

import javax.servlet.http.HttpServlet;

public class RequestServlets {

  private RequestServlets() {}

  /**
   * Handles the request for a new address for all bitcoin based chains crypto-coin that is mapped
   * to the user's token-based blockchain (e.g ethereum) address, or returns an existing mapping if
   * it already exists.
   */
  public static class GenerateAddress extends HttpServlet {}

  /** Returns a pending/successful/failed transaction attempt by it's trigger */
  public static class GetAttemptListByTrigger extends HttpServlet {}

  /**
   * Returns a pending/successful/failed transaction attempt by it's ID. This servlet is used
   * internally by the front end framework
   */
  public static class GetAttemptListByID extends HttpServlet {}

  /**
   * Query Sets of Transactions based on some criteria. This servlet is mainly consumed by the
   * frontend framework for is's internal use.
   */
  public static class QueryTransactionAttempt extends HttpServlet {}

  /**
   * The {@code EstablishTunnel} servlet handles users request to establish a tunnel between
   * BitcoinBased chains and Token-Based Chains. The tunnel is need for when users wish to make a
   * transfer from Token-Based chains to Native Block chains (i.e bitcoin-based blockchains).
   */
  public static class EstablishTunnel extends HttpServlet {}

  /**
   * This servlet is used by the frontend framework for when the user request the status of a
   * transaction. It checks to see if the request transaction exists in the database and returns the
   * status of the transaction if it does exists
   */
  public static class CheckForTransaction extends HttpServlet {}
}
