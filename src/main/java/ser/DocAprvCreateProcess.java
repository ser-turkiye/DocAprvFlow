package ser;

import com.ser.blueline.*;
import com.ser.blueline.bpm.IProcessInstance;
import com.ser.blueline.bpm.IWorkbasket;
import de.ser.doxis4.agentserver.UnifiedAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
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

            String sndr = "";
            if(sndr.isEmpty() && Utils.hasDescriptor(document, "Sender")){
                sndr = document.getDescriptorValue("Sender", String.class);
                sndr = (sndr == null ? "" : sndr);
            }
            if(sndr.isEmpty() && cusr != null){
                sndr = cusr.getID();
                sndr = (sndr == null ? "" : sndr);
            }
            if(sndr == null){throw new Exception("Sender-ID not found.");}
            document.setDescriptorValue("Sender", sndr);

            IGroup egrp = XTRObjects.findGroup(gedt);
            if(egrp == null){
                egrp = XTRObjects.createGroup(gedt);
                egrp.commit();
            }
            if(egrp == null){throw new Exception("Not found/create group '" + gedt + "'");}

            List<String> tors = allMembers(egrp, cusr, null);
            IProcessInstance proc = helper.buildNewProcessInstanceForID(Conf.ProcessInstances.DocumentApproval);

            proc.setMainInformationObjectID(document.getID());
            Utils.copyDescriptors(document, proc);
            proc.setDescriptorValue("Sender", sndr);
            proc.setDescriptorValues("To-Receiver", tors);
            proc.setDescriptorValue("ObjectName", document.getID());
            proc.commit();

            document.setDescriptorValue("ObjectState", "In-Progress");
            document.commit();
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
    public static List<String> allMembers(IGroup xgrp, IUser cusr, List<String> parm) throws Exception {

        List<String> rtrn = parm != null ? parm : new ArrayList<String>();

        /*IWorkbasket gwbk = XTRObjects.getFirstWorkbasket(xgrp);
        if(parm == null && gwbk == null){
            gwbk = XTRObjects.createWorkbasket(xgrp);
            gwbk.commit();
        }
        if(parm == null && gwbk == null){throw new Exception("Not found/create workbasket '" + xgrp.getName() + "'");}
        if(gwbk != null && !rtrn.contains(xgrp.getID())) {
            rtrn.add(xgrp.getID());
        }*/

        IUser[] embs = xgrp.getUserMembers();
        for(IUser embr : embs){
            if(embr.getID().equals(cusr.getID())){continue;}
            IWorkbasket ewbk = XTRObjects.getFirstWorkbasket(embr);
            if(ewbk == null){continue;}

            if(rtrn.contains(embr.getID())) {continue;}
            rtrn.add(embr.getID());
        }

        IUnit[] umbs = xgrp.getUnitMembers();
        for(IUnit umbr : umbs){
            if(rtrn.contains(umbr.getID())) {continue;}
            rtrn = allMembers((IGroup) umbr, cusr, rtrn);
        }

        IGroup[] sgrs = xgrp.getGroupMembers();
        for(IGroup sgrp : sgrs){
            if(rtrn.contains(sgrp.getID())) {continue;}
            rtrn = allMembers(sgrp, cusr, rtrn);
        }

        return rtrn;
    }
}