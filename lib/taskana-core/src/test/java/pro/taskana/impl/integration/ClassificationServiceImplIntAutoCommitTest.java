package pro.taskana.impl.integration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

import org.h2.store.fs.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pro.taskana.Classification;
import pro.taskana.ClassificationService;
import pro.taskana.TaskanaEngine;
import pro.taskana.TaskanaEngine.ConnectionManagementMode;
import pro.taskana.configuration.TaskanaEngineConfiguration;
import pro.taskana.exceptions.ClassificationAlreadyExistException;
import pro.taskana.exceptions.ClassificationNotFoundException;
import pro.taskana.exceptions.InvalidArgumentException;
import pro.taskana.exceptions.NotAuthorizedException;
import pro.taskana.impl.ClassificationImpl;
import pro.taskana.impl.TaskanaEngineImpl;
import pro.taskana.impl.configuration.DBCleaner;
import pro.taskana.impl.configuration.TaskanaEngineConfigurationTest;

/**
 * Integration Test for ClassificationServiceImpl with connection management mode AUTOCOMMIT.
 *
 * @author EH
 */
public class ClassificationServiceImplIntAutoCommitTest {

    static int counter = 0;

    private DataSource dataSource;
    private ClassificationService classificationService;
    private TaskanaEngineConfiguration taskanaEngineConfiguration;
    private TaskanaEngine taskanaEngine;
    private TaskanaEngineImpl taskanaEngineImpl;
    private static final String ID_PREFIX_CLASSIFICATION = "CLI";

    @BeforeClass
    public static void resetDb() throws SQLException {
        DataSource ds = TaskanaEngineConfigurationTest.getDataSource();
        DBCleaner cleaner = new DBCleaner();
        cleaner.clearDb(ds, true);
    }

    @Before
    public void setup() throws FileNotFoundException, SQLException, LoginException {
        dataSource = TaskanaEngineConfigurationTest.getDataSource();
        taskanaEngineConfiguration = new TaskanaEngineConfiguration(dataSource, false);
        taskanaEngine = taskanaEngineConfiguration.buildTaskanaEngine();
        classificationService = taskanaEngine.getClassificationService();
        taskanaEngineImpl = (TaskanaEngineImpl) taskanaEngine;
        taskanaEngineImpl.setConnectionManagementMode(ConnectionManagementMode.AUTOCOMMIT);
        DBCleaner cleaner = new DBCleaner();
        cleaner.clearDb(dataSource, false);
    }

    @Test
    public void testInsertClassifications() throws Exception {
        final String domain = "test-domain";
        final String key = "dummy-key";
        ClassificationImpl expectedClassification;
        Classification actualClassification;
        Classification actualClassification2;

        // empty classification (root)
        expectedClassification = (ClassificationImpl) this.createDummyClassificationWithUniqueKey();
        expectedClassification = (ClassificationImpl) classificationService
            .createClassification(expectedClassification);
        actualClassification = classificationService.getClassification(expectedClassification.getKey(),
            expectedClassification.getDomain());
        assertThat(actualClassification, not(equalTo(null)));
        assertThat(actualClassification.getCreated(), not(equalTo(null)));
        assertThat(actualClassification.getId(), not(equalTo(null)));
        assertThat(actualClassification.getId(), startsWith(ID_PREFIX_CLASSIFICATION));

        // specific to domain + root
        expectedClassification = (ClassificationImpl) this.createDummyClassificationWithUniqueKey();
        expectedClassification.setDomain(domain);
        expectedClassification.setKey(key);
        expectedClassification = (ClassificationImpl) classificationService
            .createClassification(expectedClassification);
        actualClassification = classificationService.getClassification(expectedClassification.getKey(),
            expectedClassification.getDomain());
        actualClassification2 = classificationService.getClassification(expectedClassification.getKey(), "");
        assertThat(actualClassification, not(equalTo(null)));
        assertThat(actualClassification.getCreated(), not(equalTo(null)));
        assertThat(actualClassification.getId(), not(equalTo(null)));
        assertThat(actualClassification.getKey(), equalTo(key));
        assertThat(actualClassification.getDomain(), equalTo(domain));
        assertThat(actualClassification.getId(), startsWith(ID_PREFIX_CLASSIFICATION));
        assertThat(actualClassification2, not(equalTo(null)));
        assertThat(actualClassification2.getCreated(), not(equalTo(null)));
        assertThat(actualClassification2.getId(), not(equalTo(null)));
        assertThat(actualClassification2.getId(), not(equalTo(actualClassification.getId())));
        assertThat(actualClassification2.getKey(), equalTo(key));
        assertThat(actualClassification2.getDomain(), equalTo(""));
        assertThat(actualClassification2.getId(), startsWith(ID_PREFIX_CLASSIFICATION));

        // does exist already
        try {
            expectedClassification = (ClassificationImpl) this.createDummyClassificationWithUniqueKey();
            expectedClassification.setKey(key);
            expectedClassification.setDomain(domain);
            classificationService.createClassification(expectedClassification);
            fail("Should have thrown 'ClassificationAlreadyExistException' here.");
        } catch (ClassificationAlreadyExistException e) {
        }

        // new classification but root existing
        expectedClassification = (ClassificationImpl) this.createDummyClassificationWithUniqueKey();
        expectedClassification.setKey(key);
        expectedClassification.setDomain(domain + "_2");
        classificationService.createClassification(expectedClassification);
        actualClassification = classificationService.getClassification(key, domain + "_2");
        assertThat(actualClassification, not(equalTo(null)));
        assertThat(actualClassification.getCreated(), not(equalTo(null)));
        assertThat(actualClassification.getId(), not(equalTo(null)));
        assertThat(actualClassification.getKey(), equalTo(key));
        assertThat(actualClassification.getDomain(), equalTo(domain + "_2"));
        assertThat(actualClassification.getId(), startsWith(ID_PREFIX_CLASSIFICATION));
        List<Classification> rootResults = classificationService.getAllClassificationsWithKey(key, "");
        assertThat(rootResults.size(), equalTo(1));

        // invalid serviceLevel
        try {
            expectedClassification = (ClassificationImpl) this.createDummyClassificationWithUniqueKey();
            expectedClassification.setDomain(domain + "_3");
            expectedClassification.setKey("");
            expectedClassification.setServiceLevel("ASAP");
            classificationService.createClassification(expectedClassification);
            fail("Should have thrown IllegalArgumentException, because ServiceLevel is invalid.");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testFindAllClassifications()
        throws NotAuthorizedException, ClassificationAlreadyExistException, ClassificationNotFoundException,
        InvalidArgumentException {
        Classification classification0 = this.createDummyClassificationWithUniqueKey();
        classificationService.createClassification(classification0);
        Classification classification1 = this.createDummyClassificationWithUniqueKey();
        classificationService.createClassification(classification1);
        Classification classification2 = this.createDummyClassificationWithUniqueKey();
        classification2.setParentClassificationKey(classification0.getKey());
        classificationService.createClassification(classification2);

        Assert.assertEquals(2 + 1, classificationService.getClassificationTree().size());
    }

    @Test
    public void testModifiedClassification()
        throws ClassificationAlreadyExistException, ClassificationNotFoundException {

        Classification classification = this.createDummyClassificationWithUniqueKey();
        classificationService.createClassification(classification);
        classification.setDescription("TEST SOMETHING");
        classificationService.updateClassification(classification);

        Assert.assertEquals(classification.getValidFrom(), Date.valueOf(LocalDate.now()));
    }

    @Test
    public void testInsertAndClassificationMapper()
        throws NotAuthorizedException, ClassificationAlreadyExistException, ClassificationNotFoundException,
        InvalidArgumentException {
        Classification classification = this.createDummyClassificationWithUniqueKey();
        Classification actualClassification = classificationService.createClassification(classification);
        Date today = Date.valueOf(LocalDate.now());
        List<Classification> list = classificationService.createClassificationQuery()
            .validInDomain(Boolean.TRUE)
            .created(today)
            .validFrom(today)
            .validUntil(Date.valueOf("9999-12-31"))
            .list();
        Assert.assertEquals(1, list.size());
        assertThat(actualClassification, equalTo(classification));
    }

    @Test
    public void testUpdateAndClassificationMapper()
        throws NotAuthorizedException, ClassificationAlreadyExistException, ClassificationNotFoundException,
        InvalidArgumentException {
        Classification classification = this.createDummyClassificationWithUniqueKey();
        classificationService.createClassification(classification);
        System.out.println(classification.getId());
        classification.setDescription("description");
        classificationService.updateClassification(classification);

        List<Classification> list = classificationService.createClassificationQuery()
            .validUntil(Date.valueOf("9999-12-31"))
            .list();
        Assert.assertEquals(1, list.size());
        list = classificationService.createClassificationQuery().validInDomain(true).list();
        Assert.assertEquals(2, list.size());

        classification.setDomain("domain");
        classificationService.updateClassification(classification);
        System.out.println(classification.getId());
        list = classificationService.createClassificationQuery().validUntil(Date.valueOf("9999-12-31")).list();
        Assert.assertEquals(2, list.size());

        System.out.println(classification.getParentClassificationKey());

        List<Classification> temp = classificationService.getClassificationTree();
        List<ClassificationImpl> allClassifications = new ArrayList<>();
        temp.stream().forEach(c -> allClassifications.add((ClassificationImpl) c));
        Assert.assertEquals(2, allClassifications.size());
    }

    @Test
    public void testFindWithClassificationMapperDomainAndCategory()
        throws NotAuthorizedException, ClassificationAlreadyExistException, ClassificationNotFoundException,
        InvalidArgumentException {
        Classification classification1 = this.createDummyClassificationWithUniqueKey();
        classification1.setDomain("domain1");
        classification1.setCategory("category1");
        classificationService.createClassification(classification1);
        Classification classification2 = this.createDummyClassificationWithUniqueKey();
        classification2.setDomain("domain2");
        classification2.setCategory("category1");
        classificationService.createClassification(classification2);
        Classification classification3 = this.createDummyClassificationWithUniqueKey();
        classification3.setDomain("domain1");
        classification3.setCategory("category2");
        classificationService.createClassification(classification3);

        List<Classification> list = classificationService.createClassificationQuery()
            .category("category1")
            .domain("domain1")
            .list();
        Assert.assertEquals(1, list.size());
        list = classificationService.createClassificationQuery().domain("domain1", "domain3").list();
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testFindWithClassificationMapperCustomAndCategory()
        throws NotAuthorizedException, ClassificationAlreadyExistException, ClassificationNotFoundException,
        InvalidArgumentException {
        Classification classification1 = this.createDummyClassificationWithUniqueKey();
        classification1.setDescription("DESC1");
        classification1.setCategory("category1");
        classificationService.createClassification(classification1);
        Classification classification2 = this.createDummyClassificationWithUniqueKey();
        classification2.setDescription("DESC1");
        classification2.setCustom1("custom1");
        classification2.setCategory("category1");
        classificationService.createClassification(classification2);
        Classification classification3 = this.createDummyClassificationWithUniqueKey();
        classification3.setCustom1("custom2");
        classification3.setCustom2("custom1");
        classification3.setCategory("category2");
        classificationService.createClassification(classification3);
        Classification classification4 = this.createDummyClassificationWithUniqueKey();
        classification4.setDescription("description2");
        classification4.setCustom8("custom2");
        classification4.setCategory("category1");
        classificationService.createClassification(classification4);

        List<Classification> list = classificationService.createClassificationQuery()
            .descriptionLike("DESC1")
            .customFields("custom1")
            .list();
        Assert.assertEquals(1, list.size());
        list = classificationService.createClassificationQuery().customFields("custom2").list();
        Assert.assertEquals(2, list.size());
        list = classificationService.createClassificationQuery().descriptionLike("DESC1").category("category1").list();
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testFindWithClassificationMapperPriorityTypeAndParent()
        throws NotAuthorizedException, ClassificationAlreadyExistException, ClassificationNotFoundException,
        NumberFormatException, InvalidArgumentException {
        Classification classification = this.createDummyClassificationWithUniqueKey();
        classification.setPriority(Integer.decode("5"));
        classification.setType("type1");
        classificationService.createClassification(classification);
        Classification classification1 = this.createDummyClassificationWithUniqueKey();
        classification1.setPriority(Integer.decode("3"));
        classification1.setType("type1");
        classification1.setParentClassificationKey(classification.getKey());
        classificationService.createClassification(classification1);
        Classification classification2 = this.createDummyClassificationWithUniqueKey();
        classification2.setPriority(Integer.decode("5"));
        classification2.setType("type2");
        classification2.setParentClassificationKey(classification.getKey());
        classificationService.createClassification(classification2);
        Classification classification3 = this.createDummyClassificationWithUniqueKey();
        classification3.setPriority(Integer.decode("5"));
        classification3.setType("type1");
        classification3.setParentClassificationKey(classification1.getKey());
        classificationService.createClassification(classification3);

        List<Classification> list = classificationService.createClassificationQuery()
            .parentClassificationKey(classification.getKey())
            .list();
        Assert.assertEquals(2, list.size());
        list = classificationService.createClassificationQuery().type("type1").priority(Integer.decode("5")).list();
        Assert.assertEquals(2, list.size());
        list = classificationService.createClassificationQuery()
            .priority(Integer.decode("5"))
            .type("type1")
            .parentClassificationKey(classification1.getKey())
            .list();
        Assert.assertEquals(1, list.size());
    }

    @Test
    public void testFindWithClassificationMapperServiceLevelNameAndDescription()
        throws NotAuthorizedException, ClassificationAlreadyExistException, ClassificationNotFoundException,
        InvalidArgumentException {
        int all = 0;
        Classification classification = this.createDummyClassificationWithUniqueKey();
        classification.setServiceLevel("P1D");
        classification.setName("name1");
        classification.setDescription("desc");
        classificationService.createClassification(classification);
        all++;
        Classification classification1 = this.createDummyClassificationWithUniqueKey();
        classification1.setServiceLevel("P1DT1H");
        classification1.setName("name1");
        classification1.setDescription("desc");
        classificationService.createClassification(classification1);
        all++;
        Classification classification2 = this.createDummyClassificationWithUniqueKey();
        classification2.setServiceLevel("P1D");
        classification2.setName("name");
        classification2.setDescription("desc");
        classificationService.createClassification(classification2);
        all++;
        Classification classification3 = this.createDummyClassificationWithUniqueKey();
        classification3.setName("name1");
        classification3.setDescription("description");
        classificationService.createClassification(classification3);
        all++;

        List<Classification> list = classificationService.createClassificationQuery().name("name").list();
        Assert.assertEquals(1, list.size());
        list = classificationService.createClassificationQuery().serviceLevel("P1D").descriptionLike("desc").list();
        Assert.assertEquals(2, list.size());
        list = classificationService.createClassificationQuery().serviceLevel("P1DT1H").name("name").list();
        Assert.assertEquals(0, list.size());
        list = classificationService.createClassificationQuery().descriptionLike("desc%").list();
        Assert.assertEquals(all, list.size());
    }

    @Test
    public void testDefaultSettingsWithClassificationMapper()
        throws NotAuthorizedException, ClassificationAlreadyExistException, ClassificationNotFoundException,
        InvalidArgumentException {
        Classification classification = this.createDummyClassificationWithUniqueKey();
        Classification classification1 = this.createDummyClassificationWithUniqueKey();
        classificationService.createClassification(classification);
        classificationService.createClassification(classification1);
        classification1.setParentClassificationKey(classification.getKey());
        classificationService.updateClassification(classification1);

        List<Classification> list = classificationService.createClassificationQuery()
            .parentClassificationKey("")
            .list();
        Assert.assertEquals(2, list.size());
        list = classificationService.createClassificationQuery().validUntil(Date.valueOf("9999-12-31")).list();
        Assert.assertEquals(2, list.size());

        List<Classification> listAll = classificationService.createClassificationQuery().list();
        list = classificationService.createClassificationQuery().validFrom(Date.valueOf(LocalDate.now())).list();
        Assert.assertEquals(listAll.size(), list.size());
        list = classificationService.createClassificationQuery().validInDomain(true).list();
        Assert.assertEquals(listAll.size(), list.size());
        list = classificationService.createClassificationQuery().created(Date.valueOf(LocalDate.now())).list();
        Assert.assertEquals(listAll.size(), list.size());

        list = classificationService.createClassificationQuery().domain("domain1").validInDomain(false).list();
        Assert.assertEquals(0, list.size());
        list = classificationService.createClassificationQuery()
            .validFrom(Date.valueOf((LocalDate.now())))
            .validUntil(Date.valueOf(LocalDate.now().minusDays(1)))
            .list();
        Assert.assertEquals(1, list.size());
    }

    @AfterClass
    public static void cleanUpClass() {
        FileUtils.deleteRecursive("~/data", true);
    }

    private Classification createDummyClassificationWithUniqueKey() {
        Classification classification = classificationService.newClassification();
        classification.setKey("TEST" + counter);
        counter++;
        return classification;
    }
}
