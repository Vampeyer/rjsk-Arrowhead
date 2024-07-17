package co.rsk.rpc.modules.personal;

import co.rsk.config.TestSystemProperties;
import co.rsk.core.RskAddress;
import co.rsk.core.Wallet;
import org.bouncycastle.util.encoders.Hex;
import org.ethereum.crypto.ECKey;
import org.ethereum.rpc.exception.RskJsonRpcRequestException;
import org.ethereum.rpc.parameters.HexKeyParam;
import org.ethereum.util.ByteUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by Nazaret García on 15/01/2021
 */

class PersonalModuleWalletEnabledTest {
    private final TestSystemProperties config = new TestSystemProperties();

    @Test
    void importRawKey_KeyIsNull_ThrowsNullPointerException() {
        PersonalModuleWalletEnabled personalModuleWalletEnabled = createPersonalModuleWalletEnabled(null);
        Assertions.assertThrows(RskJsonRpcRequestException.class, () -> personalModuleWalletEnabled.importRawKey(null, "passphrase1"));
    }

    @Test
    void importRawKey_KeyContains0xPrefix_OK() {
        ECKey eckey = new ECKey();
        String rawKey = ByteUtil.toHexString(eckey.getPrivKeyBytes());
        String passphrase = "passphrase1";
        byte[] hexDecodedKey = Hex.decode(rawKey);

        RskAddress addressMock = mock(RskAddress.class);
        doReturn("{}").when(addressMock).toJsonString();

        Wallet walletMock = mock(Wallet.class);
        doReturn(addressMock).when(walletMock).addAccountWithPrivateKey(hexDecodedKey, passphrase);
        doReturn(true).when(walletMock).unlockAccount(eq(addressMock), eq(passphrase), any(Long.class));

        PersonalModuleWalletEnabled personalModuleWalletEnabled = createPersonalModuleWalletEnabled(walletMock);
        HexKeyParam hexKeyParam = new HexKeyParam(String.format("0x%s", rawKey));
        String result = personalModuleWalletEnabled.importRawKey(hexKeyParam, passphrase);

        verify(walletMock, times(1)).addAccountWithPrivateKey(hexDecodedKey, passphrase);
        verify(walletMock, times(1)).unlockAccount(addressMock, passphrase, 1800000L);
        verify(addressMock, times(1)).toJsonString();

        assertEquals("{}", result);
    }

    @Test
    void importRawKey_KeyDoesNotContains0xPrefix_OK() {
        ECKey eckey = new ECKey();
        String rawKey = ByteUtil.toHexString(eckey.getPrivKeyBytes());
        String passphrase = "passphrase1";
        byte[] hexDecodedKey = Hex.decode(rawKey);

        RskAddress addressMock = mock(RskAddress.class);
        doReturn("{}").when(addressMock).toJsonString();

        Wallet walletMock = mock(Wallet.class);
        doReturn(addressMock).when(walletMock).addAccountWithPrivateKey(hexDecodedKey, passphrase);
        doReturn(true).when(walletMock).unlockAccount(eq(addressMock), eq(passphrase), any(Long.class));

        PersonalModuleWalletEnabled personalModuleWalletEnabled = createPersonalModuleWalletEnabled(walletMock);
        HexKeyParam hexKeyParam = new HexKeyParam(rawKey);
        String result = personalModuleWalletEnabled.importRawKey(hexKeyParam, passphrase);

        verify(walletMock, times(1)).addAccountWithPrivateKey(hexDecodedKey, passphrase);
        verify(walletMock, times(1)).unlockAccount(addressMock, passphrase, 1800000L);
        verify(addressMock, times(1)).toJsonString();

        assertEquals("{}", result);
    }

    private PersonalModuleWalletEnabled createPersonalModuleWalletEnabled(Wallet wallet) {
        return new PersonalModuleWalletEnabled(config, null, wallet, null);
    }

}
