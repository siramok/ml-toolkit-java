package learners;

import org.junit.*;
import toolkit.SupervisedLearner;

import java.util.Random;

public class PerceptronTest {
    private SupervisedLearner fixture;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        Random rand = new Random();
        fixture = new Perceptron(rand);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void exampleTest1() {
        // Adding tests for your learners is not required, but the option exists! As an example:
        // Assert.assertEquals(42, fixture.exampleMethod(xParam, yParam));
    }
}
