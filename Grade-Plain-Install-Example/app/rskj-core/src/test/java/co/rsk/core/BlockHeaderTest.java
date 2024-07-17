/*
 * This file is part of RskJ
 * Copyright (C) 2019 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package co.rsk.core;

import co.rsk.config.RskMiningConstants;
import co.rsk.crypto.Keccak256;
import co.rsk.peg.PegTestUtils;
import com.google.common.primitives.Bytes;
import org.ethereum.TestUtils;
import org.ethereum.core.BlockHeader;
import org.ethereum.core.Bloom;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.RLP;
import org.ethereum.util.RLPList;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.System.arraycopy;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class BlockHeaderTest {

    @Test
    void getHashForMergedMiningWithForkDetectionDataAndIncludedOnAndMergedMiningFields() {
        BlockHeader header = createBlockHeaderWithMergedMiningFields(new byte[0], true, new byte[0]);

        byte[] encodedBlock = header.getEncoded(false, false);
        byte[] hashForMergedMiningPrefix = Arrays.copyOfRange(HashUtil.keccak256(encodedBlock), 0, 20);
        byte[] forkDetectionData = TestUtils.generateBytes("forkDetectionData",12);
        byte[] hashForMergedMining = concatenate(hashForMergedMiningPrefix, forkDetectionData);
        byte[] coinbase = concatenate(RskMiningConstants.RSK_TAG, hashForMergedMining);
        header.setBitcoinMergedMiningCoinbaseTransaction(coinbase);
        header.seal();

        byte[] hashForMergedMiningResult = header.getHashForMergedMining();

        MatcherAssert.assertThat(hashForMergedMining, is(hashForMergedMiningResult));
    }

    @Test
    void getHashForMergedMiningWithNoForkDetectionDataAndIncludedOffAndMergedMiningFields() {
        BlockHeader header = createBlockHeaderWithMergedMiningFields(new byte[0], false, new byte[0]);

        Keccak256 hash = header.getHash();
        byte[] hashForMergedMining = header.getHashForMergedMining();

        Assertions.assertNotEquals(hashForMergedMining, hash.getBytes());
    }

    @Test
    void getHashForMergedMiningWithForkDetectionDataAndIncludedOn() {
        byte[] forkDetectionData = TestUtils.generateBytes("forkDetectionData",12);
        BlockHeader header = createBlockHeaderWithNoMergedMiningFields(forkDetectionData, true, new byte[0]);

        byte[] hash = header.getHash().getBytes();
        byte[] hashFirst20Elements = Arrays.copyOfRange(hash, 0, 20);
        byte[] expectedHash = Bytes.concat(hashFirst20Elements, forkDetectionData);

        byte[] hashForMergedMining = header.getHashForMergedMining();

        MatcherAssert.assertThat(expectedHash, is(hashForMergedMining));
    }

    @Test
    void getHashForMergedMiningWithForkDetectionDataAndIncludedOff() {
        byte[] forkDetectionData = TestUtils.generateBytes("forkDetectionData",12);
        BlockHeader header = createBlockHeaderWithNoMergedMiningFields(forkDetectionData, false, new byte[0]);

        byte[] hash = header.getHash().getBytes();
        byte[] hashForMergedMining = header.getHashForMergedMining();

        MatcherAssert.assertThat(hash, is(hashForMergedMining));
    }


    @Test
    void getEncodedWithUmmRootWithMergedMiningFields() {
        byte[] ummRoot = TestUtils.generateBytes("ummRoot",20);
        BlockHeader header = createBlockHeaderWithMergedMiningFields(new byte[0], false, ummRoot);

        byte[] headerEncoded = header.getFullEncoded();
        RLPList headerRLP = RLP.decodeList(headerEncoded);

        MatcherAssert.assertThat(headerRLP.size(), is(20));
    }

    @Test
    void getEncodedWithUmmRootWithoutMergedMiningFields() {
        byte[] ummRoot = TestUtils.generateBytes("ummRoot",20);
        BlockHeader header = createBlockHeaderWithNoMergedMiningFields(new byte[0], false, ummRoot);

        byte[] headerEncoded = header.getFullEncoded();
        RLPList headerRLP = RLP.decodeList(headerEncoded);

        MatcherAssert.assertThat(headerRLP.size(), is(17));
    }

    @Test
    void getEncodedNullUmmRootWithMergedMiningFields() {
        BlockHeader header = createBlockHeaderWithMergedMiningFields(new byte[0], false, null);

        byte[] headerEncoded = header.getFullEncoded();
        RLPList headerRLP = RLP.decodeList(headerEncoded);

        MatcherAssert.assertThat(headerRLP.size(), is(19));
    }

    @Test
    void getEncodedNullUmmRootWithoutMergedMiningFields() {
        BlockHeader header = createBlockHeaderWithNoMergedMiningFields(new byte[0], false, null);

        byte[] headerEncoded = header.getFullEncoded();
        RLPList headerRLP = RLP.decodeList(headerEncoded);

        MatcherAssert.assertThat(headerRLP.size(), is(16));
    }

    @Test
    void getEncodedEmptyUmmRootWithMergedMiningFields() {
        BlockHeader header = createBlockHeaderWithMergedMiningFields(new byte[0], false, new byte[0]);

        byte[] headerEncoded = header.getFullEncoded();
        RLPList headerRLP = RLP.decodeList(headerEncoded);

        MatcherAssert.assertThat(headerRLP.size(), is(20));
    }

    @Test
    void getEncodedEmptyUmmRootWithoutMergedMiningFields() {
        BlockHeader header = createBlockHeaderWithNoMergedMiningFields(new byte[0], false, new byte[0]);

        byte[] headerEncoded = header.getFullEncoded();
        RLPList headerRLP = RLP.decodeList(headerEncoded);

        MatcherAssert.assertThat(headerRLP.size(), is(17));
    }

    @Test
    void getMiningForkDetectionData() {
        BlockHeader header = createBlockHeaderWithMergedMiningFields(new byte[0], true, new byte[0]);

        byte[] encodedBlock = header.getEncoded(false, false);
        byte[] hashForMergedMining = Arrays.copyOfRange(HashUtil.keccak256(encodedBlock), 0, 20);
        byte[] forkDetectionData = TestUtils.generateBytes("forkDetectionData",12);
        byte[] coinbase = concatenate(hashForMergedMining, forkDetectionData);
        coinbase = concatenate(RskMiningConstants.RSK_TAG, coinbase);
        header.setBitcoinMergedMiningCoinbaseTransaction(coinbase);
        header.seal();

        MatcherAssert.assertThat(forkDetectionData, is(header.getMiningForkDetectionData()));
    }

    @Test
    void getUmmRoot() {
        byte[] ummRoot = TestUtils.generateBytes("ummRoot",20);
        BlockHeader header = createBlockHeaderWithUmmRoot(ummRoot);

        MatcherAssert.assertThat(header.getUmmRoot(), is(ummRoot));
    }

    @Test
    void isUMMBlockWhenUmmRoot() {
        byte[] ummRoot = TestUtils.generateBytes("ummRoot",20);
        BlockHeader header = createBlockHeaderWithUmmRoot(ummRoot);

        MatcherAssert.assertThat(header.isUMMBlock(), is(true));
    }

    @Test
    void isUMMBlockWhenNullUmmRoot() {
        byte[] ummRoot = null;
        BlockHeader header = createBlockHeaderWithUmmRoot(ummRoot);

        MatcherAssert.assertThat(header.isUMMBlock(), is(false));
    }

    @Test
    void isUMMBlockWhenEmptyUmmRoot() {
        byte[] ummRoot = new byte[0];
        BlockHeader header = createBlockHeaderWithUmmRoot(ummRoot);

        MatcherAssert.assertThat(header.isUMMBlock(), is(false));
    }

    @Test
    void getHashForMergedMiningWhenUmmRoot() {
        byte[] ummRoot = TestUtils.generateBytes("ummRoot",20);
        byte[] forkDetectionData = TestUtils.generateBytes("forkDetectionData",12);
        BlockHeader header = createBlockHeaderWithUmmRoot(ummRoot, forkDetectionData);

        byte[] encodedBlock = header.getEncoded(false, false);
        byte[] oldHashForMergedMining = HashUtil.keccak256(encodedBlock);
        byte[] leftHash = Arrays.copyOf(oldHashForMergedMining, 20);
        byte[] hashRoot = HashUtil.keccak256(concatenate(leftHash, ummRoot));
        byte[] newHashForMergedMining = concatenate(
                java.util.Arrays.copyOfRange(hashRoot, 0, 20),
                forkDetectionData
        );

        MatcherAssert.assertThat(header.getHashForMergedMining(), is(newHashForMergedMining));
    }

    @Test
    void getHashForMergedMiningWhenEmptyUmmRoot() {
        byte[] ummRoot = new byte[0];

        BlockHeader header = createBlockHeaderWithUmmRoot(ummRoot);

        byte[] encodedBlock = header.getEncoded(false, false);
        byte[] hashForMergedMining = HashUtil.keccak256(encodedBlock);

        MatcherAssert.assertThat(header.getHashForMergedMining(), is(hashForMergedMining));
    }

    @Test
    void getHashForMergedMiningWhenUmmRootWithLengthUnder20() {
        byte[] ummRoot = TestUtils.generateBytes("ummRoot",19);
        BlockHeader header = createBlockHeaderWithUmmRoot(ummRoot);

        Assertions.assertThrows(IllegalStateException.class, header::getHashForMergedMining);
    }

    @Test
    void getHashForMergedMiningWhenUmmRootWithLengthOver20() {
        byte[] ummRoot = TestUtils.generateBytes("ummRoot",21);
        BlockHeader header = createBlockHeaderWithUmmRoot(ummRoot);

        Assertions.assertThrows(IllegalStateException.class, header::getHashForMergedMining);
    }

    /**
     * This case is an error and should never happen in production
     */
    @Test
    void getMiningForkDetectionDataNoDataCanBeFound() {
        BlockHeader header = createBlockHeaderWithMergedMiningFields(new byte[0], true, new byte[0]);

        byte[] forkDetectionData = TestUtils.generateBytes("forkDetectionData",12);
        byte[] coinbase = concatenate(RskMiningConstants.RSK_TAG, forkDetectionData);
        header.setBitcoinMergedMiningCoinbaseTransaction(coinbase);
        header.seal();

        Assertions.assertThrows(IllegalStateException.class, header::getMiningForkDetectionData);
    }

    @Test
    void getMiningForkDetectionDataNoDataMustBeIncluded() {
        BlockHeader header = createBlockHeaderWithMergedMiningFields(new byte[0], false, new byte[0]);

        MatcherAssert.assertThat(new byte[0], is(header.getMiningForkDetectionData()));
    }

    @Test
    void getHashShouldReuseCalculatedValue() {
        BlockHeader header = createBlockHeaderWithMergedMiningFields(new byte[0], false, new byte[0]);
        byte[] headerEncoded = header.getEncoded();

        try (MockedStatic<HashUtil> hashUtilMocked = mockStatic(HashUtil.class)) {
            hashUtilMocked.when(() -> HashUtil.keccak256(headerEncoded)).thenReturn(Keccak256.ZERO_HASH.getBytes());

            for (int i = 0; i < 5; i++) {
                header.getHash();
            }

            hashUtilMocked.verify(() -> HashUtil.keccak256(ArgumentMatchers.any()));
        }
    }

    @Test
    void verifyRecalculatedHashForAmendedBlocks() {
        BlockHeader header = createBlockHeader(new byte[80], new byte[32], new byte[128], new byte[0],
                false, new byte[0], false, false);

        assertArrayEquals(HashUtil.keccak256(header.getEncoded()), header.getHash().getBytes());

        List<Consumer<BlockHeader>> stateModifiers = Arrays.asList(
                h -> h.setBitcoinMergedMiningCoinbaseTransaction(HashUtil.keccak256("BitcoinMergedMiningCoinbaseTransaction".getBytes())),
                h -> h.setBitcoinMergedMiningHeader(HashUtil.keccak256("BitcoinMergedMiningHeader".getBytes())),
                h -> h.setBitcoinMergedMiningMerkleProof(HashUtil.keccak256("BitcoinMergedMiningMerkleProof".getBytes())),
                h -> h.setDifficulty(header.getDifficulty().add(BlockDifficulty.ONE)),
                h -> h.setGasUsed(header.getGasUsed() + 10),
                h -> h.setLogsBloom(HashUtil.keccak256("LogsBloom".getBytes())),
                h -> h.setPaidFees(header.getPaidFees().add(Coin.valueOf(10))),
                h -> h.setReceiptsRoot(HashUtil.keccak256("ReceiptsRoot".getBytes())),
                h -> h.setStateRoot(HashUtil.keccak256("StateRoot".getBytes())),
                h -> h.setTransactionsRoot(HashUtil.keccak256("TransactionsRoot".getBytes())),
                BlockHeader::seal
        );

        stateModifiers.forEach(sm -> {
            sm.accept(header);
            assertArrayEquals(HashUtil.keccak256(header.getEncoded()),
                    header.getHash().getBytes(),
                    "Block header returned invalid hash after modification");
        });
    }

    private BlockHeader createBlockHeaderWithMergedMiningFields(
            byte[] forkDetectionData,
            boolean includeForkDetectionData, byte[] ummRoot) {
        return createBlockHeader(new byte[80], new byte[32], new byte[128],
                forkDetectionData, includeForkDetectionData, ummRoot, false);
    }

    private BlockHeader createBlockHeaderWithNoMergedMiningFields(
            byte[] forkDetectionData,
            boolean includeForkDetectionData, byte[] ummRoot) {
        return createBlockHeader(null, null, null,
                forkDetectionData, includeForkDetectionData, ummRoot, true);
    }

    private BlockHeader createBlockHeaderWithUmmRoot(byte[] ummRoot) {
        return createBlockHeader(null, null, null,
                new byte[0], false, ummRoot, true);
    }

    private BlockHeader createBlockHeaderWithUmmRoot(byte[] ummRoot, byte[] forkDetectionData) {
        return createBlockHeader(null, null, null,
                forkDetectionData, true, ummRoot, true);
    }

    private BlockHeader createBlockHeader(byte[] bitcoinMergedMiningHeader, byte[] bitcoinMergedMiningMerkleProof,
                                          byte[] bitcoinMergedMiningCoinbaseTransaction, byte[] forkDetectionData,
                                          boolean includeForkDetectionData, byte[] ummRoot, boolean sealed) {
        return createBlockHeader(bitcoinMergedMiningHeader, bitcoinMergedMiningMerkleProof, bitcoinMergedMiningCoinbaseTransaction,
                forkDetectionData, includeForkDetectionData, ummRoot, true, sealed);
    }

    private BlockHeader createBlockHeader(byte[] bitcoinMergedMiningHeader, byte[] bitcoinMergedMiningMerkleProof,
                                          byte[] bitcoinMergedMiningCoinbaseTransaction, byte[] forkDetectionData,
                                          boolean includeForkDetectionData, byte[] ummRoot, boolean useRskip92Encoding,
                                          boolean sealed) {
        BlockDifficulty difficulty = new BlockDifficulty(BigInteger.ONE);
        long number = 1;
        BigInteger gasLimit = BigInteger.valueOf(6800000);
        long timestamp = 7731067; // Friday, 10 May 2019 6:04:05

        return new BlockHeader(
                PegTestUtils.createHash3().getBytes(),
                HashUtil.keccak256(RLP.encodeList()),
                TestUtils.generateAddress("address"),
                HashUtil.EMPTY_TRIE_HASH,
                "tx_trie_root".getBytes(),
                HashUtil.EMPTY_TRIE_HASH,
                new Bloom().getData(),
                difficulty,
                number,
                gasLimit.toByteArray(),
                3000000,
                timestamp,
                new byte[0],
                Coin.ZERO,
                bitcoinMergedMiningHeader,
                bitcoinMergedMiningMerkleProof,
                bitcoinMergedMiningCoinbaseTransaction,
                forkDetectionData,
                Coin.valueOf(10L),
                0,
                sealed,
                useRskip92Encoding,
                includeForkDetectionData,
                ummRoot);
    }

    private byte[] concatenate(byte[] left, byte[] right) {
        byte[] leftRight = Arrays.copyOf(left, left.length + right.length);
        arraycopy(right, 0, leftRight, left.length, right.length);
        return leftRight;
    }
}
