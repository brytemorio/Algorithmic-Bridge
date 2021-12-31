package Bride.common;

import java.util.List;

public interface IChainQueryService {
    default <T> List<T> getTrxOfBlockAtHeight(int height) {
        return null;
    }

    default String getTrxByID(String trxID) {
        return null;
    }

    default <T> T getTrxHash(int blockHeight) {
        return null;
    }

    default boolean validateAddress(String Address){
        return false;
    }

}
