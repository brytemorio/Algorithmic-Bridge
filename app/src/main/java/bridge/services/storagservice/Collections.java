package bridge.services.storagservice;

public enum Collections
{
  BlOCK_HEIGHT_STORAGE("Block_Height_Store"), ADDRESS_MAPPING_STORAGE(
    "Address_Mapping_Store"), VALID_TRASANCTION_STORAGE(
    "Transaction_AttemptList_Store"), TRANSACTION_POLLING_STATE(
    "transaction_polling_state"), ASSET_STORAGE("Assets_Store"),

  /*
   * Bridge internal wallets(public and private Keys pairs per blockchains supported used for
   * handling transactions
   */
  WALLET_STORAGE("Wallet_Store");

  private String name;

  private Collections(final String name)
  {
    this.name = name;
  }


  public String toString()
  {
    return name;
  }

}
