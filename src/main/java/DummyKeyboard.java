import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.minMaxLoc;
import static org.bytedeco.javacpp.opencv_highgui.destroyAllWindows;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.TM_CCORR_NORMED;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.matchTemplate;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class DummyKeyboard implements NativeKeyListener {
	private static Robot bot = null;
	private int mask = InputEvent.BUTTON1_DOWN_MASK;
	private int mouseX = 0;
	private int mouseY = 0;

	private boolean Pause = false;

	// Get the logger for "org.jnativehook" and set the level to off.
	private final static Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());

	Mat target = imread(System.getProperty("user.dir") + "/speaker.jpg", CV_LOAD_IMAGE_GRAYSCALE);

	public void nativeKeyPressed(NativeKeyEvent e) {
		if (e.getKeyCode() == 63) {
			Pause = !Pause;
		}
		if (Pause) {
			return;
		}
		
		// System.out.println("Key Pressed: " +
		// NativeKeyEvent.getKeyText(e.getKeyCode()));
		//
		// System.out.println("mouseX" + mouseX);
		// System.out.println("mouseY" + mouseY);

		switch (e.getKeyCode()) {
		case 32:
		case 57416:
			System.out.println("UP");
			PointerInfo a = MouseInfo.getPointerInfo();
			java.awt.Point b = a.getLocation();
			mouseX = (int) b.getX();
			mouseY = (int) b.getY();
			System.out.println("mouseX: " + mouseX + " === mouseY: " + mouseY);
			bot.mouseMove(mouseX, mouseY);
			bot.mousePress(mask);
			bot.mouseRelease(mask);
			break;
		// case 57419:
		case 44:
		case 18: // e
			System.out.println("LEFT");
			bot.mouseMove(mouseX - 90, mouseY + 140);
			// mouseX
			bot.mousePress(mask);
			bot.mouseRelease(mask);
			bot.mouseMove(mouseX, mouseY);
			break;
		// case 57421:
		case 46:
		case 33: // f
			System.out.println("RIGHT");
			bot.mouseMove(mouseX + 90, mouseY + 140);
			bot.mousePress(mask);
			bot.mouseRelease(mask);
			bot.mouseMove(mouseX, mouseY);
			break;
		case 45:
		case 19: // r
		case 57424:
			System.out.println("DOWN");

			Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			BufferedImage screenFullImage = bot.createScreenCapture(screenRect);
			// In previous versions I save screenFullImage
			// to a .png file on disk
			// and then pass the filename to findTarget() and findTarget will
			// read the file to process.
			// This write/read operation is expensive.
			// Now use the following line to covert screenFullImage
			// (BufferedImage) to mat (Mat) directly
			// and then pass it to findTarget()
			// Learn this method from
			// https://github.com/bytedeco/javacv/blob/master/samples/BioInspiredRetina.java
			Mat mat = new OpenCVFrameConverter.ToMat().convert(new Java2DFrameConverter().convert(screenFullImage));

			Point speaker = findTarget(mat, target);
			// System.out.println("newStyle() called...");
			// System.out.println("X: " + target.x() + " === Y: " +
			// target.y());
			bot.mouseMove(speaker.x() + 10, speaker.y() + 20);
			bot.mousePress(mask);
			bot.mouseRelease(mask);
			// System.out.println("DOWN clicked...");
			bot.mouseMove(mouseX, mouseY);

		default:
			// System.out.println("KeyCode: " + e.getKeyCode());

		}

	}

	public void nativeKeyReleased(NativeKeyEvent e) {
		System.out.println("Key Released: " + NativeKeyEvent.getKeyText(e.getKeyCode()));
		System.out.println("KeyCode: " + e.getKeyCode());
	}

	public void nativeKeyTyped(NativeKeyEvent e) {
		// System.out.println("Key Typed: " + e.getKeyText(e.getKeyCode()));
	}

	public static void main(String[] args) {
		// Turn off logging
		logger.setLevel(Level.OFF);

		// logger.setLevel(Level.WARNING);

		try {
			bot = new Robot();
			bot.setAutoDelay(100);
			bot.setAutoWaitForIdle(true);
		} catch (Exception failed) {
			System.err.println("Failed instantiating Robot: " + failed);
		}

		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}

		GlobalScreen.addNativeKeyListener(new DummyKeyboard());
	}

	public static Point findTarget(Mat sourceColor, Mat target) {
		// read in image default colors
		// This call is extremely slow when running in commandline with the
		// exported .jar file

		Mat sourceGrey = new Mat(sourceColor.size(), CV_8UC1);
		cvtColor(sourceColor, sourceGrey, COLOR_BGR2GRAY);
		// load in template in grey
		Mat template = target;
		// Size for the result image
		Size size = new Size(sourceGrey.cols() - template.cols() + 1, sourceGrey.rows() - template.rows() + 1);
		Mat result = new Mat(size, CV_32FC1);
		matchTemplate(sourceGrey, template, result, TM_CCORR_NORMED);

		DoublePointer minVal = new DoublePointer();
		DoublePointer maxVal = new DoublePointer();
		Point min = new Point();
		Point max = new Point();
		minMaxLoc(result, minVal, maxVal, min, max, null);
		rectangle(sourceColor, new Rect(max.x(), max.y(), template.cols(), template.rows()), randColor(), 2, 0, 0);

		destroyAllWindows();

		return (max);

	}

	// some usefull things.
	public static Scalar randColor() {
		int b, g, r;
		b = ThreadLocalRandom.current().nextInt(0, 255 + 1);
		g = ThreadLocalRandom.current().nextInt(0, 255 + 1);
		r = ThreadLocalRandom.current().nextInt(0, 255 + 1);
		return new Scalar(b, g, r, 0);
	}

	public static List<Point> getPointsFromMatAboveThreshold(Mat m, float t) {
		List<Point> matches = new ArrayList<Point>();
		FloatIndexer indexer = m.createIndexer();
		for (int y = 0; y < m.rows(); y++) {
			for (int x = 0; x < m.cols(); x++) {
				if (indexer.get(y, x) > t) {
					System.out.println("(" + x + "," + y + ") = " + indexer.get(y, x));
					matches.add(new Point(x, y));
				}
			}
		}
		return matches;
	}
}