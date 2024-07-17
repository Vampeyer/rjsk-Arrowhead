/*
 * This file is part of RskJ
 * Copyright (C) 2018 RSK Labs Ltd.
 * (derived from ethereumJ library, Copyright (c) 2016 <ether.camp>)
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
package org.ethereum.datasource;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import java.util.NoSuchElementException;

public class RocksDbKeyIterator implements DataSourceKeyIterator {
    private final RocksIterator iterator;

    public RocksDbKeyIterator(RocksDB db) {
        this.iterator = db.newIterator();
        this.iterator.seekToFirst();
    }

    @Override
    public void close() throws Exception {
        this.iterator.close();
    }

    @Override
    public boolean hasNext() {
        return this.iterator.isValid();
    }

    @Override
    public byte[] next() throws NoSuchElementException {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }

        byte[] key = this.iterator.key();

        this.iterator.next();

        return key;
    }
}
