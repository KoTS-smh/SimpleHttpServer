package threadPool;

public interface ThreadPool<Job extends Runnable> {
    //执行一个job,需要实现Runnable
    void execute(Job job);
    //关闭线程池
    void shutdown();
    //立即关闭线程池
    //void shutDownNow()
    //增加工作着线程数
    void addWorkers(int num);
    //减少工作者线程数
    void removeWorkers(int num);
    //获得正在执行的任务数量
    int getJobSize();

}
