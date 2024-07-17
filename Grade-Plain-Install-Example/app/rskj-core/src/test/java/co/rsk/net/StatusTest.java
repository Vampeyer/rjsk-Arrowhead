package co.rsk.net;

import co.rsk.core.BlockDifficulty;
import org.ethereum.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

/**
 * Created by ajlopez on 26/08/2017.
 */
class StatusTest {
    @Test
    void createWithOriginalArguments() {
        byte[] hash = TestUtils.generateBytes("hash",32);

        Status status = new Status(42, hash);

        Assertions.assertEquals(42, status.getBestBlockNumber());
        Assertions.assertNotNull(status.getBestBlockHash());
        Assertions.assertArrayEquals(hash, status.getBestBlockHash());
        Assertions.assertNull(status.getBestBlockParentHash());
        Assertions.assertNull(status.getTotalDifficulty());
    }

    @Test
    void createWithCompleteArguments() {
        byte[] hash = TestUtils.generateBytes("hash",32);
        byte[] parentHash = TestUtils.generateBytes("parentHash",32);

        Status status = new Status(42, hash, parentHash, new BlockDifficulty(BigInteger.TEN));

        Assertions.assertEquals(42, status.getBestBlockNumber());
        Assertions.assertNotNull(status.getBestBlockHash());
        Assertions.assertArrayEquals(hash, status.getBestBlockHash());
        Assertions.assertNotNull(status.getBestBlockParentHash());
        Assertions.assertArrayEquals(parentHash, status.getBestBlockParentHash());
        Assertions.assertNotNull(status.getTotalDifficulty());
        Assertions.assertEquals(new BlockDifficulty(BigInteger.TEN), status.getTotalDifficulty());
    }
}
