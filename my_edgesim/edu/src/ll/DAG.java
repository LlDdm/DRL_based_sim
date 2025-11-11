package ll;

import org.apache.commons.math3.geometry.spherical.twod.Edge;
import org.apache.poi.ss.formula.functions.T;

import java.util.*;

public class DAG {
    private List<Task> tasks;
    private HashSet<Task> criticalTasks;
    private int depth;
    private int maxWidth;
    List<Task> tpoSort;
    private HashMap<Integer, Task> TasksMap;

    DAG() {
        this.tasks = new ArrayList<>();
        this.criticalTasks = new HashSet<>();
        this.TasksMap = new HashMap<>();
    }

    void addTask(Task task) {
        tasks.add(task);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public Task getTask(int id) {

        return TasksMap.get(id);
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
    public int getDepth() { return depth; }

    public void setMaxWidth(int maxWidth) { this.maxWidth = maxWidth; }
    public int getMaxWidth() { return maxWidth; }

    public HashSet<Task> getCriticalTasks() { return criticalTasks; }

    public List<Task> getTpoSort() { return tpoSort; }

    public HashMap<Integer, Task> getTasksMap() { return TasksMap; }

    public void setTasksMap() {
        for(Task task : tasks) {
            TasksMap.put(task.get_taskId(), task);
        }
    }

    public void computeCriticalPath() {
        Map<Task, Double> earliestStart = new HashMap<>();
        Map<Task, Double> latestStart = new HashMap<>();
        List<Task> tpoSort = new ArrayList<>();
        Queue<Task> taskQueue = new LinkedList<>();
        Map<Task, Integer> inDegree = new HashMap<>();

        // 拓扑排序
        for (Task task : tasks) {
            if (task.getPredecessors().isEmpty()) {
                taskQueue.add(task);
                inDegree.put(task, 0);
            } else {
                inDegree.put(task, task.getPredecessors().size());
            }
        }

        while (!taskQueue.isEmpty()) {
            Task task = taskQueue.poll();
            tpoSort.add(task);  // 保证任务按拓扑顺序处理
            for (Task suc : task.getSuccessors()) {
                if(suc != null) {
                    int in = inDegree.get(suc);
                    inDegree.replace(suc, in - 1);
                    if (inDegree.get(suc) == 0) {
                        taskQueue.add(suc);
                    }
                }
            }

        }

        // Step 1: 计算每个任务的最早开始时间
        for (Task task : tpoSort) {
            if (task.getPredecessors().isEmpty()) {
                earliestStart.put(task,  0.0);
            } else {
                double MAX_start = Long.MIN_VALUE;
                for (Task pre : task.getPredecessors()) {
                    if (earliestStart.get(pre) + pre.getSize() + task.getPredecessorsMap().get(pre) > MAX_start) {
                        MAX_start = earliestStart.get(pre) + pre.getSize() + task.getPredecessorsMap().get(pre);
                    }
                }
                earliestStart.put(task, MAX_start);
            }
        }

        this.tpoSort = tpoSort;
        List<Task> revisedTasks = new ArrayList<>(tpoSort);
        Collections.reverse(revisedTasks);

        // Step 2: 计算每个任务的最迟开始时间
        for (Task task : revisedTasks) {
            if (task.getSuccessors().isEmpty()) {
                latestStart.put(task, earliestStart.get(task));
            } else {
                double Min_Start = Long.MAX_VALUE;
                for (Task suc : task.getSuccessors()) {
                    if (latestStart.get(suc) - task.getSuccessorsMap().get(suc) - task.getSize() < Min_Start) {
                        Min_Start = latestStart.get(suc) - task.getSuccessorsMap().get(suc) - task.getSize();
                    }
                }
                latestStart.put(task, Min_Start);
            }
        }

        // Step 3: 确定关键任务
        for (Task task : tasks) {
            if (earliestStart.get(task) != null && latestStart.get(task) != null &&
                    Objects.equals(earliestStart.get(task), latestStart.get(task))) {
                criticalTasks.add(task);
            }
        }
    }
}
