//V SenClair
//B01104863
//K Means Clustering
//Output is written to file and output to console

package KMeansAlgo;

import java.io.*;
import java.util.*;

public class KMeans
{
	ArrayList<ArrayList<Double>> data;
	ArrayList<ArrayList<Double>> centroids;
	ArrayList<Integer> usedCentroidIndices;
	ArrayList<Integer> clusterAssignment;
	ArrayList<Double> sum;
	double numPoints;
	int numDims;
	String line1;
	BufferedReader reader;
	BufferedWriter writer;
	double SSE, initialSSE;
	double oldSSE;
	int cluster;
	double minDist = 0;
	double dist, min, max;
	int ind;
	int iter;
	double totalSSE;
	double rand, jaccard;
	ArrayList<Integer> trueClusterAssignment;
	int K;
	double truePos, trueNeg, falsePos, falseNeg;
	//	ArrayList<Double> dataCenter;
	//	double CHindex;
	//	double sil;

	//Variables used for initialization of data
	String st;
	String [] p;
	ArrayList<Double> point;

	public KMeans(String filename, int I, double T, int R) throws IOException
	{
		data = new ArrayList<ArrayList<Double>>();
		clusterAssignment = new ArrayList<Integer>();
		trueClusterAssignment = new ArrayList<Integer>();

		//Open files
		reader = new BufferedReader(new FileReader(filename));
		writer = new BufferedWriter(new FileWriter(filename + "SELECTIONoutput.txt"));
		//		writer = new BufferedWriter(new FileWriter(filename + "outputKof" + K + ".txt"));

		//Set the number of points and dimensions
		line1 = reader.readLine();
//		line1.trim().replaceAll("  ", "");
		String[] line = line1.split(" ");
		numPoints = Integer.parseInt(line[0]);
		numDims = Integer.parseInt(line[1]) - 1;
		K = Integer.parseInt(line[2]);

		//Store data
		while((st = reader.readLine()) != null)
		{
//			st.trim().replaceAll("  ", "");
			p = st.split(" ");
			point = new ArrayList<Double>();
			for(int i = 0; i < p.length; i++)
			{
				if(i == p.length - 1)
					trueClusterAssignment.add(Integer.parseInt(p[i]));

				else
					point.add(Double.parseDouble(p[i]));
			}
			data.add(point);

			//Make sure assignment array is same length as dataset
			clusterAssignment.add(0);
		}

		//Finish with input file
		reader.close();

		//Normalize data with minmax normalization
		for(int i = 0; i < numDims; i++)
		{
			min = data.get(0).get(i);
			max = data.get(0).get(i);

			for(int j = 0; j < numPoints; j++)
			{
				if(min > data.get(j).get(i))
					min = data.get(j).get(i);

				if(max < data.get(j).get(i))
					max = data.get(j).get(i);
			}

			for(int j = 0; j < numPoints; j++)
			{
				if(max-min == 0)
				{}

				else
				{
					double norm = (data.get(j).get(i) - min)/(max-min);
					data.get(j).set(i, norm);
				}
			}
		}

		//		dataCenter = new ArrayList<Double>();
		//		while(dataCenter.size() < numDims)
		//			dataCenter.add(0.0);

		//		//Calculate total sum of squared error of data
		//		for(int i = 0; i < numPoints; i++)
		//		{
		//			for(int j = 0; j < numDims; j++)
		//			{
		//				dataCenter.set(j, dataCenter.get(j) + data.get(i).get(j));
		//			}
		//		}
		//
		//		for(int i = 0; i < numDims; i++)
		//		{
		//			dataCenter.set(i, dataCenter.get(i)/numPoints);
		//		}
		//
		//		for(int i = 0; i < numPoints; i++)
		//		{
		//			dist = 0;
		//
		//			for(int k = 0; k < numDims; k++)
		//			{
		//				dist += (data.get(i).get(k) - dataCenter.get(k)) * (data.get(i).get(k) - dataCenter.get(k));
		//			}
		//
		//			totalSSE += dist;
		//		}

		//Clear output file and start new
		writer.write("Testing: " + filename + " " + K + " " + I + " " + T + " " + R + "\n");
		//		System.out.println("Testing: " + filename + " " + K + " " + I + " " + T + " " + R + "");
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Double> Run(String filename, int I, double T, int runNum) throws IOException
	{
		writer.append("Run " + runNum + "\n");
		System.out.println("Run " + runNum + "");

		//These arraylists must be new every run or else
		centroids = new ArrayList<ArrayList<Double>>();
		usedCentroidIndices = new ArrayList<Integer>();

		//		Initialize Centroids with random selection
		for(int i = 0; i < K; i++)
		{
			do
			{
				ind = (int)(Math.random()*((data.size())));
			}while(usedCentroidIndices.contains(ind));

			usedCentroidIndices.add(ind);

			centroids.add((ArrayList<Double>) data.get(ind).clone());
		}

		//		//Initialize Centroids with random partitioning
		//		for(int i = 0; i < numPoints; i++)
		//			clusterAssignment.set(i, (int)(Math.random()*K));
		//
		//		while(centroids.size() < K)
		//			centroids.add(new ArrayList<Double>(Collections.nCopies(numDims, 0.0)));
		//
		//		for(int j = 0; j < K; j++)
		//		{
		//			sum = new ArrayList<Double>(Collections.nCopies(numDims, 0.0));
		//
		//			for(int k = 0; k < numDims; k++)
		//			{
		//				if(Collections.frequency(clusterAssignment, j) != 0)
		//				{
		//					for(int i = 0; i < numPoints; i++)
		//					{
		//						if(clusterAssignment.get(i) == j)
		//						{
		//							sum.set(k, sum.get(k)+data.get(i).get(k));
		//						}
		//					}
		//
		//					centroids.get(j).set(k, sum.get(k)/Collections.frequency(clusterAssignment, j));
		//				}
		//			}
		//			sum = null;
		//		}

		//Calculate initial SSE of clusters
//		initialSSE = 0;
//
//		for(int i = 0; i < numPoints; i++)
//		{
//			for(int j = 0; j < K; j++)
//			{
//				dist = 0;
//
//				if(clusterAssignment.get(i) == j)
//				{
//					for(int k = 0; k < numDims; k++)
//					{
//						dist += (data.get(i).get(k) - centroids.get(j).get(k)) * (data.get(i).get(k) - centroids.get(j).get(k));
//					}
//				}
//
//				initialSSE += dist;
//			}
//		}
//
//		writer.append("Initial SSE: " + initialSSE + "\n");
//		System.out.println("Initial SSE: " + initialSSE);

		iter = 1;
		SSE = Double.MAX_VALUE / 2;
		oldSSE = Double.MAX_VALUE;

		//Run iterations until max number is reached or converges
		while(iter <= I && (oldSSE - SSE)/oldSSE >= T)
		{
			oldSSE = SSE;
			SSE = 0;

			//Assign points to clusters
			for(int i = 0; i < numPoints; i++)
			{
				for(int j = 0; j < centroids.size(); j++)
				{
					dist = 0;
					for(int k = 0; k < numDims; k++)
					{
						dist += (data.get(i).get(k) - centroids.get(j).get(k)) * (data.get(i).get(k) - centroids.get(j).get(k));
					}

					if(j == 0)
					{
						minDist = dist;
						cluster = 0;
					}

					else if(dist < minDist)
					{
						minDist = dist;
						cluster = j;
					}
				}

				clusterAssignment.set(i, cluster);
			}

			//Calculate new centroids
			for(int j = 0; j < centroids.size(); j++)
			{
				sum = new ArrayList<Double>();

				//Make the sum array the right size
				while(sum.size() < numDims)
					sum.add(0.0);

				for(int k = 0; k < numDims; k++)
				{
					if(Collections.frequency(clusterAssignment, j) != 0)
					{
						for(int i = 0; i < numPoints; i++)
						{
							if(clusterAssignment.get(i) == j)
							{
								sum.set(k, sum.get(k)+data.get(i).get(k));
							}
						}

						centroids.get(j).set(k, sum.get(k)/Collections.frequency(clusterAssignment, j));
					}
				}
				sum = null;
			}

			//Calculate SSE
			for(int i = 0; i < numPoints; i++)
			{
				for(int j = 0; j < centroids.size(); j++)
				{
					dist = 0;

					if(clusterAssignment.get(i) == j)
					{
						for(int k = 0; k < numDims; k++)
						{
							dist += (data.get(i).get(k) - centroids.get(j).get(k)) * (data.get(i).get(k) - centroids.get(j).get(k));
						}
					}

					SSE += dist;
				}
			}

			writer.append("Iteration " + iter + ": SSE = " + SSE + "\n");
			System.out.println("Iteration " + iter + ": SSE = " + SSE);

			iter++;
		}

		//		//CH index calculation
		//		//CH = (SSE between / SSE within)(n-k)/(k-1) = (total SSE - SSE within)/SSE within * (n-k)/(k-1) = (totalSSE/SSE within - 1) * (n-k)/(k-1)
		//		CHindex = (totalSSE/SSE - 1)*(numPoints - K)/(K-1);
		//		writer.append("Run " + runNum + " CH Index = " + CHindex + "\n");
		////		System.out.println("Run " + runNum + "CH Index = " + CHindex);

		//		//Silhouette width calculation
		//		sil = 0;
		//		
		//		for(int i = 0; i < numPoints; i++)
		//		{
		//			int numClus = Collections.frequency(clusterAssignment, clusterAssignment.get(i));
		//
		//			if(numClus == 1)
		//			{
		//				sil += 0;
		//			}
		//			else
		//			{
		//				double a=0, b=Double.MAX_VALUE;
		//				
		//				for(int j = 0; j < K; j++)
		//				{
		//					dist = 0;
		//
		//					if(clusterAssignment.get(i) == j)
		//					{
		//						for(int k = 0; k < numDims; k++)
		//						{
		//							dist += (data.get(i).get(k) - centroids.get(j).get(k)) * (data.get(i).get(k) - centroids.get(j).get(k));
		//						}
		//						a = dist/(numClus - 1);
		//					}
		//					else
		//					{
		//						for(int k = 0; k < numDims; k++)
		//						{
		//							dist += (data.get(i).get(k) - centroids.get(j).get(k)) * (data.get(i).get(k) - centroids.get(j).get(k));
		//						}
		//						
		//						if(dist < b)
		//							b = dist/Collections.frequency(clusterAssignment, j);
		//					}
		//				}
		//				
		//				if(a > b)
		//					sil += b/a - 1;
		//				else if(a < b)
		//					sil += 1 - a/b;
		//				else
		//					sil += 0;
		//			}
		//		}

		//		sil = sil / numPoints;
		//		writer.append("Run " + runNum + " Silhouette Index = " + sil + "\n");
		//		System.out.println("Run " + runNum + "Silhouette Index = " + sil);
		
		//Calculate pairwise values
		truePos = 0;
		trueNeg = 0;
		falsePos = 0;
		falseNeg = 0;
		rand = 0;
		jaccard = 0;
		
		for(int i = 0; i < numPoints; i++)
		{
			for(int j = i + 1; j < numPoints; j++)
			{
				if(clusterAssignment.get(i) == clusterAssignment.get(j) && trueClusterAssignment.get(i) == trueClusterAssignment.get(j))
					truePos++;
				
				else if(clusterAssignment.get(i) != clusterAssignment.get(j) && trueClusterAssignment.get(i) != trueClusterAssignment.get(j))
					trueNeg++;
				
				else if(clusterAssignment.get(i) == clusterAssignment.get(j) && trueClusterAssignment.get(i) != trueClusterAssignment.get(j))
					falsePos++;
				
				else
					falseNeg++;
			}
		}
		
		//Calculate Rand Index
		rand = (truePos + trueNeg) / ((numPoints * (numPoints - 1)) / 2);
		
		//Calculate Jaccard Index
		jaccard = truePos/(truePos + falsePos + falseNeg);

		ArrayList<Double> output = new ArrayList<Double>();
		output.add(SSE);
		//		output.add(CHindex);
		//		output.add(sil);
		output.add(rand);
		output.add(jaccard);

		return output;
	}

	//Function to make sure everything can be written to file before program finishes
	public void finish(double bestSSE, double bestRand, double bestJaccard, int bestSSERun, int bestRandRun, int bestJaccardRun) throws IOException
	{
		writer.append("Best SSE Run: " + bestSSERun + " at SSE = " + bestSSE + "\n");
		writer.append("Best Rand Run: " + bestRandRun + " at Rand index = " + bestRand + "\n");
		writer.append("Best Jaccard Run: " + bestJaccardRun + " at Jaccard index = " + bestJaccard);
		System.out.println("Best SSE Run: " + bestSSERun + " at SSE = " + bestSSE);
		System.out.println("Best Rand Run: " + bestRandRun + " at Rand index = " + bestRand);
		System.out.println("Best Jaccard Run: " + bestJaccardRun + " at Jaccard index = " + bestJaccard);
		writer.close();
	}

	public static void main(String[] args) throws NumberFormatException, IOException
	{
		
		double bestSSE = Double.MAX_VALUE;
//		double bestCH=0, bestSil=0;
		double bestRand=0;
		double bestJaccard=0;
		int bestSSERun = 0;
		int bestRandRun = 0;
		int bestJaccardRun = 0;
		KMeans KM = new KMeans(args[0], Integer.parseInt(args[1]), Double.parseDouble(args[2]), Integer.parseInt(args[3]));

		for(int i = 0; i < Integer.parseInt(args[3]); i++)
		{
			ArrayList<Double> output = KM.Run(args[0], Integer.parseInt(args[1]), Double.parseDouble(args[2]), i+1);

			if(output.get(0) < bestSSE)
			{
				bestSSERun = i+1;
				bestSSE = output.get(0);
			}
			if(output.get(1) > bestRand)
			{
				bestRandRun = i+1;
				bestRand = output.get(1);
			}
			if(output.get(2) > bestJaccard)
			{
				bestJaccardRun = i+1;
				bestJaccard = output.get(2);
			}
		}

		KM.finish(bestSSE, bestRand, bestJaccard, bestSSERun, bestRandRun, bestJaccardRun);

	}
}
