package me.lauriichan.spigot.justlootit.nms.convert;

public final class ProtoThread extends Thread {

    private final ProtoExecutor executor;

    private volatile String region;
    private volatile int cx, cz;
    private volatile String task;
    
    public ProtoThread(int id, ProtoExecutor executor) {
        setName("JLI-ConversionWorker-" + id);
        setDaemon(true);
        this.executor = executor;
    }

    @Override
    public void run() {
        executor.runQueue();
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public void setChunk(int cx, int cz) {
        this.cx = cx;
        this.cz = cz;
    }

    public void setTask(String task) {
        this.task = task;
    }
    
    public String region() {
        return region;
    }
    
    public int cx() {
        return cx;
    }
    
    public int cz() {
        return cz;
    }
    
    public String task() {
        return task;
    }
    
}
