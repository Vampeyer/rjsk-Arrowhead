package co.rsk.net.discovery.message;

import co.rsk.net.discovery.PeerDiscoveryException;
import org.ethereum.util.RLP;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.ethereum.util.ByteUtil.intToBytes;
import static org.ethereum.util.ByteUtil.stripLeadingZeroes;

class NeighborsPeerMessageTest {

    private static final int NETWORK_ID = 1;

    private static final byte[] wireNeighborsPeerMessage = new byte[]{113, -5, -63, 15, -4, 21, -42, 93, -10, 19, -95, -37, -29, 63, -92, -125, 28, -27, -60, 99, -85, -33, 43, 86, -12, -74, 66, 100, -31, -41, 32, 107, -69, 20, -38, -122, -33, -110, -118, -126, -12, -85, 20, 91, 44, 44, -5, 79, -101, -53, 124, -57, -29, 118, -81, -48, -89, -99, -29, -93, 8, -17, 113, -87, 100, -123, -124, -77, -99, 76, -51, 54, 4, -78, -41, 116, 84, -116, 35, -35, -30, -12, 16, -87, 23, -32, -73, 124, 71, -26, -105, -28, -14, 91, 122, -114, 1, 1, -8, 76, -48, -119, 108, 111, 99, 97, 108, 104, 111, 115, 116, -126, 31, -112, -126, 31, -112, -48, -119, 108, 111, 99, 97, 108, 104, 111, 115, 116, -126, 31, -112, -126, 31, -112, -92, 48, 97, 57, 54, 55, 56, 53, 53, 45, 99, 102, 98, 97, 45, 52, 98, 49, 98, 45, 57, 56, 57, 48, 45, 102, 54, 99, 57, 51, 49, 97, 52, 97, 55, 97, 48, -124, -59, 3, -58, -55};
    private static final byte[] mdcNeighborsPeerMessage = new byte[]{113, -5, -63, 15, -4, 21, -42, 93, -10, 19, -95, -37, -29, 63, -92, -125, 28, -27, -60, 99, -85, -33, 43, 86, -12, -74, 66, 100, -31, -41, 32, 107};
    private static final byte[] signatureNeighborsPeerMessage = new byte[]{-69, 20, -38, -122, -33, -110, -118, -126, -12, -85, 20, 91, 44, 44, -5, 79, -101, -53, 124, -57, -29, 118, -81, -48, -89, -99, -29, -93, 8, -17, 113, -87, 100, -123, -124, -77, -99, 76, -51, 54, 4, -78, -41, 116, 84, -116, 35, -35, -30, -12, 16, -87, 23, -32, -73, 124, 71, -26, -105, -28, -14, 91, 122, -114, 1};

    @Test
    void parseInvalidMessageId() {
        try {
            createNeighborsPeerMessageWithCheck("http://fake-uuid.com/run");
            Assertions.fail("Invalid messageId exception should've been thrown");
        } catch (PeerDiscoveryException pde) {
            Assertions.assertEquals(NeighborsPeerMessage.class.getSimpleName() + " needs valid messageId", pde.getMessage());
        }
    }

    @Test
    void parseUUIDV1MessageId() {
        try {
            String uuidV1 = "06ce06f8-7230-11ec-90d6-0242ac120003";
            createNeighborsPeerMessageWithCheck(uuidV1);
            Assertions.fail("Invalid messageId exception should've been thrown");
        } catch (PeerDiscoveryException pde) {
            Assertions.assertEquals(NeighborsPeerMessage.class.getSimpleName() + " needs valid messageId", pde.getMessage());
        }
    }

    @Test
    void parseValidMessageId() {
        try {
            NeighborsPeerMessage message = createNeighborsPeerMessageWithCheck(UUID.randomUUID().toString());
            Assertions.assertNotNull(message);
        } catch (PeerDiscoveryException pde) {
            Assertions.fail(NeighborsPeerMessage.class.getSimpleName() + " should've worked with valid messageId");
        }
    }

    private NeighborsPeerMessage createNeighborsPeerMessageWithCheck(String check) {
        byte[] type = new byte[]{(byte) DiscoveryMessageType.NEIGHBORS.getTypeValue()};

        byte[] rlpCheck = RLP.encodeElement(check.getBytes(StandardCharsets.UTF_8));

        byte[] rlpNetworkId = RLP.encodeElement(stripLeadingZeroes(intToBytes(NETWORK_ID)));

        byte[] rlpListNodes = RLP.encodeList();
        byte[] data = RLP.encodeList(rlpListNodes, rlpCheck, rlpNetworkId);

        return NeighborsPeerMessage.buildFromReceived(wireNeighborsPeerMessage, mdcNeighborsPeerMessage, signatureNeighborsPeerMessage, type, data);
    }
}
