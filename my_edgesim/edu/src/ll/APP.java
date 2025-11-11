package ll;

public class APP implements Comparable<APP> {
    private int Appid;
    private String AppName;
    private long startTime, deadline;
    private long excursionTime;
    private long offsetTime;
    private double inputsize;
    private double outputsize;
    private long lenth;
    private DAG dag;
    private double CCR;
    private double shape_factor;
    private long completeTime;
    private int mobileDeviceId;
    //private boolean isComplete;
    private long makeSpan;

    public APP(int Appid, String AppName, long startTime,long deadline,long excursionTime,long offsetTime,
               double inputsize, double outputsize, DAG dag,double CCR, double shape_factor ,int mobileDeviceId) {
        this.Appid = Appid;
        this.AppName = AppName;
        this.startTime = startTime;
        this.deadline = deadline;
        this.inputsize = inputsize;
        this.outputsize = outputsize;
//        this.lenth = lenth;
        this.dag = dag;
        this.CCR = CCR;
        this.shape_factor = shape_factor;
        this.mobileDeviceId = mobileDeviceId;
        //this.isComplete = false;
        this.makeSpan = 0;
        this.completeTime = 0;
        this.excursionTime = excursionTime;
        this.offsetTime = offsetTime;
    }

    public int getAppid() {
        return Appid;
    }

    public String getAppName() {
        return AppName;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDeadline() {
        return deadline;
    }

    public long getCompleteTime() {
        return completeTime;
    }

    public double getInputsize() {
        return inputsize;
    }

    public double getOutputsize() {
        return outputsize;
    }

    public long getLenth() {
        return lenth;
    }

    public double getShape_factor() {
        return shape_factor;
    }

    public double getCCR() {
        return CCR;
    }

    public DAG getDag() {
        return dag;
    }

    public void setAppName(String appName) {
        AppName = appName;
    }

    public void setAppid(int appid) {
        Appid = appid;
    }

    public void setCCR(double CCR) {
        this.CCR = CCR;
    }

    public void setCompleteTime(long completeTime) {
        this.completeTime = completeTime;
    }

    public void setDag(DAG dag) {
        this.dag = dag;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public void setInputsize(long inputsize) {
        this.inputsize = inputsize;
    }

    public void setLenth(long lenth) {
        this.lenth = lenth;
    }

    public void setOutputsize(long outputsize) {
        this.outputsize = outputsize;
    }

    public void setShape_factor(double shape_factor) {
        this.shape_factor = shape_factor;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getMobileDeviceId() { return mobileDeviceId; }

    public Task getstartTask(){ return dag.getTask(-1);}

    public Task getendTask(){ return dag.getTask(-2);}

    //public void setComplete(boolean complete) { isComplete = complete;}
    //public boolean isComplete() {return isComplete;}

    public void setMakeSpan(long makeSpan) {this.makeSpan = makeSpan;}
    public long getMakeSpan() {return makeSpan;}

    public long getExcursionTime() {return excursionTime;}
    public void setExcursionTime(long excursionTime) { this.excursionTime = excursionTime;}

    public long getOffsetTime() {return offsetTime;}
    public void setOffsetTime(long offsetTime) {this.offsetTime = offsetTime;}

    // 实现 Comparable 接口，按照截止时间排序
    @Override
    public int compareTo(APP other) {
        return Long.compare(this.deadline, other.deadline);
    }
}

