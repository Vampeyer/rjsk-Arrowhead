/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
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

package co.rsk.rpc.modules.eth;

import static org.ethereum.rpc.exception.RskJsonRpcRequestException.invalidParamError;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import co.rsk.core.RskAddress;
import org.bouncycastle.util.BigIntegers;
import org.ethereum.core.*;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.crypto.signature.ECDSASignature;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.rsk.core.Wallet;
import co.rsk.util.HexUtils;

public class EthModuleWalletEnabled implements EthModuleWallet {

    private static final Logger LOGGER = LoggerFactory.getLogger("web3");
    private final Wallet wallet;
    private final TransactionPool transactionPool;
    private final SignatureCache signatureCache;

    public EthModuleWalletEnabled(Wallet wallet, TransactionPool transactionPool, SignatureCache signatureCache) {
        this.wallet = wallet;
        this.transactionPool = transactionPool;
        this.signatureCache = signatureCache;
    }

    @Override
    public String sign(String addr, String data) {
        String s = null;
        try {
            Account account = this.wallet.getAccount(new RskAddress(addr));
            if (account == null) {
                throw invalidParamError("Account not found");
            }

            s = this.sign(data, account.getEcKey());

            return s;
        } finally {
            LOGGER.debug("eth_sign({}, {}): {}", addr, data, s);
        }
    }

    @Override
    public String[] accounts() {
        String[] s = null;
        try {
            return s = wallet.getAccountAddressesAsHex();
        } finally {
            LOGGER.debug("eth_accounts(): {}", Arrays.toString(s));
        }
    }

    private String sign(String data, ECKey ecKey) {
        byte[] dataHash = HexUtils.stringHexToByteArray(data);
        // 0x19 = 25, length should be an ascii decimals, message - original
        String prefix = (char) 25 + "Ethereum Signed Message:\n" + dataHash.length;

        byte[] messageHash = HashUtil.keccak256(ByteUtil.merge(
                prefix.getBytes(StandardCharsets.UTF_8),
                dataHash
        ));
        ECDSASignature signature = ECDSASignature.fromSignature(ecKey.sign(messageHash));

        return HexUtils.toJsonHex(ByteUtil.merge(
                BigIntegers.asUnsignedByteArray(32, signature.getR()),
                BigIntegers.asUnsignedByteArray(32, signature.getS()),
                new byte[]{signature.getV()}
        ));
    }
    @Override
    public List<Transaction> ethPendingTransactions() {
        List<Transaction> pendingTxs = transactionPool.getPendingTransactions();
        List<String> managedAccounts = Arrays.asList(accounts());
        return pendingTxs.stream()
                .filter(tx -> managedAccounts.contains(tx.getSender(signatureCache).toJsonString()))
                .collect(Collectors.toList());
    }
}