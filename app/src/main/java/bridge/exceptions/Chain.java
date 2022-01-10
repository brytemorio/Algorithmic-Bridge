package bridge.exceptions;

public class Chain {

  private Chain() {}

  public static class AssetNotFoundException extends RuntimeException {
    public AssetNotFoundException(String msg) {
      super(msg);
    }
  }

  public static class ChainNodeException extends Exception {
    public ChainNodeException(String msg, int statusCode, String info) {
      super(msg + " " + statusCode + " " + info + " ");
    }
  }

  public static class ChainAddressNotFoundException extends Exception {
    public ChainAddressNotFoundException(String msg) {
      super(msg);
    }
  }

  public static class InvalidAssetIDException extends Exception {
    public InvalidAssetIDException(String msg) {
      super(msg);
    }
  }
}
