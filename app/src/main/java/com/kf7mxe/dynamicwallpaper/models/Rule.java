package com.kf7mxe.dynamicwallpaper.models;

import java.io.Serializable;

public class Rule implements Serializable {
    private Trigger trigger;
    private Action action;

    public Rule(){

    }

    public Rule(Trigger trigger,Action action){
        this.trigger= trigger;
        this.action = action;
    }

    public Rule(String stringToCreate){
        if(stringToCreate == null || stringToCreate.length()==0){return;}
        String[] rulesSplit = stringToCreate.split("~triggersAndActions~");
        this.trigger = setTriggerFromString(rulesSplit[0]);
        this.action = new Action(rulesSplit[1]);
    }

    public Trigger setTriggerFromString(String triggerString){
        String[] stringTriggerTypeSplit = triggerString.split("~triggerTypeDeliminator~");
        String triggerType = stringTriggerTypeSplit[0];
        switch (triggerType){
            case "triggerByDateTime":
                return new TriggerByDateTime(stringTriggerTypeSplit[1]);
            default:
                return new Trigger();
        }
    }

    public String myToString() {
        return this.trigger.myToString() + "~triggersAndActions~" + this.action.myToString();
    }
}