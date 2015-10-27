package edu.umich.si.inteco.minuku.util;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import edu.umich.si.inteco.minuku.context.EventManager;
import edu.umich.si.inteco.minuku.model.Circumstance;
import edu.umich.si.inteco.minuku.model.ProbeObject;
import edu.umich.si.inteco.minuku.model.ProbeObjectControl.ActionControl;
import edu.umich.si.inteco.minuku.model.TriggerLink;
import edu.umich.si.inteco.minuku.model.actions.Action;

/**
 * Created by Armuro on 7/6/14.
 */
public class TriggerManager {

    private static final String LOG_TAG = "EventManager";
    private static Context mContext;



    /** ProbeObject Class**/
    public static final String PROBE_OBJECT_CLASS_EVENT= "Circumstance";
    public static final String PROBE_OBJECT_CLASS_ACTION= "Action";
    public static final String PROBE_OBJECT_CLASS_ACTION_CONTROL= "ActionControl";

    /**manage a list of ProbeOject **/
    private static ArrayList<ProbeObject> mProbeObjectList;

    /*** trigger links**/
    private static ArrayList<TriggerLink> mTriggerLinks;


    public TriggerManager(Context context) {

        mContext = context;
        mProbeObjectList = new ArrayList<ProbeObject>();
        mTriggerLinks = new ArrayList<TriggerLink>();

    }


    public static  void executeTriggers(ArrayList<TriggerLink> triggerLinks){

        if (triggerLinks!=null && triggerLinks.size()>0) {

            for (int i=0; i<triggerLinks.size(); i++ ){

                executeTrigger(triggerLinks.get(i));
            }
        }
    }

    public static void executeTrigger(TriggerLink tl) {

        Log.d(LOG_TAG, "[executeTrigger] examineTransportation the triggerdlinks's trigger is " + tl.getTriggerClass() + " " +
                tl.getTrigger().getId() + " and it triggers object " + tl.getTriggeredProbeObject().getProbeObjectClass() + " " + tl.getTriggeredProbeObject().getId() + " , of which the class is "
                + tl.getTriggeredProbeObject().getProbeObjectClass());

        ProbeObject triggeredObject = tl.getTriggeredProbeObject();

        if (triggeredObject.getProbeObjectClass().equals(PROBE_OBJECT_CLASS_ACTION_CONTROL)){
            ActionControl ac = (ActionControl) triggeredObject;
            //		Log.d(LOG_TAG, "[examineEventConditions] found the triggered probe object is action control" + ac.getId() + " of which the action is " + ac.getAction().getName()  );

            //schedule the action control
            ScheduleAndSampleManager.registerActionControl(ac);

            Log.d(LOG_TAG,  "[executeTrigger] examineTransportation Triggering ActionControl:\t" + ActionManager.getActionControlTypeName(ac.getType()) + "\t" + ac.getAction().getName());
            //log triggering actioncontrol
            LogManager.log(LogManager.LOG_TYPE_SYSTEM_LOG,
                    LogManager.LOG_TAG_ACTION_TRIGGER,
                    "Triggering ActionControl:\t" + ActionManager.getActionControlTypeName(ac.getType()) + "\t" + ac.getAction().getName());
        }

        else if (triggeredObject.getClass().equals(PROBE_OBJECT_CLASS_EVENT)){


        }

        else if (triggeredObject.getClass().equals(PROBE_OBJECT_CLASS_ACTION)){


        }

    }


    public static void executeTriggers(Circumstance circumstance){

        for (int k=0; k< circumstance.getTriggerLinks().size(); k++){

            TriggerLink tl = circumstance.getTriggerLinks().get(k);

            executeTrigger(tl);
        }
    }


    public static void executeTriggers(ActionControl actionControl){

        for (int k=0; k<actionControl.getTriggerLinks().size(); k++){

            TriggerLink tl = actionControl.getTriggerLinks().get(k);

            executeTrigger(tl);
        }
    }


    /**
     *
     * The function will retrieve the list of triggerlinks and connect triggers and their triggeredAction
     * After the connections is build.
     *
     */
    public static void setUpTriggerLinks() {


        //for every trigger in mTriggerLinks, get the trigger id and class to find out its trigger
        //and connect the trigger object and the triggered object

        for (int i=0; i<TriggerManager.getTriggerLinks().size(); i++){

            TriggerLink tl =TriggerManager.getTriggerLinks().get(i);

            String trigger_class = tl.getTriggerClass();
            int trigger_id = tl.getTriggerId();
            ProbeObject triggeredObject = tl.getTriggeredProbeObject();
            int study_id = tl.getStudyId();
            float trigger_rate = tl.getTriggerRate();


            /**try to find the trigger of the triggeredObject**/

            //if the ProbeObject is triggered by an event
            if ( trigger_class.equals(ActionManager.ACTION_TRIGGER_CLASS_EVENT)   ){

                //find the event as the trigger
                for (int j = 0; j< EventManager.getEventList().size(); j++){

                    Circumstance evt = EventManager.getEventList().get(j);

                    //use the trigger id and study id to find the event ( different events in a different study may have a same event id in their own study)
                    //the eventlist in the eventMonitor stores the events in all studies. So we need to use event id and study id together to identify the correct event
                    if (trigger_id == evt.getId() && study_id==evt.getStudyId() ) {

                        /**connet the trigger and the trigger link**/
                        //1. set the triggerlink of the trigger
                        tl.setTrigger(evt);

                        //2. add the current trigger link to the probe object
                        evt.addTriggerLink(tl);

                        Log.d(LOG_TAG, "[setUpTriggerLinks  the ProbeObject " + triggeredObject.getId() + " find is trigger event" + " the triggerlink now is linked to the trigger" + tl.getTriggerClass() + " " + tl.getTrigger().getId());

                    }
                }


            }

            //if the ProbeObject is triggered by an ActionControl
            else if ( trigger_class.equals(ActionManager.ACTION_TRIGGER_CLASS_ACTION_START)){

                //use the action Id and the trigger class to find all action controls that will trigger the triggeredObject
                for (int j = 0; j<ActionManager.getActionList().size(); j++){

                    Action action = ActionManager.getActionList().get(j);
                    if (trigger_id == action.getId() && study_id==action.getStudyId() ){

                        ArrayList<ActionControl> actionControls =  ActionManager.getActionControls(action, ActionManager.ACTION_CONTROL_TYPE_START);

                        //get all startControl of the current action
                        for (int k=0; k<actionControls.size(); k++){

                            ActionControl ac = actionControls.get(k);

                            tl.setTrigger(ac);

                            ac.addTriggerLink(tl);

                            Log.d(LOG_TAG, "[setUpTriggerLinks  the ProbeObject " + triggeredObject.getId() + " find is trigger action start control" + " the triggerlink now is linked to the trigger" + tl.getTriggerClass() + " " + tl.getTrigger().getId());

                        }
                    }
                }


            }


            //if the ProbeObject is triggered by an Action
            else if ( trigger_class.equals(ActionManager.ACTION_TRIGGER_CLASS_ACTION_STOP)){

                //use the action Id and the trigger class to find all action controls that will trigger the triggeredObject
                for (int j = 0; j<ActionManager.getActionList().size(); j++){

                    Action action = ActionManager.getActionList().get(j);
                    if (trigger_id == action.getId() && study_id==action.getStudyId() ){

                        ArrayList<ActionControl> actionControls =  ActionManager.getActionControls(action, ActionManager.ACTION_CONTROL_TYPE_STOP);

                        //get all startControl of the current action
                        for (int k=0; k<actionControls.size(); k++){

                            ActionControl ac = actionControls.get(k);

                            tl.setTrigger(ac);

                            ac.addTriggerLink(tl);

                            Log.d(LOG_TAG, "[setUpTriggerLinks  the ProbeObject " + triggeredObject.getId() + " find is trigger action start control" + " the triggerlink now is linked to the trigger" + tl.getTriggerClass() + " " + tl.getTrigger().getId());

                        }
                    }
                }
            }


            else if ( trigger_class.equals(ActionManager.ACTION_TRIGGER_CLASS_ACTION_PAUSE)){

                //use the action Id and the trigger class to find all action controls that will trigger the triggeredObject
                for (int j = 0; j<ActionManager.getActionList().size(); j++){

                    Action action = ActionManager.getActionList().get(j);
                    if (trigger_id == action.getId() && study_id==action.getStudyId() ){

                        ArrayList<ActionControl> actionControls =  ActionManager.getActionControls(action, ActionManager.ACTION_CONTROL_TYPE_PAUSE);

                        //get all startControl of the current action
                        for (int k=0; k<actionControls.size(); k++){

                            ActionControl ac = actionControls.get(k);

                            tl.setTrigger(ac);

                            ac.addTriggerLink(tl);

                            Log.d(LOG_TAG, "[setUpTriggerLinks  the ProbeObject " + triggeredObject.getId() + " find is trigger action start control" + " the triggerlink now is linked to the trigger" + tl.getTriggerClass() + " " + tl.getTrigger().getId());

                        }
                    }
                }
            }

            else if ( trigger_class.equals(ActionManager.ACTION_TRIGGER_CLASS_ACTION_RESUME)){

                //use the action Id and the trigger class to find all action controls that will trigger the triggeredObject
                for (int j = 0; j<ActionManager.getActionList().size(); j++){

                    Action action = ActionManager.getActionList().get(j);
                    if (trigger_id == action.getId() && study_id==action.getStudyId() ){

                        ArrayList<ActionControl> actionControls =  ActionManager.getActionControls(action, ActionManager.ACTION_CONTROL_TYPE_RESUME);

                        //get all startControl of the current action
                        for (int k=0; k<actionControls.size(); k++){

                            ActionControl ac = actionControls.get(k);

                            tl.setTrigger(ac);

                            ac.addTriggerLink(tl);

                            Log.d(LOG_TAG, "[setUpTriggerLinks  the ProbeObject " + triggeredObject.getId() + " find is trigger action start control" + " the triggerlink now is linked to the trigger" + tl.getTriggerClass() + " " + tl.getTrigger().getId());

                        }
                    }
                }
            }

            else if ( trigger_class.equals(ActionManager.ACTION_TRIGGER_CLASS_ACTION_CANCEL)){

                //use the action Id and the trigger class to find all action controls that will trigger the triggeredObject
                for (int j = 0; j<ActionManager.getActionList().size(); j++){

                    Action action = ActionManager.getActionList().get(j);
                    if (trigger_id == action.getId() && study_id==action.getStudyId() ){

                        ArrayList<ActionControl> actionControls =  ActionManager.getActionControls(action, ActionManager.ACTION_CONTROL_TYPE_CANCEL);

                        //get all startControl of the current action
                        for (int k=0; k<actionControls.size(); k++){

                            ActionControl ac = actionControls.get(k);

                            tl.setTrigger(ac);

                            ac.addTriggerLink(tl);

                            Log.d(LOG_TAG, "[setUpTriggerLinks  the ProbeObject " + triggeredObject.getId() + " find is trigger action start control" + " the triggerlink now is linked to the trigger" + tl.getTriggerClass() + " " + tl.getTrigger().getId());

                        }
                    }
                }
            }


            else if ( trigger_class.equals(ActionManager.ACTION_TRIGGER_CLASS_ACTIONCONTROL)){


                Log.d(LOG_TAG, "[setUpTriggerLinks  the ProbeObject " + triggeredObject.getId() + " find is trigger action" +
                        tl.getTriggeredProbeObject().getId());
            }
        }

    }

    public static ArrayList<ProbeObject> getProbeObjectList (){

        return mProbeObjectList;
    }


    public static void addProbeObject(ProbeObject probe_object) {

        mProbeObjectList.add(probe_object);
    }

    public static ArrayList<TriggerLink> getTriggerLinks(){

        return mTriggerLinks;
    }

    public static void addTriggerLink(TriggerLink trigger_link) {

        mTriggerLinks.add(trigger_link);
    }



}
