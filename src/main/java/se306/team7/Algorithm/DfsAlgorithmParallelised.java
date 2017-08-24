package se306.team7.Algorithm;

import pt.runtime.TaskID;
import pt.runtime.TaskIDGroup;
import pt.runtime.TaskInfo;
import pt.runtime.TaskpoolFactory;
import se306.team7.CostEstimatedSchedule;
import se306.team7.Digraph.Digraph;
import se306.team7.Schedule;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class DfsAlgorithmParallelised implements IAlgorithm{

    private Digraph _digraph;
    private Set<ICostEstimator> _costEstimators;
    private IScheduleGenerator _scheduleGenerator;
    private int _currentBestCost;
    private List<Schedule> _subTreeSchedules;

    public DfsAlgorithmParallelised (Set<ICostEstimator> costEstimators, IScheduleGenerator scheduleGenerator) {
        _costEstimators = costEstimators;
        _scheduleGenerator = scheduleGenerator;
        _currentBestCost = Integer.MAX_VALUE;
        _subTreeSchedules = new ArrayList<Schedule>();
    }

    private Schedule runAllSubtrees(List<Schedule> partialSchedulesToComplete, Digraph digraph, int processorCount) {
        try {

            Method aStarAlgorithmMethod = AStarParalleliser.class.getMethod("getSchedule", Schedule.class, int.class, Digraph.class);

            TaskIDGroup<Void> id = new TaskIDGroup<Void>(_subTreeSchedules.size());

            for (Schedule schedule : _subTreeSchedules) {

                TaskInfo taskInfo = new TaskInfo();
                taskInfo.setMethod(aStarAlgorithmMethod);
                taskInfo.setParameters(schedule, processorCount, digraph);

                TaskID<Void> task = TaskpoolFactory.getTaskpool().enqueue(taskInfo);

                id.add(task);
            }

            id.waitTillFinished();

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException ex) {
            System.exit(1); // Something has gone horribly wrong
        }

        return AStarParalleliser.bestSchedule;
    }

    public Schedule getOptimalSchedule(Digraph digraph, int numOfProcessors, Schedule schedule) {
        _digraph = digraph;

        List<Schedule> nextSchedules = _scheduleGenerator.generateSchedules(schedule, digraph);

        if (nextSchedules.isEmpty()) {
            if (schedule.endTime() < _currentBestCost)
                _currentBestCost = Math.min(_currentBestCost, schedule.endTime());

            return schedule;
        }

        List<CostEstimatedSchedule> costEstimatedSchedules = new ArrayList<CostEstimatedSchedule>();

        for (Schedule nextSchedule : nextSchedules) {
            int costEstimate = getCostEstimate(nextSchedule);
            if (costEstimate < _currentBestCost)
                costEstimatedSchedules.add(new CostEstimatedSchedule(nextSchedule, costEstimate));
        }

        if (costEstimatedSchedules.isEmpty()) {
            return null;
        }

        Collections.sort(costEstimatedSchedules);

        Schedule bestSchedule = null;

        for (CostEstimatedSchedule nextSchedule : costEstimatedSchedules) {
            if(digraph.getNodes().size() - schedule.getTasks().size() <= 10){

                _subTreeSchedules.add(nextSchedule.getSchedule());

            }else {
                Schedule s = getOptimalSchedule(digraph, numOfProcessors, nextSchedule.getSchedule());

                if (s == null)
                    continue;

                if (bestSchedule == null || s.endTime() < bestSchedule.endTime()) {
                    bestSchedule = s;
                }
            }
        }

        return bestSchedule;
    }

    /**
     * Get the optimal schedule for a given digraph containing tasks and task dependencies
     * @param digraph Represents tasks and task dependencies
     * @param numOfProcessors Processors available to concurrently complete tasks
     * @return Optimal complete schedule
     */
    public Schedule getOptimalSchedule(Digraph digraph, int numOfProcessors) {
        getOptimalSchedule(digraph, numOfProcessors, new Schedule(numOfProcessors));

        return runAllSubtrees(_subTreeSchedules, digraph, numOfProcessors);
    }

    /**
     * Cost estimate of a schedule is given by the maximum out of (the latest task end time) or
     * (newestAddedTask's start time plus its bottom level)
     * @param schedule
     * @return
     */
    public int getCostEstimate(Schedule schedule) {

        int currentMax = 0;

        for (ICostEstimator costEstimator : _costEstimators) {
            currentMax = Math.max(currentMax, costEstimator.estimateCost(schedule, _digraph));
        }

        return currentMax;
    }

}