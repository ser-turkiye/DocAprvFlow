package junit

import de.ser.doxis4.agentserver.AgentExecutionResult
import org.junit.*
import ser.DocAprvCreateProcess

class TEST_DocAprvCreateProcess {

    Binding binding

    @BeforeClass
    static void initSessionPool() {
        AgentTester.initSessionPool()
    }

    @Before
    void retrieveBinding() {
        binding = AgentTester.retrieveBinding()
    }

    @Test
    void testForAgentResult() {
        def agent = new DocAprvCreateProcess()

        binding["AGENT_EVENT_OBJECT_CLIENT_ID"] = "SD08GIB_DOCS24eb53efb7-57e4-4b4a-a44e-4fca282434f2182024-04-30T08:23:08.351Z011"

        def result = (AgentExecutionResult)agent.execute(binding.variables)
        assert result.resultCode == 0
    }

    @Test
    void testForJavaAgentMethod() {
        //def agent = new JavaAgent()
        //agent.initializeGroovyBlueline(binding.variables)
        //assert agent.getServerVersion().contains("Linux")
    }

    @After
    void releaseBinding() {
        AgentTester.releaseBinding(binding)
    }

    @AfterClass
    static void closeSessionPool() {
        AgentTester.closeSessionPool()
    }
}
