import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Upload {
    Uploadbutton uploadPanel = new Uploadbutton();
    Timer slideInTimer;
    int currentY; 

    Upload(JPanel loc)
    {
        uploadPanel.setPreferredSize(new Dimension(100, 80));
        uploadPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        uploadPanel.setBackground(Color.CYAN);
        
        JButton btn = new JButton();
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorder(BorderFactory.createEmptyBorder(4, 150, 0, 150));
        // btn.setSize(800,50);
        btn.setFont(new Font("Trebuchet MS", Font.BOLD, 40));
        btn.setText("    Upload");
        NoScalingIcon icon = new NoScalingIcon(
                new ImageIcon("/Users/thalleencn/Desktop/Sem6/OOAD/PROJECT/FrontEnd/icons/upload.png"));
        btn.setIcon(icon);
        
        btn.setForeground(Color.WHITE);
        btn.setActionCommand("Success");
        uploadPanel.add(btn);

        // btn.setActionCommand("Failure");

        ActionListener upload = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                     
                // onupload(() -> {

                //     String ac = e.getActionCommand();
                //     Toast t;
                //     if (ac.equals("Success")) {
                //     t = new Toast("Upload Successful", 1);
                //     } else {
                //     t = new Toast("Upload Failed", 2);
                //     }

                //     loc.add(t.panel);
                //     t.display();
                //     loc.revalidate();
                //     loc.repaint();
                    
                // });
                JFrame dnd = new FileDropFrame();
                
                
            }
        };
        

        btn.addActionListener(upload);
    }

 
   
}
