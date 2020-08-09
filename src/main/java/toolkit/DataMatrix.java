// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------
package toolkit;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Exception;

public class DataMatrix {
	// Data
	List< double[] > matrixData;

	// Meta-data
	List< String > attributeNamesByColIndex;
	List< TreeMap<String, Integer> > valueIndexByAttributeColAndValueName;
	List< TreeMap<Integer, String> > valueNameByAttributeColAndValueIndex;

	static double MISSING = Double.MAX_VALUE; // representation of missing values in the dataset

	// Creates a 0x0 matrix. You should call loadARFF or setSize next.
	public DataMatrix() {}

	// Copies the specified portion of otherMatrix to create a new matrix
	public DataMatrix(DataMatrix otherMatrix, int rowStart, int colStart, int rowCount, int colCount) {
		matrixData = new ArrayList< double[] >();
		for(int j = 0; j < rowCount; j++) {
			double[] rowSrc = otherMatrix.getRow(rowStart + j);
			double[] rowDest = new double[colCount];
			for(int i = 0; i < colCount; i++)
				rowDest[i] = rowSrc[colStart + i];
			matrixData.add(rowDest);
		}
		attributeNamesByColIndex = new ArrayList<String>();
		valueIndexByAttributeColAndValueName = new ArrayList< TreeMap<String, Integer> >();
		valueNameByAttributeColAndValueIndex = new ArrayList< TreeMap<Integer, String> >();
		for(int i = 0; i < colCount; i++) {
			attributeNamesByColIndex.add(otherMatrix.getAttributeNameAtColumn(colStart + i));
			valueIndexByAttributeColAndValueName.add(otherMatrix.valueIndexByAttributeColAndValueName.get(colStart + i));
			valueNameByAttributeColAndValueIndex.add(otherMatrix.valueNameByAttributeColAndValueIndex.get(colStart + i));
		}
	}

	// Adds a copy of the specified portion of otherMatrix to this matrix
	public void add(DataMatrix otherMatrix, int rowStart, int colStart, int rowCount) throws Exception {
		if(colStart + getColCount() > otherMatrix.getColCount())
			throw new Exception("out of range");
		for(int i = 0; i < getColCount(); i++) {
			if(otherMatrix.getValueCountForAttributeAtColumn(colStart + i) != getValueCountForAttributeAtColumn(i))
				throw new Exception("incompatible relations");
		}
		for(int j = 0; j < rowCount; j++) {
			double[] rowSrc = otherMatrix.getRow(rowStart + j);
			double[] rowDest = new double[getColCount()];
			for(int i = 0; i < getColCount(); i++)
				rowDest[i] = rowSrc[colStart + i];
			matrixData.add(rowDest);
		}
	}

	// Resizes this matrix (and sets all attributes to be continuous). This writes over any data currently in the matrix
	public void setSize(int rows, int cols) {
		matrixData = new ArrayList< double[] >();
		for(int j = 0; j < rows; j++) {
			double[] row = new double[cols];
			matrixData.add(row);
		}
		attributeNamesByColIndex = new ArrayList<String>();
		valueIndexByAttributeColAndValueName = new ArrayList< TreeMap<String, Integer> >();
		valueNameByAttributeColAndValueIndex = new ArrayList< TreeMap<Integer, String> >();
		for(int i = 0; i < cols; i++) {
			attributeNamesByColIndex.add("");
			valueIndexByAttributeColAndValueName.add(new TreeMap<String, Integer>());
			valueNameByAttributeColAndValueIndex.add(new TreeMap<Integer, String>());
		}
	}

	// Loads from an ARFF file
	public void loadArff(String filename) throws Exception, FileNotFoundException {
		matrixData = new ArrayList<double[]>();
		attributeNamesByColIndex = new ArrayList<String>();
		valueIndexByAttributeColAndValueName = new ArrayList< TreeMap<String, Integer> >();
		valueNameByAttributeColAndValueIndex = new ArrayList< TreeMap<Integer, String> >();
		boolean READDATA = false;
		Scanner fileScanner = new Scanner(new File(filename));
		while (fileScanner.hasNext()) {
			String line = fileScanner.nextLine().trim();
			if (line.length() > 0 && line.charAt(0) != '%') {
				if (!READDATA) {
					
					Scanner lineScanner = new Scanner(line);
					String firstToken = lineScanner.next().toUpperCase();

					switch (firstToken) {
						case "@RELATION":
							lineScanner.nextLine();
							break;
						case "@ATTRIBUTE":
							TreeMap<String, Integer> ste = new TreeMap<String, Integer>();
							valueIndexByAttributeColAndValueName.add(ste);
							TreeMap<Integer, String> ets = new TreeMap<Integer, String>();
							valueNameByAttributeColAndValueIndex.add(ets);

							Scanner secondaryLineScanner = new Scanner(line);
							if (line.contains("'")) secondaryLineScanner.useDelimiter("'");
							secondaryLineScanner.next();
							String attributeName = secondaryLineScanner.next();
							if (line.contains("'")) attributeName = "'" + attributeName + "'";
							attributeNamesByColIndex.add(attributeName);

							int vals = 0;
							String type = secondaryLineScanner.next().trim().toUpperCase();
							if (!type.equals("REAL") && !type.equals("CONTINUOUS") && !type.equals("INTEGER")) {
								try {
									String values = line.substring(line.indexOf("{") + 1, line.indexOf("}"));
									Scanner v = new Scanner(values);
									v.useDelimiter(",");
									while (v.hasNext()) {
										String value = v.next().trim();
										if (value.length() > 0) {
											ste.put(value, vals);
											ets.put(vals, value);
											vals++;
										}
									}
									v.close();
								} catch (Exception e) {
									secondaryLineScanner.close();
									fileScanner.close();
									throw new Exception("Error parsing line: " + line + "\n" + e.toString());
								}
							}
							secondaryLineScanner.close();
							break;
						case "@DATA":
							READDATA = true;
							break;
					}

					lineScanner.close();
				}
				else {
					double[] newRow = new double[getColCount()];
					int curPos = 0;

					try {
						Scanner t = new Scanner(line);
						t.useDelimiter(",");
						while (t.hasNext()) {
							String textValue = t.next().trim();

							if (textValue.length() > 0) {
								double doubleValue;
								int vals = valueNameByAttributeColAndValueIndex.get(curPos).size();
								
								//Missing instances appear in the dataset as a double defined as MISSING
								if (textValue.equals("?")) {
									doubleValue = MISSING;
								}
								// Continuous values appear in the instance vector as they are
								else if (vals == 0) {
									doubleValue = Double.parseDouble(textValue);
								}
								// Discrete values appear as an index to the "name" 
								// of that value in the "attributeValue" structure
								else {
									doubleValue = valueIndexByAttributeColAndValueName.get(curPos).get(textValue);
									if (doubleValue == -1) {
										throw new Exception("Error parsing the value '" + textValue + "' on line: " + line);
									}
								}
								
								newRow[curPos] = doubleValue;
								curPos++;
							}
						}
						t.close();
					}
					catch(Exception e) {
						fileScanner.close();
						throw new Exception("Error parsing line: " + line + "\n" + e.toString());
					}
					matrixData.add(newRow);
				}
			}
		}
		fileScanner.close();
	}

	// Returns the number of rows in the matrix
	int getRowCount() { return matrixData.size(); }

	// Returns the number of columns (or attributes) in the matrix
	int getColCount() { return attributeNamesByColIndex.size(); }

	// Returns the specified row
	double[] getRow(int r) { return matrixData.get(r); }

	// Returns the element at the specified row and column
	double getValueAt(int row, int col) { return matrixData.get(row)[col]; }

	// Sets the value at the specified row and column
	void setValue(int row, int col, double newValue) { getRow(row)[col] = newValue; }

	// Returns the name of the specified attribute
	String getAttributeNameAtColumn(int col) { return attributeNamesByColIndex.get(col); }

	// Set the name of the specified attribute
	void setAttributeName(int col, String newName) { attributeNamesByColIndex.set(col, newName); }

	// Returns the name of the specified value
	String getAttributeValueName(int attributeIndex, int valueIndex) { return valueNameByAttributeColAndValueIndex.get(attributeIndex).get(valueIndex); }

	// Returns the number of values associated with the specified attribute (or column)
	// 0=continuous, 2=binary, 3=trinary, etc.
	int getValueCountForAttributeAtColumn(int col) { return valueNameByAttributeColAndValueIndex.get(col).size(); }
	
	// Returns true if the attribute at column is continuous
	boolean isAttributeAtColumnContinuous(int col) { return getValueCountForAttributeAtColumn(col) == 0; }

	// Shuffles the row order
	void shuffleRowOrder(Random rand) {
		for(int n = getRowCount(); n > 0; n--) {
			int i = rand.nextInt(n);
			double[] tmp = getRow(n - 1);
			matrixData.set(n - 1, getRow(i));
			matrixData.set(i, tmp);
		}
	}

	// Shuffles the row order with a buddy matrix 
	void shuffleRowOrderWithBuddyMatrix(Random rand, DataMatrix buddy) {
		for (int n = getRowCount(); n > 0; n--) {
			int i = rand.nextInt(n);
			double[] tmp = getRow(n - 1);
			matrixData.set(n - 1, getRow(i));
			matrixData.set(i, tmp);

			double[] tmp1 = buddy.getRow(n - 1);
			buddy.matrixData.set(n - 1, buddy.getRow(i));
			buddy.matrixData.set(i, tmp1);
		}
	}

	// Returns the mean of the specified column
	double getColumnMean(int col) {
		double sum = 0;
		int count = 0;
		for(int i = 0; i < getRowCount(); i++) {
			double v = getValueAt(i, col);
			if(v != MISSING) {
				sum += v;
				count++;
			}
		}
		return sum / count;
	}

	// Returns the min value in the specified column
	double getColumnMin(int col) {
		double m = MISSING;
		for(int i = 0; i < getRowCount(); i++) {
			double v = getValueAt(i, col);
			if(v != MISSING)
			{
				if(m == MISSING || v < m)
					m = v;
			}
		}
		return m;
	}

	// Returns the max value in the specified column
	double getColumnMax(int col) {
		double m = MISSING;
		for(int i = 0; i < getRowCount(); i++) {
			double v = getValueAt(i, col);
			if(v != MISSING)
			{
				if(m == MISSING || v > m)
					m = v;
			}
		}
		return m;
	}

	// Returns the most common value in the specified column
	double getMostCommonValueForColumn(int col) {
		TreeMap<Double, Integer> tm = new TreeMap<Double, Integer>();
		for(int i = 0; i < getRowCount(); i++) {
			double v = getValueAt(i, col);
			if(v != MISSING) {
				Integer count = tm.get(v);
				if(count == null)
					tm.put(v, 1);
				else
					tm.put(v, count.intValue() + 1);
			}
		}
		int maxCount = 0;
		double val = MISSING;
		Iterator< Entry<Double, Integer> > it = tm.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Double, Integer> e = it.next();
			if(e.getValue() > maxCount) {
				maxCount = e.getValue();
				val = e.getKey();
			}
		}
		return val;
	}

	double[][] normalize() {
		double[][] normalizationRanges = new double[getColCount()][];
		for(int i = 0; i < getColCount(); i++) {
			if(getValueCountForAttributeAtColumn(i) == 0) {
				double min = getColumnMin(i);
				double max = getColumnMax(i);
				normalizationRanges[i] = new double[] {min, max};
				for(int j = 0; j < getRowCount(); j++) {
					double v = getValueAt(j, i);
					if(v != MISSING)
						setValue(j, i, (v - min) / (max - min));
				}
			}
		}
		return normalizationRanges;
	}
	
	void normalize(double[][] normalizationRanges) {
		for(int i = 0; i < getColCount(); i++) {
			if(getValueCountForAttributeAtColumn(i) == 0) {
				double min = normalizationRanges[i][0];
				double max = normalizationRanges[i][1];
				for(int j = 0; j < getRowCount(); j++) {
					double v = getValueAt(j, i);
					if(v != MISSING)
						setValue(j, i, (v - min) / (max - min));
				}
			}
		}
	}

	void print() {
		System.out.println("@RELATION Untitled");
		for(int i = 0; i < attributeNamesByColIndex.size(); i++) {
			System.out.print("@ATTRIBUTE " + attributeNamesByColIndex.get(i));
			int vals = getValueCountForAttributeAtColumn(i);
			if(vals == 0)
				System.out.println(" CONTINUOUS");
			else
			{
				System.out.print(" {");
				for(int j = 0; j < vals; j++) {
					if(j > 0)
						System.out.print(", ");
					System.out.print(valueNameByAttributeColAndValueIndex.get(i).get(j));
				}
				System.out.println("}");
			}
		}
		System.out.println("@DATA");
		for(int i = 0; i < getRowCount(); i++) {
			double[] r = getRow(i);
			for(int j = 0; j < r.length; j++) {
				if(j > 0)
					System.out.print(", ");
				if(getValueCountForAttributeAtColumn(j) == 0)
					System.out.print(r[j]);
				else
					System.out.print(valueNameByAttributeColAndValueIndex.get(j).get((int)r[j]));
			}
			System.out.println();
		}
	}
}
