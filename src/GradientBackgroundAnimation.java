import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.lang.Math;

public class GradientBackgroundAnimation extends JPanel implements ActionListener {
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 300;
    private static final int ANIMATION_DELAY = 16; // Delay in milliseconds between animation frames
    private static final int NUM_GRADIENTS_INITIAL = 25; // Initial number of gradients
    private static final int NEW_GRADIENT_DELAY = 10; // Delay in frames for generating a new gradient

    private List<GradientShape> gradients;
    private Timer timer;
    private Random random;
    private int frameCount;

    public GradientBackgroundAnimation() {
        setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setBackground(new Color(0x1d1d1d)); // Set a background color to avoid flickering
        
        gradients = new ArrayList<>();
        random = new Random();
        frameCount = 0;

        // Initialize gradients with random positions, sizes, and velocities
        for (int i = 0; i < NUM_GRADIENTS_INITIAL; i++) {
            addNewGradient();
        }

        // Create a timer for the animation
        timer = new Timer(ANIMATION_DELAY, this);
        timer.start(); // Start the animation timer
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Clear the background
        // g2d.setColor(new Color(0x1d1d1d));
        setBackground(Color.WHITE);
        g2d.fillRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT);

        // Draw the gradients
        for (GradientShape gradient : gradients) {
            gradient.draw(g2d);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update the positions and gradients
        Iterator<GradientShape> iterator = gradients.iterator();
        while (iterator.hasNext()) {
            GradientShape gradient = iterator.next();
            gradient.updatePosition(FRAME_WIDTH, FRAME_HEIGHT);
            gradient.updateGradient();

            // Remove gradients that reach the borders
            if (gradient.isAtBorder()) {

                iterator.remove();
            }
        }

        // Generate a new gradient periodically
        frameCount+=2;
        if (frameCount % NEW_GRADIENT_DELAY == 0) {
            addNewGradient();
        }

        repaint(); // Trigger a repaint to update the animation
    }

    private void addNewGradient() {
        int x = random.nextInt(FRAME_WIDTH);
        int y = random.nextInt(FRAME_HEIGHT);
        int size = random.nextInt(50) + 50; // Random size between 50 and 150
        double vx;
        do {
            // Generate a random number from -2 to 2
            vx = random.nextInt(5) - 2;
        } while (vx == 0);// Random velocity in x-direction (-2 to 2)
        double vy;
        do {
            // Generate a random number from -2 to 2
            vy = random.nextInt(5) - 2;
        } while (vy == 0); // Random velocity in y-direction (-2 to 2)
        gradients.add(new GradientShape(x, y, size, (vx), (vy)));
    }

    private static class GradientShape {
        private int x, y, size;
        private double vx, vy;
        private Color startColor, endColor;
        private float gradientPosition;
        private float alpha; // Alpha value for fading
        private ArrayList<Color> colour_list = new ArrayList<>();
        private Random random = new Random();
        int ri;
        public GradientShape(int x, int y, int size, double vx, double vy) {
            colour_list.add(new Color(0x77B0AA));
            colour_list.add(new Color(0xE9C874));
            colour_list.add(new Color(0xC0D6E8));
            colour_list.add(new Color(0xC0D6E8));
            colour_list.add(new Color(0x561C24));
            colour_list.add(new Color(0x8644A2));
            colour_list.add(new Color(0xF8F6E3));
            colour_list.add(new Color(0x77B0AA));
            this.x = x;
            this.y = y;
            this.size = size;
            this.vx = vx;
            this.vy = vy;
            ri = random.nextInt(colour_list.size());
            this.startColor = colour_list.get(ri);
            this.endColor = colour_list.get(ri);
            this.gradientPosition = 0.0f;
            this.alpha = 1.0f; // Start with full opacity
        }

        public void draw(Graphics2D g2d) {

            GradientPaint gradient = new GradientPaint(x, y, startColor,x + size, y + size,startColor, true);
            g2d.setPaint(gradient);
            g2d.fillRect(x, y, size, size);
        }

        public void updatePosition(int frameWidth, int frameHeight) {
            x += vx;
            y += vy;

            // Adjust position to stay within frame boundaries
            if (x < 0) {
                x = 0;
                vx = -vx; // Reverse velocity in x-direction
                alpha = 0.8f; // Reduce opacity when hitting the border
            } else if (x + size >= frameWidth) {
                x = frameWidth - size;
                vx = -vx; // Reverse velocity in x-direction
                alpha = 0.8f; // Reduce opacity when hitting the border
            }

            if (y < 0) {
                y = 0;
                vy = -vy; // Reverse velocity in y-direction
                alpha = 0.8f; // Reduce opacity when hitting the border
            } else if (y + size >= frameHeight) {
                y = frameHeight - size;
                vy = -vy; // Reverse velocity in y-direction
                alpha = 0.8f; // Reduce opacity when hitting the border
            }
        }

        public void updateGradient() {
            gradientPosition += 0.03f; // Adjust the speed of gradient transition
            if (gradientPosition >= 1.0f) {
                gradientPosition = 0.0f;
            }

            float adjustedPosition = gradientPosition * 2.0f; // Adjust the range to [0.0, 2.0]
            if (adjustedPosition >= 1.0f) {
                adjustedPosition = 2.0f - adjustedPosition; // Reverse the direction after 1.0
            }

            // Update the start and end colors based on the adjusted position
            // startColor = new Color(
            //         (int) (148 + (75 - 148) * adjustedPosition),
            //         0,
            //         (int) (211 - (211 - 130) * adjustedPosition));
            // endColor = new Color(
            //         (int) (148 + (75 - 148) * (1.0f - adjustedPosition)),
            //         0,
            //         (int) (211 - (211 - 130) * (1.0f - adjustedPosition)));
        }

        public boolean isAtBorder() {
            return x <= 0 || x + size >= FRAME_WIDTH || y <= 0 || y + size >= FRAME_HEIGHT;
        }
    }


}