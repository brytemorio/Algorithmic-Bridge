package bridge.exceptions;

// TODO: Improve this Execptions class
public class AssetNotFoundException extends Exception {
    public AssetNotFoundException(String msg){
        super(msg);
        super.fillInStackTrace();
    }
}
