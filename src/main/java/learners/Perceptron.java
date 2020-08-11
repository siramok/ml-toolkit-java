package learners;

import java.util.Random;

import toolkit.DataMatrix;
import toolkit.SupervisedLearner;

public class Perceptron extends SupervisedLearner {
    Random rand;

    public Perceptron(Random rand) {
        this.rand = rand;
        throw new UnsupportedOperationException("The Perceptron class has not been implemented yet.");
    }

    @Override
    public void train(DataMatrix featuresNoBias, DataMatrix labels) throws Exception {

    }

    @Override
    public void predictInstanceLabelsFromFeatures(double[] featureVector, double[] arrayInWhichToPutLabels) throws Exception {

    }
}