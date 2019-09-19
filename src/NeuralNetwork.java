import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class NeuralNetwork {

	private static final String DIR_PATH = "./images";
	private static final String trainPath = "./train.csv";
	private static final String testPath = "./test.csv";
	private static final Double ALPHA = 0.01;
	private static final int MAX_EPOCHS = 0;
	private static final double EPSILON = 0.001;
	static int height = 25;
	static int width = 25;
	static Random rand = new Random();
	static int size = height * width;

	// init weight, bias, a
	static Double prev_c = 0.0;
	static Double curr_c = 0.0;

	static Double[][] w1 = new Double[size][size];
	static Double[] w2 = new Double[size];
	static Double[] b1 = new Double[size];
	static Double b2 = rand.nextDouble() - rand.nextDouble();
	static Double[] a1 = new Double[size];
	static Double a2;

	public static List<List<Double>> readCSV(String filePath) {
		List<List<Double>> result = new ArrayList<>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		try {
			br = new BufferedReader(new FileReader(filePath));
			while ((line = br.readLine()) != null) {
				String[] parseByLine = line.split(cvsSplitBy);
				Double[] doubleByLine = new Double[parseByLine.length];
				doubleByLine[0] = Double.parseDouble(parseByLine[0]);
				for (int i = 1; i < parseByLine.length; i++) {
					doubleByLine[i] = Double.parseDouble(parseByLine[i]);
				}
				result.add(Arrays.asList(doubleByLine));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void propagation(int index, List<List<Double>> list) {
		for (int i = 0; i < size; i++) {
			double sum_wx = 0;
			for (int j = 1; j < size; j++) {
				sum_wx += w1[i][j - 1] * list.get(index).get(j);
			}
			a1[i] = 1 / (1 + Math.exp(-(sum_wx + b1[i])));
		}
		double sum_aw = 0;
		for (int j = 0; j < size; j++) {
			sum_aw += a1[j] * w2[j];
		}
		a2 = 1 / (1 + Math.exp(-(sum_aw + b2)));
	}

	public static void main(String[] args) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter("./output.txt"));

		List<List<Double>> train = new ArrayList<List<Double>>();
		List<List<Double>> test = new ArrayList<List<Double>>();

		train = readCSV(trainPath);
		test = readCSV(testPath);

		for (int i = 0; i < size; i++) {
			w2[i] = rand.nextDouble() - rand.nextDouble();
			b1[i] = rand.nextDouble() - rand.nextDouble();
		}
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < w1.length; j++) {
				w1[i][j] = rand.nextDouble() - rand.nextDouble();
			}
		}

		for (int e = 1;; e++) { // epoch
			rand = new Random();
			for (int itr = 0; itr < train.size(); itr++) {
				int randIndex = rand.nextInt(train.size());
				// propagation
				propagation(randIndex, train);

				// Back propagation
				Double dc_db2 = (a2 - train.get(randIndex).get(0)) * (1 - a2) * a2;

				Double[] dc_dw2j = new Double[size];

				for (int j = 0; j < dc_dw2j.length; j++) {
					dc_dw2j[j] = (a2 - train.get(randIndex).get(0)) * a2 * (1 - a2) * a1[j];
				}
				Double[] dc_db1j = new Double[size];
				for (int j = 0; j < dc_db1j.length; j++) {
					dc_db1j[j] = (a2 - train.get(randIndex).get(0)) * a2 * (1 - a2) * w2[j] * a1[j] * (1 - a1[j]);
				}
				Double[][] dc_dw1j = new Double[size][size];
				for (int j = 0; j < size; j++) {
					for (int j_prime = 0; j_prime < size; j_prime++) {
						dc_dw1j[j][j_prime] = (a2 - train.get(randIndex).get(0)) * a2 * (1 - a2) * w2[j] * a1[j]
								* (1 - a1[j]) * train.get(randIndex).get(j_prime + 1);
					}
				}

				// Update weights & biases
				for (int j = 0; j < size; j++) {
					w2[j] -= ALPHA * dc_dw2j[j];
					b1[j] -= ALPHA * dc_db1j[j];
					for (int j_prime = 0; j_prime < size; j_prime++) {
						w1[j][j_prime] -= ALPHA * dc_dw1j[j][j_prime];
					}
				}
				b2 -= ALPHA * dc_db2;
			}

			prev_c = curr_c;
			curr_c = 0.0;
			for (int i = 0; i < train.size(); i++) {
				propagation(i, train);
				curr_c += Math.pow((train.get(i).get(0) - a2), 2);
			}
			curr_c = 0.5 * curr_c;
			for (int i = 0; i < test.size(); i++) {
				propagation(i, test);
				if (a2 >= 0.5 && test.get(i).get(0) == 1.0) {
					System.out.println("correct answer: 1 " + "actual answer: " + a2);
					writer.write("1");
					writer.newLine();
				} else if (a2 < 0.5 && test.get(i).get(0) == 0.0) {
					System.out.println("correct answer: 0 " + "actual answer: " + a2);
					writer.write("0");
					writer.newLine();
				}
			}

			if (Math.abs(curr_c - prev_c) < EPSILON)
				break;
			else if (e >= MAX_EPOCHS) { // halt program
				System.out.println("training done");
				break;
			}
		}
		System.out.println("end of program");
		writer.flush();
		writer.close();
	}

}
