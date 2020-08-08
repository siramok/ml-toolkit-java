// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------
package toolkit;

public abstract class SupervisedLearner {

	// Before you call this method, you need to divide your data
	// into a feature matrix and a label matrix.
	public abstract void train(DataMatrix featuresOnlyDataMatrix, DataMatrix labelsOnlyDataMatrix) throws Exception;


	/**
	 * 
	 * @param featureVector a vector of features from which to predict a label or labels
	 * @param arrayInWhichToPutLabels array in which to put return values (i.e., label(s))
	 * @throws Exception
	 * 
	 * 	A feature vector goes in. A label vector comes out. (Some supervised
	 * learning algorithms only support one-dimensional label vectors. Some
	 * support multi-dimensional label vectors.)
	 * 
	 * Labels are returned using the passed in array in order to avoid unnecessary creation/
	 * destruction of arrays, improving the speed of prediction.
	 */
	public abstract void predictInstanceLabelsFromFeatures(double[] featureVector, double[] arrayInWhichToPutLabels) throws Exception;

	// The model must be trained before you call this method. If the label is nominal,
	// it returns the predictive accuracy. If the label is continuous, it returns
	// the root mean squared error (RMSE). If confusion is non-NULL, and the
	// output label is nominal, then confusion will hold stats for a confusion matrix.
	public double measurePredictiveAccuracy(DataMatrix features, DataMatrix labels, DataMatrix confusion) throws Exception
	{
		if(features.getRowCount() != labels.getRowCount())
			throw(new Exception("Expected the features and labels to have the same number of rows"));
		if(labels.getColCount() != 1)
			throw(new Exception("Sorry, this method currently only supports one-dimensional labels"));
		if(features.getRowCount() == 0)
			throw(new Exception("Expected at least one row"));

		int labelValues = labels.getValueCountForAttributeAtColumn(0);
		if(labelValues == 0) // If the label is continuous...
		{
			// The label is continuous, so measure root mean squared error
			double sse = 0.0;
			double[] predictedLabels = new double[1];
			for(int i = 0; i < features.getRowCount(); i++)
			{
				double[] featuresForInstance = features.getRow(i);
				double[] targ = labels.getRow(i);
				predictInstanceLabelsFromFeatures(featuresForInstance, predictedLabels);
				double delta = targ[0] - predictedLabels[0];
				sse += (delta * delta);
			}
			return Math.sqrt(sse / features.getRowCount());
		}
		else
		{
			// The label is nominal, so measure predictive accuracy
			if(confusion != null)
			{
				confusion.setSize(labelValues, labelValues);
				for(int i = 0; i < labelValues; i++)
					confusion.setAttributeName(i, labels.getAttributeValueName(0, i));
			}
			int correctCount = 0;
			for(int i = 0; i < features.getRowCount(); i++)
			{
				double[] featuresForInstance = features.getRow(i);
				int targ = (int)labels.getValueAt(i, 0);
				if(targ >= labelValues)
					throw new Exception("The label is out of ransge");
				double[] predictedLabels = new double[1]; 
				predictInstanceLabelsFromFeatures(featuresForInstance, predictedLabels);
				int pred = (int)predictedLabels[0];
				if(confusion != null)
					confusion.setValue(targ, pred, confusion.getValueAt(targ, pred) + 1);
				if(pred == targ)
					correctCount++;
			}
			return (double)correctCount / features.getRowCount();
		}
	}

}
