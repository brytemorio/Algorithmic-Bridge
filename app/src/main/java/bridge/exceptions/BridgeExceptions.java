package bridge.exceptions;

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

  public static class ChainAddressNotFoundException extends Exception {
    private static final long serialVersionUID = -7518046457242084859L;

    public ChainAddressNotFoundException(final String msg) {
      super(msg);
    }
  }

  public static class InvalidAssetIDException extends Exception {
    private static final long serialVersionUID = 8416989915174220244L;

    public InvalidAssetIDException(final String msg) {
      super(msg);
    }
  }

  public static class ObjectCreationException extends Exception {
    private static final long serialVersionUID = 8814384019065260156L;

    public ObjectCreationException(final String msg) {
      super(msg);
    }
  }
}
