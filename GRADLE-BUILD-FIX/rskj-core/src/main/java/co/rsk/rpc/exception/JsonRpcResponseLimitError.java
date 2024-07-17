/*
 * This file is part of RskJ
 * Copyright (C) 2023 RSK Labs Ltd.
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

package co.rsk.rpc.exception;

import co.rsk.jsonrpc.JsonRpcError;

public class JsonRpcResponseLimitError extends JsonRpcThrowableError {
    private static final String ERROR_MSG_WITH_LIMIT = "Response size limit of %d bytes exceeded";
    private static final long serialVersionUID = 3145337981628533511L;

    public JsonRpcResponseLimitError(int max) {
        super(String.format(ERROR_MSG_WITH_LIMIT, max));
    }

    @Override
    public JsonRpcError getErrorResponse() {
        return new JsonRpcError(JsonRpcError.RPC_LIMIT_ERROR, getMessage());
    }
}
