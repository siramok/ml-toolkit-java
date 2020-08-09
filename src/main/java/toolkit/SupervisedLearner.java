// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------
package toolkit;

public abstract class SupervisedLearner {

	/**
	 *
	 * @param featuresOnlyDataMatrix a DataMatrix of values for the training algorithm to operate on
	 * @param labelsOnlyDataMatrix a DataMatrix of labels corresponding to each row of features
	 * @throws Exception
	 *
	 * Before you call this method, you need to divide your data
	 * into a feature matrix and a label matrix.
	 */
	public abstract void train(DataMatrix featuresOnlyDataMatrix, DataMatrix labelsOnlyDataMatrix) throws Exception;

	/**
	 * 
	 * @param featureVector a vector of features from which to predict a label or labels
	 * @param arrayInWhichToPutLabels array in which to put return values (i.e., label(s))
	 * @throws Exception
	 * 
	 * A feature vector goes in. A label vector comes out. (Some supervised
	 * learning algorithms only support one-dimensional label vectors. Some
	 * support multi-dimensional label vectors.)
	 * 
	 * Labels are returned using the passed in array in order to avoid unnecessary creation/
	 * destruction of arrays, improving the speed of prediction.
	 */
	public abstract void predictInstanceLabelsFromFeatures(double[] featureVector, double[] arrayInWhichToPutLabels) throws Exception;

	/**
	 *
	 * @param featuresOnlyDataMatrix a DataMatrix of values for the model to predict from
	 * @param labelsOnlyDataMatrix a DataMatrix of labels corresponding to each row of features
	 * @param confusion an empty DataMatrix that will be populated with the model's prediction data
	 * @return a double representing the trained model's prediction accuracy
	 * @throws Exception
	 *
	 * The model must be trained before you call this method. If the label is nominal,
	 * it returns the predictive accuracy. If the label is continuous, it returns
	 * the root mean squared error (RMSE). If confusion is non-NULL, and the
	 * output label is nominal, then confusion will hold stats for a confusion matrix.
	 */
	public double measurePredictiveAccuracy(DataMatrix featuresOnlyDataMatrix, DataMatrix labelsOnlyDataMatrix, DataMatrix confusion) throws Exception
	{
		if(featuresOnlyDataMatrix.getRowCount() != labelsOnlyDataMatrix.getRowCount())
			throw new Exception("Expected the features and labels to have the same number of rows");
		if(labelsOnlyDataMatrix.getColCount() != 1)
			throw new Exception("Sorry, this method currently only supports one-dimensional labels");
		if(featuresOnlyDataMatrix.getRowCount() == 0)
			throw new Exception("Expected at least one row");

		int labelValues = labelsOnlyDataMatrix.getValueCountForAttributeAtColumn(0);
		if(labelValues == 0)
		{
			// The label is continuous, so measure root mean squared error
			double sse = 0.0;
			double[] predictedLabels = new double[1];
			for(int i = 0; i < featuresOnlyDataMatrix.getRowCount(); i++)
			{
				double[] featuresForInstance = featuresOnlyDataMatrix.getRow(i);
				double[] target = labelsOnlyDataMatrix.getRow(i);
				predictInstanceLabelsFromFeatures(featuresForInstance, predictedLabels);
				double delta = target[0] - predictedLabels[0];
				sse += (delta * delta);
			}
			return Math.sqrt(sse / featuresOnlyDataMatrix.getRowCount());
		}
		else
		{
			// The label is nominal, so measure predictive accuracy
			if(confusion != null)
			{
				confusion.setSize(labelValues, labelValues);
				for(int i = 0; i < labelValues; i++)
					confusion.setAttributeName(i, labelsOnlyDataMatrix.getAttributeValueName(0, i));
			}
			int correctCount = 0;
			for(int i = 0; i < featuresOnlyDataMatrix.getRowCount(); i++)
			{
				double[] featuresForInstance = featuresOnlyDataMatrix.getRow(i);
				int target = (int) labelsOnlyDataMatrix.getValueAt(i, 0);
				if(target >= labelValues)
					throw new Exception("The label is out of range");
				double[] predictedLabels = new double[1]; 
				predictInstanceLabelsFromFeatures(featuresForInstance, predictedLabels);
				int prediction = (int) predictedLabels[0];
				if(confusion != null)
					confusion.setValue(target, prediction, confusion.getValueAt(target, prediction) + 1);
				if(prediction == target)
					correctCount++;
			}
			return (double)correctCount / featuresOnlyDataMatrix.getRowCount();
		}
	}
}
