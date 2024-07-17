/*
 * This file is part of RskJ
 * Copyright (C) 2022 RSK Labs Ltd.
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

package co.rsk.net.handler.quota;

import org.ethereum.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Calculates the virtualGas consumed by a transaction taking into account several factors of the transaction and the moment it is being executed
 */
class TxVirtualGasCalculator {

    private static final int FUTURE_NONCE_FACTOR = 2;
    private static final int NONCE_FACTOR = 5;
    private static final int SIZE_FACTOR = 5;
    private static final int LOW_GAS_PRICE_FACTOR = 4;
    private static final double REPLACEMENT_FACTOR = 1.9;
    private static final int GAS_LIMIT_FACTOR = 5;
    private static final double MAX_FACTOR = FUTURE_NONCE_FACTOR * NONCE_FACTOR * SIZE_FACTOR * LOW_GAS_PRICE_FACTOR * REPLACEMENT_FACTOR * GAS_LIMIT_FACTOR;

    private static final double GAS_LIMIT_WEIGHT = 4;
    private static final double NONCE_WEIGHT = 4;
    private static final double LOW_GAS_PRICE_WEIGH = 3;
    private static final double SIZE_FACTOR_DIVISOR = 25000d;

    private static final Logger logger = LoggerFactory.getLogger(TxVirtualGasCalculator.class);

    private final long accountNonce;

    private final long blockGasLimit;

    private final long blockMinGasPrice;

    private final long avgGasPrice;

    private final boolean skipGasPriceFactor;

    static TxVirtualGasCalculator createWithAllFactors(long accountNonce, long blockGasLimit, long blockMinGasPrice, long avgGasPrice) {
        return new TxVirtualGasCalculator(accountNonce, blockGasLimit, blockMinGasPrice, avgGasPrice, false);
    }

    static TxVirtualGasCalculator createSkippingGasPriceFactor(long accountNonce, long blockGasLimit, long blockMinGasPrice) {
        return new TxVirtualGasCalculator(accountNonce, blockGasLimit, blockMinGasPrice, -1, true);
    }

    private TxVirtualGasCalculator(long accountNonce, long blockGasLimit, long blockMinGasPrice, long avgGasPrice, boolean skipGasPriceFactor) {
        this.accountNonce = accountNonce;
        this.blockGasLimit = blockGasLimit;
        this.blockMinGasPrice = blockMinGasPrice;
        this.avgGasPrice = avgGasPrice;
        this.skipGasPriceFactor = skipGasPriceFactor;
    }

    /**
     * Calculates the virtualGas consumed by a transaction taking into account several factors of the transaction and the moment it is being executed
     *
     * @param newTx      The tx being executed
     * @param replacedTx The tx replaced by <code>newTx</code> if any
     * @return The virtualGas consumed by the provided <code>newTx</code>
     */
    double calculate(Transaction newTx, @Nullable Transaction replacedTx) {
        long txGasLimit = newTx.getGasLimitAsInteger().longValue();

        long newTxNonce = newTx.getNonceAsInteger().longValue();
        long futureNonceFactor = newTxNonce == accountNonce ? 1 : 2;

        double lowGasPriceFactor = skipGasPriceFactor ? 1 : calculateLowGasPriceFactor(newTx);

        double nonceFactor = 1 + NONCE_WEIGHT / (accountNonce + 1);

        double sizeFactor = 1 + newTx.getSize() / SIZE_FACTOR_DIVISOR;

        double replacementFactor = calculateReplacementFactor(newTx, replacedTx);

        double gasLimitFactor = calculateGasLimitFactor(txGasLimit);

        double compositeFactor = futureNonceFactor * lowGasPriceFactor * nonceFactor * sizeFactor * replacementFactor * gasLimitFactor;

        double result = capeResult(txGasLimit, compositeFactor);

        logger.trace("virtualGasConsumed calculation for tx [{}] and avgGasPrice [{}]: result = [{}] (txGasLimit {}, compositeFactor {}, futureNonceFactor {}, lowGasPriceFactor {}, skipGasPriceFactor {}, nonceFactor {}, sizeFactor {}, replacementFactor {}, gasLimitFactor {})", newTx.getHash(), avgGasPrice, result, txGasLimit, compositeFactor, futureNonceFactor, lowGasPriceFactor, skipGasPriceFactor, nonceFactor, sizeFactor, replacementFactor, gasLimitFactor);

        return result;
    }

    private double calculateLowGasPriceFactor(Transaction newTx) {
        long txGasPrice = newTx.getGasPrice().asBigInteger().longValue();

        if (txGasPrice < this.avgGasPrice) {
            double factor = (this.avgGasPrice - txGasPrice) / (this.avgGasPrice - (double) this.blockMinGasPrice);
            return 1 + LOW_GAS_PRICE_WEIGH * factor;
        } else {
            return 1;
        }
    }

    private double calculateReplacementFactor(Transaction newTx, @Nullable Transaction replacedTx) {
        double newTxGasPrice = newTx.getGasPrice().asBigInteger().doubleValue();
        double replacementRatio = Optional.ofNullable(replacedTx)
                .map(Transaction::getGasPrice)
                .map(rtxGasPrice -> rtxGasPrice.asBigInteger().doubleValue())
                .map(rtxGasPrice -> newTxGasPrice / rtxGasPrice)
                .orElse(0.0);
        return replacementRatio > 0 ? (1 + 1 / replacementRatio) : 1;
    }

    private double calculateGasLimitFactor(long txGasLimit) {
        return 1 + GAS_LIMIT_WEIGHT * txGasLimit / this.blockGasLimit;
    }

    @SuppressWarnings("squid:S1244")
    // extra security measure
    private double capeResult(double gasLimit, double factor) {
        double result = gasLimit * factor;

        if (Double.POSITIVE_INFINITY == result) {
            return gasLimit * MAX_FACTOR;
        }

        return result;
    }

}
