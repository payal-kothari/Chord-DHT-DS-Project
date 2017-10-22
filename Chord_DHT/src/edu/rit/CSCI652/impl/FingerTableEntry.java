package edu.rit.CSCI652.impl;


/**
 * Created by payalkothari on 10/18/17.
 */

public class FingerTableEntry {

    int start;
    int intervalBegin;
    int intervalEnd;

//    @JsonManagedReference

//    @JsonIgnoreProperties("fingerTable")
    Node succ;


    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getIntervalBegin() {
        return intervalBegin;
    }

    public void setIntervalBegin(int intervalBegin) {
        this.intervalBegin = intervalBegin;
    }

    public int getIntervalEnd() {
        return intervalEnd;
    }

    public void setIntervalEnd(int intervalEnd) {
        this.intervalEnd = intervalEnd;
    }

    public Node getSucc() {
        return succ;
    }

    public void setSucc(Node succ) {
        this.succ = succ;
    }


}
