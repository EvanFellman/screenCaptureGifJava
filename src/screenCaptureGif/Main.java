package screenCaptureGif;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class Main {
	public static Point upperLeftCorner;
	public static Point lowerRightCorner;
	public static LLNode headOfImages;
	public static LLNode tailOfImages;
	public static int numberOfImages;
	public static JFrame f;
	public static JFrame mainFrame;
	public static JLabel info;
	public static JLabel recording;
	public static JLabel loading;
	public static JSpinner speeds;
	public static JSpinner compression;
	public static final String INSTRUCTIONS = "<html>First hit record, then click the two corners of the rectangle to record. Next pull up this window to stop recording.</html>";
	public static final String RECORDING = "Open me to stop recording.";
	public static final String LOADING = "Loading...";
	public static boolean keepRecording = true;
	public static double SPEED = 0.125;
	public static double COMPRESS = 1;

	public static void main(String[] argv) throws Exception {
		mainFrame = new JFrame();
		mainFrame.setSize(500, 500);
		mainFrame.setResizable(false);
		mainFrame.setLayout(null);
		info = new JLabel(INSTRUCTIONS);
		info.setBounds(10, 0, 480, 30);
		mainFrame.add(info);
		JLabel speedLabel = new JLabel("Speed:");
		speedLabel.setBounds(0, 40, 70, 15);
		mainFrame.add(speedLabel);
		speeds = new JSpinner(new SpinnerNumberModel(5, 0.5, 30, 0.5));
		speeds.setBounds(80, 40, 50, 15);
		mainFrame.add(speeds);
		JLabel compressLabel = new JLabel("Compress:");
		compressLabel.setBounds(0, 60, 70, 15);
		mainFrame.add(compressLabel);
		compression = new JSpinner(new SpinnerNumberModel(1, 1, 20, 0.25));
		compression.setBounds(80, 60, 50, 15);
		mainFrame.add(compression);
		JButton button = new JButton("Record");
		button.setBounds(0, 100, 500, 400);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				text.addMouseListener(new MouseButtonRecognH());
				f = new JFrame();
				f.add(text);
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				f.setSize(screenSize.width, screenSize.height);
				f.setExtendedState(JFrame.MAXIMIZED_BOTH);
				f.setUndecorated(true);
				f.setOpacity(0.4f);
				f.setAlwaysOnTop(true);
				text.setForeground(Color.WHITE);
				text.setCaretColor(Color.WHITE);
				f.setVisible(true);
				Main.mainFrame.setState(Frame.ICONIFIED);
			}
		});
		mainFrame.add(button);
		mainFrame.setVisible(true);
	}
}

class MouseButtonRecognH extends MouseAdapter {

	@Override
	public void mouseClicked(MouseEvent event) {

		if ((event.getButton() == MouseEvent.BUTTON1)) {
			try {
				if (Main.lowerRightCorner == null) {
					Main.lowerRightCorner = new Point(event.getPoint().getX(), event.getPoint().getY());
				} else {
					Main.upperLeftCorner = new Point(event.getPoint().getX(), event.getPoint().getY());
					if (Main.upperLeftCorner.x > Main.lowerRightCorner.x) {
						int temp = Main.upperLeftCorner.x;
						Main.upperLeftCorner.x = Main.lowerRightCorner.x;
						Main.lowerRightCorner.x = temp;
					}
					if (Main.upperLeftCorner.y > Main.lowerRightCorner.y) {
						int temp = Main.upperLeftCorner.y;
						Main.upperLeftCorner.y = Main.lowerRightCorner.y;
						Main.lowerRightCorner.y = temp;
					}
					(new ButtonThread()).run();
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
}

class Point {
	public int x;
	public int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Point(double x, double y) {
		this.x = (int) x;
		this.y = (int) y;
	}

	public String toString() {
		return "(" + Integer.toString(this.x) + ", " + Integer.toString(this.y) + ")";
	}
}

class ButtonThread extends Thread {
	public void run() {
		try {
			Main.f.dispatchEvent(new WindowEvent(Main.f, WindowEvent.WINDOW_CLOSING));
			Main.mainFrame.setState(Frame.ICONIFIED);
			TimeUnit.MILLISECONDS.sleep(500);
			Main.SPEED = 1 / ((double) Main.speeds.getValue());
			Main.COMPRESS = 1 / ((double) Main.compression.getValue());
			Main.info.setText(Main.RECORDING);
			Main.mainFrame.repaint();
			while (Main.mainFrame.getState() == Frame.ICONIFIED) {
				PointerInfo a = MouseInfo.getPointerInfo();
				Point mousePos = new Point(a.getLocation().getX(), a.getLocation().getY());
				if (Main.tailOfImages == null) {
					Main.numberOfImages = 1;
					Main.headOfImages = new LLNode(
							(new Robot()).createScreenCapture(new Rectangle(Main.upperLeftCorner.x,
									Main.upperLeftCorner.y, Main.lowerRightCorner.x - Main.upperLeftCorner.x,
									Main.lowerRightCorner.y - Main.upperLeftCorner.y)),
							mousePos);
					Main.tailOfImages = Main.headOfImages;
				} else {
					Main.numberOfImages++;
					Main.tailOfImages.next = new LLNode(
							(new Robot()).createScreenCapture(new Rectangle(Main.upperLeftCorner.x,
									Main.upperLeftCorner.y, Main.lowerRightCorner.x - Main.upperLeftCorner.x,
									Main.lowerRightCorner.y - Main.upperLeftCorner.y)),
							mousePos);
					Main.tailOfImages = Main.tailOfImages.next;
				}
				TimeUnit.MILLISECONDS.sleep((int) (1000 * Main.SPEED));
			}
			Main.info.setText(Main.LOADING);
			Main.mainFrame.repaint();
			int i = 0;
			while ((new File("./HereYouGo" + Integer.toString(i) + ".gif")).exists()) {
				i++;
			}
			ImageOutputStream output = new FileImageOutputStream(new File("./HereYouGo" + Integer.toString(i) + ".gif"));
			GifSequenceWriter writer = new GifSequenceWriter(output, Main.headOfImages.img.getType(), (int) (1000 * Main.SPEED), true);
			LLNode t = Main.headOfImages;
			BufferedImage mouse = ImageIO.read(new File("mouse.png"));
			while (t != null) {
				Graphics g = t.img.getGraphics();
				g.drawImage(mouse, t.mousePosition.x - Main.upperLeftCorner.x, t.mousePosition.y - Main.upperLeftCorner.y, null);
				BufferedImage out = new BufferedImage((int) (t.img.getWidth() * Main.COMPRESS), (int) (t.img.getHeight() * Main.COMPRESS), BufferedImage.TYPE_INT_RGB);
				out.getGraphics().drawImage(t.img.getScaledInstance((int) (t.img.getWidth() * Main.COMPRESS), (int) (t.img.getHeight() * Main.COMPRESS), Image.SCALE_AREA_AVERAGING), 0, 0, null);
				writer.writeToSequence(out);
				t = t.next;
			}
			writer.close();
			output.close();
			Main.numberOfImages = 0;
			Main.lowerRightCorner = null;
			Main.upperLeftCorner = null;
			Main.headOfImages = null;
			Main.tailOfImages = null;
			Main.info.setText(Main.INSTRUCTIONS);
			Main.mainFrame.repaint();
		} catch (Exception e) {
		}
	}
}

class LLNode {
	public BufferedImage img;
	public LLNode next;
	public Point mousePosition;

	public LLNode(BufferedImage img, Point pos) {
		this.img = img;
		this.next = null;
		this.mousePosition = pos;
	}
}
