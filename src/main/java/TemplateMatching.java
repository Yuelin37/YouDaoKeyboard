import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_32F;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvMinMaxLoc;
import static org.bytedeco.javacpp.opencv_core.cvReleaseImage;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import static org.bytedeco.javacpp.opencv_core.cvZero;
import static org.bytedeco.javacpp.opencv_core.minMaxLoc;
import static org.bytedeco.javacpp.opencv_highgui.cvShowImage;
import static org.bytedeco.javacpp.opencv_highgui.cvWaitKey;
import static org.bytedeco.javacpp.opencv_highgui.destroyAllWindows;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.CV_TM_CCORR_NORMED;
import static org.bytedeco.javacpp.opencv_imgproc.TM_CCORR_NORMED;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvMatchTemplate;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.matchTemplate;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvScalar;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.indexer.FloatIndexer;

/**
 * Example of template javacv (opencv) template matching using the last java
 * build
 *
 * We need 2 default parameters like this (source image, image to find )
 * "C:\Users\Waldema\Desktop\bg.jpg" "C:\Users\Waldema\Desktop\imageToFind.jpg"
 *
 * @author Waldemar Neto
 */
public class TemplateMatching {

	public static void main(String[] args) throws Exception {

		String myArgs[] = new String[] {"/Users/ylyan/Desktop/Temp/bg2.jpg", "/Users/ylyan/Desktop/Temp/speaker.jpg"};
		newStyle(myArgs);
		// oldStyle(args);

	}

	public static void newStyle(String[] args) {
		// read in image default colors
		Mat sourceColor = imread(args[0]);
		Mat sourceGrey = new Mat(sourceColor.size(), CV_8UC1);
		cvtColor(sourceColor, sourceGrey, COLOR_BGR2GRAY);
		// load in template in grey
		Mat template = imread(args[1], CV_LOAD_IMAGE_GRAYSCALE);// int = 0
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

		System.out.println(max.x());
		System.out.println(max.y());
		imshow("Original marked", sourceColor);
//		imshow("Ttemplate", template);
//		imshow("Results matrix", result);
		waitKey(0);
		destroyAllWindows();

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

	public static void oldStyle(String[] args) {
		// get color source image to draw red rect on later
		IplImage srcColor = cvLoadImage(args[0]);
		// create blank 1 channel image same size as the source
		IplImage src = cvCreateImage(cvGetSize(srcColor), IPL_DEPTH_8U, 1);
		// convert source to grey and copy to src
		cvCvtColor(srcColor, src, CV_BGR2GRAY);
		// get the image to match loaded in greyscale.
		IplImage tmp = cvLoadImage(args[1], 0);
		// this image will hold the strength of the match
		// as the template is translated across the image
		IplImage result = cvCreateImage(cvSize(src.width() - tmp.width() + 1, src.height() - tmp.height() + 1),
				IPL_DEPTH_32F, src.nChannels());

		cvZero(result);

		// Match Template Function from OpenCV
		cvMatchTemplate(src, tmp, result, CV_TM_CCORR_NORMED);

		// double[] min_val = new double[2];
		// double[] max_val = new double[2];
		DoublePointer min_val = new DoublePointer();
		DoublePointer max_val = new DoublePointer();

		CvPoint minLoc = new CvPoint();
		CvPoint maxLoc = new CvPoint();

		cvMinMaxLoc(result, min_val, max_val, minLoc, maxLoc, null);

		// Get the Max or Min Correlation Value
		// System.out.println(Arrays.toString(min_val));
		// System.out.println(Arrays.toString(max_val));

		CvPoint point = new CvPoint();
		point.x(maxLoc.x() + tmp.width());
		point.y(maxLoc.y() + tmp.height());
		// cvMinMaxLoc(src, min_val, max_val,0,0,result);

		cvRectangle(srcColor, maxLoc, point, CvScalar.RED, 2, 8, 0); // Draw a
		// Rectangle for
		// Matched
		// Region

		cvShowImage("Lena Image", srcColor);
		cvWaitKey(0);
		cvReleaseImage(srcColor);
		cvReleaseImage(src);
		cvReleaseImage(tmp);
		cvReleaseImage(result);
	}
}