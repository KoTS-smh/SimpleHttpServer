package threadPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleThreadPool<Job extends Runnable> implements ThreadPool<Job> {
    //线程池最大限制数
    private static final int MAX_WORKER_NUM = 16;
    //默认线程池大小
    private static final int DEFAULT_WORKER_NUM = 8;
    //线程池最小限制
    private static final int MIN_WORKER_NUM = 1;
    //工作列表
    private final LinkedList<Job> jobs = new LinkedList<Job>();
    //工作者列表
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());
    //工作者线程数
    private int workerNum = DEFAULT_WORKER_NUM;
    //用于线程编号生成
    private AtomicInteger threadNum = new AtomicInteger();

    public SimpleThreadPool(){
        initializeWorkers(DEFAULT_WORKER_NUM);
    }

    public SimpleThreadPool(int num){
        workerNum = num>MAX_WORKER_NUM?MAX_WORKER_NUM:num<MIN_WORKER_NUM?MIN_WORKER_NUM:num;
        initializeWorkers(workerNum);
    }

    //初始化工作者
    private void initializeWorkers(int num){
        for (int i=0;i<num;i++){
            Worker worker = new Worker();
            workers.add(worker);
            Thread thread = new Thread(worker,"Worker-"+threadNum.incrementAndGet());
            thread.start();
        }
    }

    @Override
    public void execute(Job job) {
        if (job != null){
            synchronized (jobs){
                jobs.addLast(job);
                jobs.notify();
            }
        }
    }

    @Override
    public void shutdown() {
        for (Worker worker:workers){
            worker.shutdown();
        }
    }

    @Override
    public void addWorkers(int num) {
        synchronized (jobs){
            if (num+workerNum>MAX_WORKER_NUM){
                num = MAX_WORKER_NUM - workerNum;
            }
            initializeWorkers(num);
            workerNum += num;
        }
    }

    @Override
    public void removeWorkers(int num) {
        synchronized (jobs) {
            if (num >= workerNum) {
                throw new IllegalArgumentException();
            }
            for (int i=0;i<num;i++){
                workers.get(i).shutdown();
            }
            workerNum -= num;
        }
    }

    @Override
    public int getJobSize() {
        return jobs.size();
    }

    //工作者。负责消费任务
    class Worker implements Runnable{
        //volatile保证可见性
        private volatile boolean running  = true;
        @Override
        public void run() {
            while (running){
                Job job = null;
                synchronized (jobs){
                    while (jobs.isEmpty()){
                        try {
                            jobs.wait();
                        } catch (InterruptedException e) {
                            //当有外部对工作者线程进行中断操作，返回
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    job = jobs.removeFirst();
                }
                if (job != null){
                    try {
                        job.run();
                    }catch (Exception ignored){
                        //忽略Job执行时的异常
                    }
                }
            }
        }

        public void shutdown(){
            running = false;
        }
    }
}
