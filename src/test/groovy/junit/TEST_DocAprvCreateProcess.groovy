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

        binding["AGENT_EVENT_OBJECT_CLIENT_ID"] = "SD08GIB_DOCS24941aa279-d185-4e5d-bd81-bef7fa276dfb182024-05-16T06:22:37.699Z011"

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
