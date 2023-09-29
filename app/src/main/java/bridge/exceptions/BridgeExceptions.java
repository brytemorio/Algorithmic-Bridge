package bridge.exceptions;

import java.io.Serial;

public class BridgeExceptions {

  private BridgeExceptions() {}

  public static class AssetNotFoundException extends Exception {
    private static final long serialVersionUID = 1053304875646104669L;

    public AssetNotFoundException(final String msg) {
      super(msg);
    }
  }

  public static class ChainNodeException extends Exception {
    private static final long serialVersionUID = -122950159451235638L;

    public ChainNodeException(final String msg, final int statusCode, final String info) {
      super(msg + " " + statusCode + " " + info + " ");
    }
  }

  public static class ChainAddressNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -7518046457242084859L;

    public ChainAddressNotFoundException(final String msg) {
      super(msg);
    }
  }

  public static class InvalidAssetIDException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8416989915174220244L;

    public InvalidAssetIDException(final String msg) {
      super(msg);
    }
  }

  public static class ObjectCreationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 8814384019065260156L;

    public ObjectCreationException(final String msg) {
      super(msg);
    }
  }

  public static class MultipleGateWayReceiverException extends RuntimeException {
    public MultipleGateWayReceiverException(final String trx)
    {
      super("Encountered a transaction (" + trx + ") with multiple receiver addresses. The " +
          "Gateway cannot handle this.");
    }
  }

  public static class ConfigurationStoreNotFoundException extends  RuntimeException{
    public ConfigurationStoreNotFoundException(final String msg)
    {
      super(msg);
    }
  }
}
