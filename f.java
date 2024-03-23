import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import javax.swing.filechooser.*;
import javax.tools.JavaFileObject;
class f{
    static File selectedFile;
    static String performRead(File file) throws FileNotFoundException
    {
        System.out.println("Reading "+file.getName());
        StringBuilder contents = new StringBuilder();
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            contents.append(sc.nextLine()).append("\n");
        }
        sc.close();
        return contents.toString();
    }
    static void performWrite(JEditorPane ePane) throws IOException
    {
        FileWriter writer = new FileWriter(selectedFile);
        writer.write(ePane.getText());
        writer.close();
        System.out.println("Changes saved to " + selectedFile.getName());
    }
    static void editFile(File file) throws FileNotFoundException {
        if (file.canWrite()) {
            System.out.println("Can write to " + file.getName());
            JFrame eFrame = new JFrame("File Editor");
            eFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            eFrame.setSize(400, 200);
            JEditorPane ePane = new JEditorPane();
            ePane.setContentType("text/plain");
            try {
                ePane.setPage(file.toURI().toURL());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            JScrollPane scrollPane = new JScrollPane(ePane);
            
            // Create a panel to hold the scroll pane and the "Save" button
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);
            
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        performWrite(ePane);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            panel.add(saveButton, BorderLayout.SOUTH);
            
            // Set the panel as the content pane of the JFrame
            eFrame.setContentPane(panel);
            eFrame.setVisible(true);
        } else {
            System.out.println("Cannot write to " + file.getName());
        }
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Open File Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton b = new JButton("Open"); 
        b.setBounds(0,0,95,30);  
        JButton e = new JButton("Edit");
        e.setBounds(100,0,95,30);  
        String filePath = "C:\\Tannan\\PES\\SEM 6 (SEC K)\\OOAD\\Codes\\miniproj\\Swing";
        b.addActionListener(new ActionListener(){  
            public void actionPerformed(ActionEvent e){  
                        JFileChooser jf =  new JFileChooser(filePath);
                        jf.showOpenDialog(null);
                        selectedFile = jf.getSelectedFile();
                        try {
                            System.out.println(performRead(selectedFile));
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }

                    }  
                }); 
        frame.add(b); 
        e.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    editFile(selectedFile);
                } catch (FileNotFoundException e1) {
                    System.out.println("Need to select file first");
                }
                
            }
        });
        frame.add(e);
        frame.setSize(400,400);  
        frame.setLayout(null);  
        frame.setVisible(true);     
        
    }
}