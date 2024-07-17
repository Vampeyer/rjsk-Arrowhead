/*
 * This file is part of RskJ
 * Copyright (C) 2023 RSK Labs Ltd.
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
package co.rsk.pcc.environment;

import co.rsk.pcc.ExecutionEnvironment;
import co.rsk.pcc.NativeMethod;
import co.rsk.pcc.exception.NativeContractIllegalArgumentException;
import org.ethereum.core.CallTransaction;
import org.ethereum.vm.program.invoke.ProgramInvoke;

import javax.annotation.Nullable;

public class GetCallStackDepth extends NativeMethod {
    private final CallTransaction.Function function = CallTransaction.Function.fromSignature(
            "getCallStackDepth",
            new String[]{},
            new String[]{"uint32"}
    );

    /**
     * This variable could receive null as a value, in those cases the execute function
     * will return teh minimum value of 1 as specified in RSKIP-203.
     */
    @Nullable
    private final ProgramInvoke programInvoke;

    public GetCallStackDepth(ExecutionEnvironment executionEnvironment, ProgramInvoke programInvoke) {
        super(executionEnvironment);
        this.programInvoke = programInvoke;
    }

    @Override
    public CallTransaction.Function getFunction() {
        return function;
    }

    @Override
    public Object execute(Object[] arguments) throws NativeContractIllegalArgumentException {
        return programInvoke == null ? 1 : programInvoke.getCallDeep() + 1;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean onlyAllowsLocalCalls() {
        return false;
    }

    @Override
    public long getGas(Object[] parsedArguments, byte[] originalData) {
        return 0;
    }
}
