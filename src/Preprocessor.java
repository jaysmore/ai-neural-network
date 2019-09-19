import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class Preprocessor {
	static int height;
	static int width;

	public Preprocessor(int height, int width) {
		Preprocessor.height = height;
		Preprocessor.width = width;
	}

	/* Resize an image and write to the provided write_to_path path */
	public void write_to_resize(String img_file_path, String write_to_path) throws IOException {
		File input = new File(img_file_path);
		BufferedImage image = ImageIO.read(input);
		BufferedImage resized = resize(image, height, width);
		File output = new File(write_to_path);
		ImageIO.write(resized, "png", output);
	}

	/*
	 * Convert img files in a directory_path to a csv file with y value at the
	 * beginning of line
	 */
	public void convert_to_cvs(String directory_path, String write_to_path) throws IOException {
		File dir = new File(directory_path);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			List<List<String>> records = new ArrayList<>();
			for (File child : directoryListing) {
				List<String> rec = resize_then_parse(child);
				if (rec != null)
					records.add(rec);
			}
			// Write to csv
			FileWriter csvWriter = new FileWriter(write_to_path);
			for (List<String> rowData : records) {
				csvWriter.append(String.join(",", rowData));
				csvWriter.append("\n");
			}
			csvWriter.flush();
			csvWriter.close();
		} else {
			System.out.println("Error: Is provided path a directory?");
			return;
		}
	}

	/* Resize an image then parse RGB. Return the average of R, G and B. */
	private static List<String> resize_then_parse(File input) throws IOException {
		BufferedImage image = ImageIO.read(input);
		BufferedImage resized = resize(image, height, width);

		List<String> record = new ArrayList<>();
		String name = input.getName();
		// Naively validate label of img file
		if (name.contains("a.jpg"))
			record.add("0");
		else if (name.contains("b.jpg"))
			record.add("1");
		else
			return null;

		// Extract R, G and B values then average them
		for (int y = 0; y < Preprocessor.height; y++) {
			for (int x = 0; x < Preprocessor.width; x++) {
				int p = resized.getRGB(x, y);
				int r = (p >> 16) & 0xff; // R
				int g = (p >> 8) & 0xff; // G
				int b = p & 0xff; // B
				Double avg = (r + g + b) / 3.0;
				record.add(avg.toString());
			}
		}
		return record;
	}

	private static BufferedImage resize(BufferedImage img, int height, int width) {
		Image tmp = null;
		if (img != null)
			tmp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = resized.createGraphics();
		g2d.drawImage(tmp, 0, 0, null);
		g2d.dispose();
		return resized;
	}
}
