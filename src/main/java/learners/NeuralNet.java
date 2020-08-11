package learners;

import java.util.Random;

import toolkit.DataMatrix;
import toolkit.SupervisedLearner;

public class NeuralNet extends SupervisedLearner {
    Random rand;

    public NeuralNet(Random rand) {
        this.rand = rand;
        throw new UnsupportedOperationException("The NeuralNet class has not been implemented yet.");
    }

    @Override
    public void train(DataMatrix featuresOnlyDataMatrix, DataMatrix labelsOnlyDataMatrix) throws Exception {

    }

    @Override
    public void predictInstanceLabelsFromFeatures(double[] featureVector, double[] arrayInWhichToPutLabels) throws Exception {

    }
}
