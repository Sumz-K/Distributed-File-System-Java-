import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;


public class Toast {
    ToastLabel panel;
    int type;
    public Toast(String msg, int type) {
        panel = new ToastLabel();
        this.type = type;
        JLabel lbl = new JLabel();
        lbl.setText(msg);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 8, 8,8));
        panel.setPreferredSize(new Dimension(200, 50));
        NoScalingIcon icon = new NoScalingIcon(
                new ImageIcon("/Users/thalleencn/Desktop/Sem6/OOAD/PROJECT/FrontEnd/icons/check.png"));
        lbl.setIcon(icon);
        panel.setForeground(Color.WHITE);
        panel.setBackground(new Color(0,0,0,0));
        panel.add(lbl);
        // if (this.type == 1)
        //     panel.setBorder(new EtchedBorder(0, Color.GREEN, Color.GREEN));
        // if (this.type == 2)
        //     panel.setBorder(new EtchedBorder(0, Color.RED, Color.RED));
        // win.add(panel);
        // win.pack();
        // win.setVisible(true);
    }
    

    public void display() {
        
        Timer timer = new Timer(2000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                panel.setVisible(false);
            }
        });
        timer.setRepeats(false);
        timer.start();
        panel.setVisible(true);
    }

}