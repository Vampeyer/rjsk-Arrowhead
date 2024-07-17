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

package co.rsk.scoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class PeerScoringReporterUtilTest {

    @Test
    void buildsReputationSummary() {
        List<PeerScoringInformation> peerScoringInformationList = new ArrayList<>(goodReputationPeers());
        peerScoringInformationList.addAll(badReputationPeers());
        peerScoringInformationList.addAll(goodReputationPeers());

        PeerScoringReputationSummary peerScoringReputationSummary =
                PeerScoringReporterUtil.buildReputationSummary(peerScoringInformationList);

        Assertions.assertEquals(new PeerScoringReputationSummary(8, 32, 0,
                0, 40, 24,
                72, 8, 0,
                0, 0, 0,
                32, 0, 0,6,2), peerScoringReputationSummary);
    }

    @Test
    void emptyDetailedStatusShouldMatchToEmptySummary() throws JsonProcessingException {
        List<PeerScoringInformation> peerScoringInformationList = new ArrayList<>();

        String detailedStatusResult = PeerScoringReporterUtil.detailedReputationString(peerScoringInformationList);
        String summaryResultString =  PeerScoringReporterUtil.reputationSummaryString(peerScoringInformationList);

        Assertions.assertEquals("{\"count\":0,\"successfulHandshakes\":0,\"failedHandshakes\":0," +
                "\"invalidHeader\":0,\"validBlocks\":0,\"invalidBlocks\":0,\"validTransactions\":0," +
                "\"invalidTransactions\":0,\"invalidNetworks\":0,\"invalidMessages\":0," +
                "\"repeatedMessages\":0,\"timeoutMessages\":0,\"unexpectedMessages\":0," +
                "\"peersTotalScore\":0,\"punishments\":0,\"goodReputationCount\":0," +
                "\"badReputationCount\":0}", summaryResultString);
        Assertions.assertEquals("[]", detailedStatusResult);
    }

    private List<PeerScoringInformation> badReputationPeers() {
        return Arrays.asList(buildPeerScoringInformation("4", false)
                , buildPeerScoringInformation("5", false));
    }

    private List<PeerScoringInformation> goodReputationPeers() {
        return Arrays.asList(buildPeerScoringInformation("1", true),
                buildPeerScoringInformation("2", true),
                buildPeerScoringInformation("3", true)
        );
    }

    private PeerScoringInformation buildPeerScoringInformation(String id, boolean goodReputation) {
        return new PeerScoringInformation(4, 0, 0,
                5, 3, 9, 1,
                0, 0, 0, 0,
                4, 0, 0, goodReputation,  0, id, "node");
    }
}
