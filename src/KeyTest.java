
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author hai
 */
public class KeyTest extends javax.swing.JFrame {

	/**
	 * Creates new form KeyTest
	 */
	public KeyTest() {
		initComponents();
		
		// This here won't work since the frame (or any parent of the panel) catches all the key events.
		thePanel.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				theLabel.setText("B Key released " + e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				theLabel.setText("B Key pressed " + e);
			}
			
		});
		
		// This works, but the best way to handle key events is shown next ...
		// This here handles any key press, you'll have to figure out what was pressed
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				theLabel.setText("A Key released " + e.getKeyCode());
			}

			@Override
			public void keyPressed(KeyEvent e) {
				theLabel.setText("A Key pressed " + e.getKeyCode());
			}
		});
		
		// This is the best way ... in my opinion (for more info check out: https://docs.oracle.com/javase/tutorial/uiswing/misc/keybinding.html)
		// Here we handle specific key presses
		JRootPane theRootPane = getRootPane(); // It's important to catch key events right at the root pane
		ActionMap actionMap = theRootPane.getActionMap();
		InputMap inputMap = theRootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW/*You may need to experiment with this depending on what else is on the panel*/);
		
		// Handle key F1
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0/*No modifier here, e.g. Shift/Alt, ...*/), "f1"/*Any object will do, a string is more easy to understand*/);
		actionMap.put("f1"/*Same string as the input map above*/, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				theLabel2.setText("Someone pressed F1");
			}
		});
		
		// Handle key F10
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0/*No modifier here, e.g. Shift/Alt, ...*/), "f10"/*Any object will do, a string is more easy to understand*/);
		actionMap.put("f10"/*Same string as the input map above*/, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				theLabel2.setText("Someone pressed F10");
			}
		});
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        thePanel = new javax.swing.JPanel();
        theLabel = new javax.swing.JLabel();
        theLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        theLabel.setText("jLabel1");

        theLabel2.setText("jLabel1");

        javax.swing.GroupLayout thePanelLayout = new javax.swing.GroupLayout(thePanel);
        thePanel.setLayout(thePanelLayout);
        thePanelLayout.setHorizontalGroup(
            thePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(thePanelLayout.createSequentialGroup()
                .addGap(79, 79, 79)
                .addComponent(theLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(180, Short.MAX_VALUE))
            .addGroup(thePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, thePanelLayout.createSequentialGroup()
                    .addContainerGap(79, Short.MAX_VALUE)
                    .addComponent(theLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(178, Short.MAX_VALUE)))
        );
        thePanelLayout.setVerticalGroup(
            thePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, thePanelLayout.createSequentialGroup()
                .addContainerGap(164, Short.MAX_VALUE)
                .addComponent(theLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(109, 109, 109))
            .addGroup(thePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, thePanelLayout.createSequentialGroup()
                    .addContainerGap(132, Short.MAX_VALUE)
                    .addComponent(theLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(143, Short.MAX_VALUE)))
        );

        getContentPane().add(thePanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(KeyTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(KeyTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(KeyTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(KeyTest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
        //</editor-fold>

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new KeyTest().setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel theLabel;
    private javax.swing.JLabel theLabel2;
    private javax.swing.JPanel thePanel;
    // End of variables declaration//GEN-END:variables
}