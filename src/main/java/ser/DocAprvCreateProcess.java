package ser;

import com.ser.blueline.IDocument;
import com.ser.blueline.IGroup;
import com.ser.blueline.IInformationObject;
import com.ser.blueline.IUser;
import com.ser.blueline.bpm.IProcessInstance;
import com.ser.blueline.bpm.IWorkbasket;
import de.ser.doxis4.agentserver.UnifiedAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;


public class DocAprvCreateProcess extends UnifiedAgent {
    Logger log = LogManager.getLogger();
    IInformationObject qaInfObj;
    ProcessHelper helper;
    IDocument document;
    String compCode;
    String docId;
    @Override
    protected Object execute() {
        if (getEventDocument() == null)
            return resultError("Null Document object");

        Utils.session = getSes();
        Utils.bpm = getBpm();
        Utils.server = Utils.session.getDocumentServer();
        Utils.loadDirectory(Conf.Paths.MainPath);
        
        document = getEventDocument();

        try {

            helper = new ProcessHelper(Utils.session);
            XTRObjects.setSession(Utils.session);
            XTRObjects.setBpm(Utils.bpm);

            IUser cusr = XTRObjects.getDocCreatorUser(document);
            String gedt = "_" + (document.getArchiveClass().getName() != null ?
                    document.getArchiveClass().getName() : "_All_Document")+ "_Edit";

            IGroup egrp = XTRObjects.findGroup(gedt);
            if(egrp == null){
                egrp = XTRObjects.createGroup(gedt);
                egrp.commit();
            }
            if(egrp == null){throw new Exception("Not found/create group '" + gedt + "'");}
            IWorkbasket gwbk = XTRObjects.getFirstWorkbasket(egrp);
            if(gwbk == null){
                gwbk = XTRObjects.createWorkbasket(egrp);
                gwbk.commit();
            }
            if(gwbk == null){throw new Exception("Not found/create workbasket '" + gedt + "'");}
            IProcessInstance proc = helper.buildNewProcessInstanceForID(Conf.ProcessInstances.DocumentApproval);

            proc.setMainInformationObjectID(document.getID());

            XTRObjects.copyDescriptors(document, proc);
            proc.setDescriptorValue("To-Receiver", egrp.getID());
            proc.setDescriptorValue("ObjectType", document.getArchiveClass().getName());
            proc.setDescriptorValue("ObjectName", document.getID());
            proc.commit();
            //proc.getID();
            //XTRObjects.createLink(document, proc);

            log.info("Tested.");

        } catch (Exception e) {
            //throw new RuntimeException(e);
            log.error("Exception       : " + e.getMessage());
            log.error("    Class       : " + e.getClass());
            log.error("    Stack-Trace : " + e.getStackTrace() );
            return resultError("Exception : " + e.getMessage());
        }

        log.info("Finished");
        return resultSuccess("Ended successfully");
    }
}