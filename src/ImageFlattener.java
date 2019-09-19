import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

import javax.imageio.ImageIO;

/**
 * Given a directory containing a set of images, this class resizes the images
 * to a specified dimension then flattens them to nx1 vector of of averaged RGB
 * values and adds each of these vectors to a array with number of rows equal to
 * the number of images in the given directory.
 * 
 * @author Erik Tiedt
 *
 */
public class ImageFlattener {

	private Double[][] flatImages;
	private String[] filesList;
	private String directoryPath;

	public ImageFlattener(String directoryPath) {
		this.flatImages = new Double[new File(directoryPath).list().length][];
		this.filesList = new File(directoryPath).list();
		this.directoryPath = directoryPath;
	}

	private static void resizeImages(String directoryPath, int width, int height) {

		String[] filesList = new File(directoryPath).list();
		File dir = new File(directoryPath);
		File newDir = new File(dir + File.separator + "resize");
		newDir.mkdirs();
		File output;

		BufferedImage resImage;
		Image temp;

		for (String file : filesList) {
			try {
				BufferedImage image = ImageIO.read(new File(directoryPath + File.separator + file));
				temp = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
				resImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

				Graphics2D imToBuf = resImage.createGraphics();
				imToBuf.drawImage(temp, 0, 0, null);
				imToBuf.dispose();

				output = new File(newDir + "/" + file);
				ImageIO.write(resImage, "jpg", output);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		System.out.println("Images resized and saved to: " + newDir.getAbsolutePath());
	}

	private static Double[] flattenImage(BufferedImage image) {

		int col = image.getWidth();
		int row = image.getHeight();
		int index = 0;
		Color color;
		int r, g, b;

		Double[] flatImage = new Double[col * row];

		for (int y = 0; y < row; ++y) {
			for (int x = 0; x < col; ++x) {
				color = new Color(image.getRGB(x, y));
				r = color.getRed();
				g = color.getGreen();
				b = color.getBlue();
				flatImage[index] = (((r + g + b) / 3.0));
				++index;
			}
		}

		return flatImage;

	}

	public void makeFlat() {
		int instance = 0;
		for (String file : this.filesList) {
			try {
				BufferedImage image = ImageIO.read(new File(this.directoryPath + File.separator + file));
				flatImages[instance] = flattenImage(image);
			} catch (IOException e) {
			}
			++instance;
		}
	}

	public void export(ImageFlattener flattener, String path) {
		File output = new File(path + File.separator + "flattened.csv");
		if (!output.exists()) {
			try {
				output.createNewFile();
			} catch (IOException e) {
				System.out.println("Could not create 'flattened.csv' \n" + e.getMessage());
			}
		}

		try {
			BufferedWriter writer = Files.newBufferedWriter(output.toPath());
			for (int i = 0; i < flattener.flatImages.length; ++i) {
				for (int j = 0; j < flattener.flatImages[i].length; ++j) {
					writer.write(flatImages[i][j].toString());
					if (j != flattener.flatImages[i].length - 1) {
						writer.write(",");
					}
				}
				if (i != flattener.flatImages.length - 1) {
					writer.write("\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String path;
		int width, height;
		String temp;

		System.out.println("Welcome to Image Resizer And Flattener \nWritten by: Erik Tiedt");
		System.out.print("Enter the absolute path to the directory containing your images: ");
		path = in.nextLine().trim();
		System.out.print("Enter desired width: ");
		width = in.nextInt();
		System.out.print("Enter desired height: ");
		height = in.nextInt();
		System.out.println("Resizing...");
		resizeImages(path, width, height);
		System.out.print("Flatten images and export to text file? \n'Y' for yes, any other input to quit. ");
		temp = in.next().substring(0, 1);

		if (temp.toLowerCase().equals("y")) {
			System.out.println("Exporting...");
			ImageFlattener flattener = new ImageFlattener(path + File.separator + "resize");
			flattener.makeFlat();
			flattener.export(flattener, path);
			System.out.println("Exporting Finished\nGoodbye.");
		} else {
			System.out.println("Goodbye");
		}

		in.close();
	}
}