/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
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

package org.ethereum.net.p2p;

import org.bouncycastle.util.encoders.Hex;

import java.util.Arrays;

/**
 * Wrapper around an Ethereum Pong message on the network
 *
 * @see org.ethereum.net.p2p.P2pMessageCodes#PONG
 */
public class PongMessage extends P2pMessage {

    /**
     * Pong message is always a the same single command payload
     */
    private static final byte[] FIXED_PAYLOAD = Hex.decode("C0");

    @Override
    public byte[] getEncoded() {
        return Arrays.copyOf(FIXED_PAYLOAD, FIXED_PAYLOAD.length);
    }

    @Override
    public Class<?> getAnswerMessage() {
        return null;
    }

    @Override
    public P2pMessageCodes getCommand() {
        return P2pMessageCodes.PONG;
    }

    @Override
    public String toString() {
        return "[" + this.getCommand().name() + "]";
    }
}