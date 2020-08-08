// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------
package toolkit;

/**
 * For nominal labels, this model simply returns the majority class. For
 * continuous labels, it returns the mean value.
 * If the learning model you're using doesn't do as well as this one,
 * it's time to find a new learning model.
 */
public class BaselineLearner extends SupervisedLearner {

	double[] predictedLabels;

	public void train(DataMatrix featuresOnlyDataMatrix, DataMatrix labelsOnlyDataMatrix) throws Exception {
		predictedLabels = new double[labelsOnlyDataMatrix.getColCount()];
		for(int i = 0; i < labelsOnlyDataMatrix.getColCount(); i++) {
			if(labelsOnlyDataMatrix.getValueCountForAttributeAtColumn(i) == 0) {
				predictedLabels[i] = labelsOnlyDataMatrix.getColumnMean(i); // continuous
			}
			else {
				predictedLabels[i] = labelsOnlyDataMatrix.getMostCommonValueForColumn(i); // nominal
			}
		}
	}

	public void predictInstanceLabelsFromFeatures(double[] featuresForInstance, double[] arrayInWhichToPutLabels) throws Exception {
		for(int i = 0; i < predictedLabels.length; i++) {
			arrayInWhichToPutLabels[i] = predictedLabels[i];
		}
	}

}
