package pro.taskana.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.taskana.TaskMonitorService;
import pro.taskana.TaskanaEngine;
import pro.taskana.impl.util.LoggerUtils;
import pro.taskana.model.DueWorkbasketCounter;
import pro.taskana.model.TaskState;
import pro.taskana.model.TaskStateCounter;
import pro.taskana.model.mappings.TaskMonitorMapper;

/**
 * This is the implementation of TaskMonitorService.
 */
public class TaskMonitorServiceImpl implements TaskMonitorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskMonitorServiceImpl.class);

    private TaskanaEngineImpl taskanaEngineImpl;

    private TaskMonitorMapper taskMonitorMapper;

    public TaskMonitorServiceImpl(TaskanaEngine taskanaEngine, TaskMonitorMapper taskMonitorMapper) {
        super();
        this.taskanaEngineImpl = (TaskanaEngineImpl) taskanaEngine;
        this.taskMonitorMapper = taskMonitorMapper;
    }

    @Override
    public List<TaskStateCounter> getTaskCountForState(List<TaskState> states) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("entry to getTaskCountForState(states = {})", LoggerUtils.listToString(states));
        }
        List<TaskStateCounter> result = null;
        try {
            taskanaEngineImpl.openConnection();
            result = taskMonitorMapper.getTaskCountForState(states);
            return result;
        } finally {
            taskanaEngineImpl.returnConnection();
            if (LOGGER.isDebugEnabled()) {
                int numberOfResultObjects = result == null ? 0 : result.size();
                LOGGER.debug("exit from getTaskCountForState(). Returning {} resulting Objects: {} ",
                    numberOfResultObjects, LoggerUtils.listToString(result));
            }
        }
    }

    @Override
    public long getTaskCountForWorkbasketByDaysInPastAndState(String workbasketId, long daysInPast,
        List<TaskState> states) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                "entry to getTaskCountForWorkbasketByDaysInPastAndState(workbasketId {}, daysInPast={}, states = {})",
                workbasketId, daysInPast, LoggerUtils.listToString(states));
        }
        long result = -1;
        try {
            taskanaEngineImpl.openConnection();
            LocalDate time = LocalDate.now();
            time = time.minusDays(daysInPast);
            Date fromDate = Date.valueOf(time);
            result = taskMonitorMapper.getTaskCountForWorkbasketByDaysInPastAndState(workbasketId, fromDate, states);
            return result;
        } finally {
            taskanaEngineImpl.returnConnection();
            LOGGER.debug("exit from getTaskCountForWorkbasketByDaysInPastAndState(). Returning result {} ", result);
        }
    }

    @Override
    public List<DueWorkbasketCounter> getTaskCountByWorkbasketAndDaysInPastAndState(long daysInPast,
        List<TaskState> states) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("entry to getTaskCountByWorkbasketAndDaysInPastAndState(daysInPast = {}, states = {})",
                daysInPast, LoggerUtils.listToString(states));
        }
        List<DueWorkbasketCounter> result = null;
        try {
            taskanaEngineImpl.openConnection();
            LocalDate time = LocalDate.now();
            time = time.minusDays(daysInPast);
            Date fromDate = Date.valueOf(time);
            result = taskMonitorMapper.getTaskCountByWorkbasketIdAndDaysInPastAndState(fromDate, states);
            return result;
        } finally {
            taskanaEngineImpl.returnConnection();
            if (LOGGER.isDebugEnabled()) {
                int numberOfResultObjects = result == null ? 0 : result.size();
                LOGGER.debug(
                    "exit from getTaskCountByWorkbasketAndDaysInPastAndState(daysInPast,states). Returning {} resulting Objects: {} ",
                    numberOfResultObjects, LoggerUtils.listToString(result));
            }
        }
    }

}
