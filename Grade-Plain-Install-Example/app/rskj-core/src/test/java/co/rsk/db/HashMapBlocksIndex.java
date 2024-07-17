package co.rsk.db;

import co.rsk.crypto.Keccak256;
import org.ethereum.db.IndexedBlockStore;

import java.util.*;

public class HashMapBlocksIndex implements BlocksIndex {

    private final Map<Long, List<IndexedBlockStore.BlockInfo>> index;

    public HashMapBlocksIndex() {
        index = new HashMap<>();
    }

    @Override
    public boolean isEmpty() {
        return index.isEmpty();
    }

    @Override
    public long getMaxNumber() {
        if (index.isEmpty()) {
            return -1;
        }

        return index.keySet().stream().max(Long::compare).get();
    }

    @Override
    public long getMinNumber() {
        return index.keySet().stream().min(Long::compare)
                .orElseThrow(() -> new IllegalStateException("Index is empty"));
    }

    @Override
    public boolean contains(long blockNumber) {
        return index.containsKey(blockNumber);
    }

    @Override
    public List<IndexedBlockStore.BlockInfo> getBlocksByNumber(long blockNumber) {
        return index.getOrDefault(blockNumber, new LinkedList<>());
    }

    @Override
    public void putBlocks(long blockNumber, List<IndexedBlockStore.BlockInfo> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            throw new IllegalArgumentException("Blocks to store cannot be null nor empty");
        }
        index.put(blockNumber, blocks);
    }

    @Override
    public List<IndexedBlockStore.BlockInfo> removeLast() {
        List<IndexedBlockStore.BlockInfo> result = index.remove(getMaxNumber());
        if (result == null) {
            result = new ArrayList<>();
        }
        return result;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {

    }

    @Override
    public void removeBlock(long blockNumber, Keccak256 blockHash) {
        if (!index.containsKey(blockNumber)) {
            return;
        }

        List<IndexedBlockStore.BlockInfo> blockInfoList = index.get(blockNumber);

        if (blockInfoList == null) {
            return;
        }

        List<IndexedBlockStore.BlockInfo> toRemove = new ArrayList<>();

        for (IndexedBlockStore.BlockInfo bInfo : blockInfoList) {
            if (bInfo.getHash().equals(blockHash)) {
                toRemove.add(bInfo);
            }
        }
        //Here no any other update is needed as we are modifying the list stored in the map.
        blockInfoList.removeAll(toRemove);
        if (blockInfoList.isEmpty()) {
            index.remove(blockNumber);
        }
    }

}
