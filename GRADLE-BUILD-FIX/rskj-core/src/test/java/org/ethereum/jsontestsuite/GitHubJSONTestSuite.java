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

package org.ethereum.jsontestsuite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.jsontestsuite.runners.StateTestRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Test file specific for tests maintained in the GitHub repository
 * by the Ethereum DEV team. <br/>
 *
 * @see <a href="https://github.com/ethereum/tests/">https://github.com/ethereum/tests/</a>
 */
@Disabled
public class GitHubJSONTestSuite {

    private static Logger logger = LoggerFactory.getLogger("TCK-Test");

    public static void runGitHubJsonVMTest(String json, String testName) throws IOException {
        Assumptions.assumeFalse(json.equals(""), "Online test is not available");

        ObjectMapper parser = new ObjectMapper();
        JsonNode testSuiteObj = parser.readTree(json);

        TestSuite testSuite = new TestSuite(testSuiteObj);
        Iterator<TestingCase> testIterator = testSuite.iterator();

        for (TestingCase testCase : testSuite.getAllTests()) {

            String prefix = "    ";
            if (testName.equals(testCase.getName())) prefix = " => ";

            logger.info(prefix + testCase.getName());
        }

        while (testIterator.hasNext()) {

            TestingCase testCase = testIterator.next();
            if (testName.equals((testCase.getName()))) {
                TestRunner runner = new TestRunner();
                List<String> result = runner.runTestCase(testCase);
                Assertions.assertTrue(result.isEmpty());
                return;
            }
        }
    }

    protected static void runGitHubJsonVMTest(String json) throws IOException {
        Set<String> excluded = new HashSet<>();


        runGitHubJsonVMTest(json, excluded,null);
    }
    public static void runGitHubJsonVMTest(String json, Set<String> excluded) throws IOException {
        runGitHubJsonVMTest(json, excluded, null);
    }

        public static void runGitHubJsonVMTest(String json, Set<String> excluded, Set<String> included) throws IOException {
        Assumptions.assumeFalse(json.equals(""), "Online test is not available");

        ObjectMapper parser = new ObjectMapper();
        JsonNode testSuiteObj = parser.readTree(json);

        TestSuite testSuite = new TestSuite(testSuiteObj);
        Iterator<TestingCase> testIterator = testSuite.iterator();

        if (logger.isDebugEnabled()) {
        for (TestingCase testCase : testSuite.getAllTests()) {
            String prefix = "    ";
            if (excluded.contains(testCase.getName())) prefix = "[-] ";
            if (included!=null)
                if (included.contains(testCase.getName())) prefix = "[+] ";

            logger.debug(prefix + testCase.getName());
        }
        }


        while (testIterator.hasNext()) {

            TestingCase testCase = testIterator.next();
            if (excluded.contains(testCase.getName()))
                continue;
            if ((included!=null) && (!included.contains(testCase.getName())))
                continue;

            TestRunner runner = new TestRunner();
            List<String> result = runner.runTestCase(testCase);
            Assertions.assertTrue(result.isEmpty());
        }
    }

    public static void runGitHubJsonSingleBlockTest(String json, String testName) throws IOException {
        BlockTestSuite testSuite = new BlockTestSuite(json);
        Set<String> testCollection = testSuite.getTestCases().keySet();

        for (String testCase : testCollection) {
            if (testCase.equals(testName))
                logger.info(" => " + testCase);
            else
                logger.info("    " + testCase);
        }

        runSingleBlockTest(testSuite, testName);
    }

    public static void runGitHubJsonBlockTest(String json, Set<String> excluded) throws IOException {
        Assumptions.assumeFalse(json.equals(""), "Online test is not available");

        BlockTestSuite testSuite = new BlockTestSuite(json);
        Set<String> testCases = testSuite.getTestCases().keySet();
        Map<String, Boolean> summary = new HashMap<>();

        for (String testCase : testCases)
            if ( excluded.contains(testCase))
                logger.info(" [X] " + testCase);
            else
                logger.info("     " + testCase);


        for (String testName : testCases) {

            if ( excluded.contains(testName)) {
                logger.info(" Not running: " + testName);
                continue;
            }

            List<String> result = runSingleBlockTest(testSuite, testName);

            if (!result.isEmpty())
                summary.put(testName, false);
            else
                summary.put(testName, true);
        }


        logger.info("");
        logger.info("");
        logger.info("Summary: ");
        logger.info("=========");

        int fails = 0; int pass = 0;
        for (String key : summary.keySet()){

            if (summary.get(key)) ++pass; else ++fails;
            String sumTest = String.format("%-60s:^%s", key, (summary.get(key) ? "OK" : "FAIL")).
                    replace(' ', '.').
                    replace("^", " ");
            logger.info(sumTest);
        }

        logger.info(" - Total: Pass: {}, Failed: {} - ", pass, fails);

        Assertions.assertEquals(0, fails);

    }

    protected static void runGitHubJsonBlockTest(String json) throws IOException {
        Set<String> excluded = new HashSet<>();
        runGitHubJsonBlockTest(json, excluded);
    }

    private static List<String> runSingleBlockTest(BlockTestSuite testSuite, String testName){

        BlockTestingCase blockTestCase =  testSuite.getTestCases().get(testName);
        TestRunner runner = new TestRunner();

        logger.info("\n\n ***************** Running test: {} ***************************** \n\n", testName);
        List<String> result = runner.runTestCase(blockTestCase);

        logger.info("--------- POST Validation---------");
        if (!result.isEmpty())
            for (String single : result)
                logger.info(single);


        return result;
    }


    public static void runStateTest(String jsonSuite) throws IOException {
        runStateTest(jsonSuite, new HashSet<String>());
    }


    public static void runStateTest(String jsonSuite, String testName) throws IOException {

        StateTestSuite stateTestSuite = new StateTestSuite(jsonSuite);
        Map<String, StateTestingCase> testCases = stateTestSuite.getTestCases();

        for (String testCase : testCases.keySet()) {
            if (testCase.equals(testName))
                logger.info("  => " + testCase);
            else
                logger.info("     " + testCase);
        }

        StateTestingCase testCase = testCases.get(testName);
        if (testCase != null){
            String output = String.format("*  running: %s  *", testName);
            String line = output.replaceAll(".", "*");

            logger.info(line);
            logger.info(output);
            logger.info(line);
            List<String> fails = StateTestRunner.run(testCases.get(testName));

            Assertions.assertEquals(0, fails.size());

        } else {
            logger.error("Sorry test case doesn't exist: {}", testName);
        }
    }

    public static void runStateTest(String jsonSuite, Set<String> excluded) throws IOException {

        StateTestSuite stateTestSuite = new StateTestSuite(jsonSuite);
        Map<String, StateTestingCase> testCases = stateTestSuite.getTestCases();
        Map<String, Boolean> summary = new HashMap<>();


        for (String testCase : testCases.keySet()) {
            if ( excluded.contains(testCase))
                logger.info(" [X] " + testCase);
            else
                logger.info("     " + testCase);
        }

        Set<String> testNames = stateTestSuite.getTestCases().keySet();

        HashMap<String,List<String>> results = new HashMap<>();

        int ignores = 0;
        for (String testName : testNames){

            if (excluded.contains(testName)) {
                ignores++;
                continue;
            }

            String output = String.format("*  running: %s  *", testName);
            String line = output.replaceAll(".", "*");

            logger.info(line);
            logger.info(output);
            logger.info(line);

            List<String> result = StateTestRunner.run(testCases.get(testName));
            results.put(testName,result);


            if (!result.isEmpty()) {
                summary.put(testName, false);
            }
            else
                summary.put(testName, true);
        }

        logger.info("Summary: ");
        logger.info("=========");

        int fails = 0; int pass = 0;
        for (String key : summary.keySet()){

            if (summary.get(key)) ++pass; else ++fails;
            String sumTest = String.format("%-60s:^%s", key, (summary.get(key) ? "OK" : "FAIL")).
                    replace(' ', '.').
                    replace("^", " ");
            logger.info(sumTest);

        }

        logger.info(" - Total: Pass: {}, Failed: {} - Ignore: {} -", pass, fails,ignores);

        for (String testname : results.keySet()) {
            Assertions.assertTrue(results.get(testname).isEmpty(), testname + " error array not empty: "+results.get(testname));
        }
    }

}
