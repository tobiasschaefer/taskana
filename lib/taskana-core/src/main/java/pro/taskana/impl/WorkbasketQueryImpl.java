package pro.taskana.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.taskana.TaskanaEngine;
import pro.taskana.Workbasket;
import pro.taskana.WorkbasketQuery;
import pro.taskana.configuration.TaskanaEngineConfiguration;
import pro.taskana.exceptions.InvalidArgumentException;
import pro.taskana.impl.util.LoggerUtils;
import pro.taskana.model.WorkbasketAuthorization;
import pro.taskana.model.WorkbasketType;
import pro.taskana.model.mappings.WorkbasketAccessMapper;
import pro.taskana.security.CurrentUserContext;

/**
 * WorkbasketQuery for generating dynamic SQL.
 *
 * @author bbr
 */
public class WorkbasketQueryImpl implements WorkbasketQuery {

    private static final String LINK_TO_MAPPER = "pro.taskana.model.mappings.QueryMapper.queryWorkbasket";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkbasketQueryImpl.class);
    private String[] accessId;
    private WorkbasketAuthorization authorization;
    private String[] name;
    private String[] key;
    private String[] domain;
    private WorkbasketType[] type;
    private Date createdAfter;
    private Date createdBefore;
    private Date modifiedAfter;
    private Date modifiedBefore;
    private String description;
    private String[] owner;
    private TaskanaEngineImpl taskanaEngineImpl;

    public WorkbasketQueryImpl(TaskanaEngine taskanaEngine, WorkbasketAccessMapper workbasketAccessMapper) {
        this.taskanaEngineImpl = (TaskanaEngineImpl) taskanaEngine;
    }

    @Override
    public WorkbasketQuery keyIn(String... key) {
        this.key = key;
        return this;
    }

    @Override
    public WorkbasketQuery domainIn(String... domain) {
        this.domain = domain;
        return this;
    }

    @Override
    public WorkbasketQuery typeIn(WorkbasketType... type) {
        this.type = type;
        return this;
    }

    @Override
    public WorkbasketQuery nameIn(String... names) {
        this.name = names;
        return this;
    }

    @Override
    public WorkbasketQuery createdAfter(Date createdAfter) {
        this.createdAfter = createdAfter;
        return this;
    }

    @Override
    public WorkbasketQuery createdBefore(Date createdBefore) {
        this.createdBefore = createdBefore;
        return this;
    }

    @Override
    public WorkbasketQuery modifiedAfter(Date modifiedAfter) {
        this.modifiedAfter = modifiedAfter;
        return this;
    }

    @Override
    public WorkbasketQuery modifiedBefore(Date modifiedBefore) {
        this.modifiedBefore = modifiedBefore;
        return this;
    }

    @Override
    public WorkbasketQuery descriptionLike(String description) {
        this.description = description;
        return this;
    }

    @Override
    public WorkbasketQuery ownerIn(String... owners) {
        this.owner = owners;
        return this;
    }

    @Override
    public WorkbasketQuery accessIdsHavePersmission(WorkbasketAuthorization permission, String... accessIds)
        throws InvalidArgumentException {
        // Checking pre-conditions
        if (permission == null) {
            throw new InvalidArgumentException("Permission can´t be null.");
        }
        if (accessIds == null || accessIds.length == 0) {
            throw new InvalidArgumentException("accessIds can´t be NULL or empty.");
        }

        // set up permissions and ids
        this.authorization = permission;
        this.accessId = accessIds;
        lowercaseAccessIds();

        return this;
    }

    @Override
    public WorkbasketQuery callerHasPermission(WorkbasketAuthorization permission) throws InvalidArgumentException {
        String[] accessIds;
        // Check pre-conditions
        if (permission == null) {
            throw new InvalidArgumentException("Permission can´t be null.");
        }
        if (CurrentUserContext.getAccessIds() != null && CurrentUserContext.getAccessIds().size() > 0) {
            accessIds = new String[CurrentUserContext.getAccessIds().size()];
            accessIds = CurrentUserContext.getAccessIds().toArray(accessIds);
        } else {
            throw new InvalidArgumentException("CurrentUserContext need to have at least one accessId.");
        }

        // set up permissions and ids
        accessIds = new String[CurrentUserContext.getAccessIds().size()];
        accessIds = CurrentUserContext.getAccessIds().toArray(accessIds);
        this.authorization = permission;
        this.accessId = accessIds;
        lowercaseAccessIds();

        return this;
    }

    @Override
    public List<Workbasket> list() {
        LOGGER.debug("entry to list(), this = {}", this);
        List<Workbasket> workbaskets = null;
        try {
            taskanaEngineImpl.openConnection();
            workbaskets = taskanaEngineImpl.getSqlSession().selectList(LINK_TO_MAPPER, this);
            return workbaskets;
        } finally {
            taskanaEngineImpl.returnConnection();
            if (LOGGER.isDebugEnabled()) {
                int numberOfResultObjects = workbaskets == null ? 0 : workbaskets.size();
                LOGGER.debug("exit from list(). Returning {} resulting Objects: {} ", numberOfResultObjects,
                    LoggerUtils.listToString(workbaskets));
            }
        }
    }

    @Override
    public List<Workbasket> list(int offset, int limit) {
        LOGGER.debug("entry to list(offset = {}, limit = {}), this = {}", offset, limit, this);
        List<Workbasket> workbaskets = null;
        try {
            taskanaEngineImpl.openConnection();
            RowBounds rowBounds = new RowBounds(offset, limit);
            workbaskets = taskanaEngineImpl.getSqlSession().selectList(LINK_TO_MAPPER, this, rowBounds);
            return workbaskets;
        } finally {
            taskanaEngineImpl.returnConnection();
            if (LOGGER.isDebugEnabled()) {
                int numberOfResultObjects = workbaskets == null ? 0 : workbaskets.size();
                LOGGER.debug("exit from list(offset,limit). Returning {} resulting Objects: {} ", numberOfResultObjects,
                    LoggerUtils.listToString(workbaskets));
            }
        }
    }

    @Override
    public Workbasket single() {
        LOGGER.debug("entry to single(), this = {}", this);
        WorkbasketImpl workbasket = null;
        try {
            taskanaEngineImpl.openConnection();
            workbasket = taskanaEngineImpl.getSqlSession().selectOne(LINK_TO_MAPPER, this);
            return workbasket;
        } finally {
            taskanaEngineImpl.returnConnection();
            LOGGER.debug("exit from single(). Returning result {} ", workbasket);
        }
    }

    public String[] getAccessId() {
        return accessId;
    }

    public void setAccessId(String[] accessId) {
        this.accessId = accessId;
    }

    public WorkbasketAuthorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(WorkbasketAuthorization authorization) {
        this.authorization = authorization;
    }

    public String[] getName() {
        return name;
    }

    public void setName(String[] name) {
        this.name = name;
    }

    public String[] getKey() {
        return key;
    }

    public void setKey(String[] key) {
        this.key = key;
    }

    public String[] getDomain() {
        return domain;
    }

    public void setDomain(String[] domain) {
        this.domain = domain;
    }

    public WorkbasketType[] getType() {
        return type;
    }

    public void setType(WorkbasketType[] type) {
        this.type = type;
    }

    public Date getCreatedAfter() {
        return createdAfter;
    }

    public void setCreatedAfter(Date createdAfter) {
        this.createdAfter = createdAfter;
    }

    public Date getCreatedBefore() {
        return createdBefore;
    }

    public void setCreatedBefore(Date createdBefore) {
        this.createdBefore = createdBefore;
    }

    public Date getModifiedAfter() {
        return modifiedAfter;
    }

    public void setModifiedAfter(Date modifiedAfter) {
        this.modifiedAfter = modifiedAfter;
    }

    public Date getModifiedBefore() {
        return modifiedBefore;
    }

    public void setModifiedBefore(Date modifiedBefore) {
        this.modifiedBefore = modifiedBefore;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getOwner() {
        return owner;
    }

    public void setOwner(String[] owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WorkbasketQueryImpl [accessId=");
        builder.append(Arrays.toString(accessId));
        builder.append(", authorization=");
        builder.append(authorization);
        builder.append(", name=");
        builder.append(Arrays.toString(name));
        builder.append(", key=");
        builder.append(Arrays.toString(key));
        builder.append(", domain=");
        builder.append(Arrays.toString(domain));
        builder.append(", type=");
        builder.append(Arrays.toString(type));
        builder.append(", createdAfter=");
        builder.append(createdAfter);
        builder.append(", createdBefore=");
        builder.append(createdBefore);
        builder.append(", modifiedAfter=");
        builder.append(modifiedAfter);
        builder.append(", modifiedBefore=");
        builder.append(modifiedBefore);
        builder.append(", description=");
        builder.append(description);
        builder.append(", owner=");
        builder.append(Arrays.toString(owner));
        builder.append("]");
        return builder.toString();
    }

    private void lowercaseAccessIds() {
        if (TaskanaEngineConfiguration.shouldUseLowerCaseForAccessIds()) {
            for (int i = 0; i < this.accessId.length; i++) {
                String id = this.accessId[i];
                if (id != null) {
                    this.accessId[i] = id.toLowerCase();
                }
            }
        }
    }
}
