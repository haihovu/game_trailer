/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trailer;

import com.mitel.miutil.MiBackgroundTask;
import com.mitel.miutil.MiExceptionUtil;
import com.mitel.miutil.MiLogMsg;
import com.mitel.miutil.MiSystem;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.dejavu.game.DvActor;
import org.dejavu.game.DvAnimatedPanel;
import org.dejavu.game.DvCharacter;
import org.dejavu.game.DvControlKey;
import org.dejavu.game.DvTarget;

/**
 * This is the game trailer frame.
 * @author hai
 */
public class GameTrailer extends javax.swing.JFrame {
	private static final long serialVersionUID = 1L;
	/**
	 * The state of this application
	 */
	@XmlRootElement
	private static class State {
		@XmlAttribute
		private Integer width = 500;
		@XmlAttribute
		private Integer heigth = 400;
		@XmlAttribute
		private Integer x = 0;
		@XmlAttribute
		private Integer y = 0;
		/**
		 * Updates the state of this object with that of another's.
		 * @param aCopy The other state object whose value is to be copied.
		 */
		public void copy(State aCopy) {
			this.heigth = aCopy.getHeigth();
			this.width = aCopy.getWidth();
			this.x = aCopy.getX();
			this.y = aCopy.getY();
		}
		/**
		 * Imports the bounds into this state object.
		 * Note that the given bounds object is a hybrid where the X-Y coordinate
		 * is of that of the frame, where as the width-height is that of the animation panel.
		 * @param app The application from which to retrieve the bounds info.
		 * @return This object.
		 */
		public State importBounds(GameTrailer app) {
			Rectangle animationBounds = app.animation.getBounds();
			setHeigth(animationBounds.height);
			setWidth(animationBounds.width);
			
			Rectangle appBounds = app.getBounds();
			setX(appBounds.x);
			setY(appBounds.y);
			return this;
		}
		/**
		 * Loads the state of this object with data from a file.
		 * @param fromFile The file from which to retrieve the state data.
		 * @return This object.
		 */
		public State loadState(File fromFile) {
			if(fromFile.exists()) {
				try {
					JAXBContext ctx = JAXBContext.newInstance(State.class);
					Unmarshaller marshal = ctx.createUnmarshaller();
					Object pojo = marshal.unmarshal(fromFile);
					if(pojo instanceof State) {
						copy((State)pojo);
					}
				} catch (JAXBException ex) {
					MiSystem.logWarning(MiLogMsg.Category.DESIGN, MiExceptionUtil.simpleTrace(ex));
				}
			}
			return this;
		}
		/**
		 * Saves the state of this object to a file.
		 * @param toFile The file to which to save the state data.
		 */
		public void saveState(File toFile) {
			try {
				JAXBContext ctx = JAXBContext.newInstance(State.class);
				Marshaller marshal = ctx.createMarshaller();
				marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				marshal.marshal(this, toFile);
			} catch (JAXBException ex) {
				MiSystem.logWarning(MiLogMsg.Category.DESIGN, MiExceptionUtil.simpleTrace(ex));
			}
		}
		/**
		 * Retrieves the width of the animation panel.
		 * @return The requested value in number of pixels.
		 */
		@XmlTransient
		public int getWidth() {
			return width != null ? width : 0;
		}

		/**
		 * Specifies the width of the animation panel.
		 * @param width The new value in number of pixels.
		 * @return This object
		 */
		public State setWidth(int width) {
			this.width = width;
			return this;
		}

		/**
		 * Retrieves the height of the animation panel.
		 * @return The requested value in number of pixels.
		 */
		@XmlTransient
		public int getHeigth() {
			return heigth != null ? heigth : 0;
		}

		/**
		 * Specifies the height of the animation panel.
		 * @param heigth The new value in number of pixels.
		 * @return This object
		 */
		public State setHeigth(int heigth) {
			this.heigth = heigth;
			return this;
		}

		/**
		 * Retrieves the X coordinate of the frame.
		 * @return The requested value in number of pixels.
		 */
		@XmlTransient
		public int getX() {
			return x != null ? x : 0;
		}

		/**
		 * Specifies the X coordinate of the frame.
		 * @param x The new value in number of pixels.
		 * @return This object.
		 */
		public State setX(int x) {
			this.x = x;
			return this;
		}

		/**
		 * Retrieves the Y coordinate of the frame.
		 * @return The requested value in number of pixels.
		 */
		@XmlTransient
		public int getY() {
			return y != null ? y : 0;
		}

		/**
		 * Specifies the Y coordinate of the frame.
		 * @param y The new value in number of pixels.
		 * @return 
		 */
		public State setY(int y) {
			this.y = y;
			return this;
		}
	}
	
	/**
	 * The task for moving the villain about inside the frame.
	 */
	private class VillainRunner extends MiBackgroundTask {
		private final long period;
		private final Map<String, Villain> villains = new HashMap<>(1024);
		/**
		 * Creates a new villain runner task
		 * @param period The task period in number of milliseconds.
		 */
		public VillainRunner(long period) {
			super("Villains");
			this.period = period;
		}
		
		public void addVillain(Villain villain) {
			synchronized(villains) {
				villains.put(villain.name, villain);
			}
		}
		
		public void removeVillain(String name) {
			synchronized(villains) {
				villains.remove(name);
			}
		}
		
		private Collection<Villain> getVillains() {
			synchronized(villains) {
				return new ArrayList<>(villains.values());
			}
		}
		@Override
		public void run() {
			try {
				while(getRunFlag()) {
					Rectangle bounds = getBounds();
					getVillains().stream().forEach((villain) -> {
						SwingUtilities.invokeLater(() -> {
							villain.moveVillain(bounds);
						});
					});
					synchronized(this) {
						this.wait(period);
					}
				}
			} catch (InterruptedException ex) {
			} finally {
			}
		}
	}
	/**
	 * Creates new game trailer.
	 * @throws java.io.IOException
	 */
	public GameTrailer() throws IOException {
		ImageIcon exitImg = new ImageIcon(GameTrailer.class.getClassLoader().getResource("icons/door_in.png"));
		ImageIcon villainLaunchImg = new ImageIcon(GameTrailer.class.getClassLoader().getResource("icons/tux.png"));
		
		initComponents(); 
		
		fileExitMenu.setIcon(exitImg);
		optionLaunchVillain.setIcon(villainLaunchImg);
		
		status = new VinInventory(animation);
		status.setScale(scalingFactor);
		add(animation.start(67), BorderLayout.CENTER);
		animation.setBackground(ImageIO.read(getClass().getClassLoader().getResource("background.png")));
		animation.setLayout(new GridBagLayout());
		Bullet.start(animation);
		animation.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				Point pt = evt.getPoint();
				if(actor != null) {
					Bullet.addBullet(actor.character.getPoint(), pt);
				}
			}
		});
		
		// Load the state from the archive file.
		state.loadState(stateFile);
		setLocation(state.getX(), state.getY());
		animation.setPreferredSize(new Dimension(state.getWidth(), state.getHeigth()));
		
		// Pack the window to fit everything according to their layouts.
		pack();
		
		// Add objects and actors later, using the EDT
		SwingUtilities.invokeLater(() -> {
			addStuff();
			try {
				actor = new DvActor(new DvCharacter("Nil", ImageIO.read(getClass().getClassLoader().getResource("nil-f1.png")), new Image[]{
					ImageIO.read(getClass().getClassLoader().getResource("nil-f2.png")),
					ImageIO.read(getClass().getClassLoader().getResource("nil-f3.png")),
				}).setScale(scalingFactor), animation);
				addActor(actor, new DvControlKey[]{
					new DvControlKey(DvControlKey.Direction.UP, KeyEvent.VK_UP),
					new DvControlKey(DvControlKey.Direction.DOWN, KeyEvent.VK_DOWN),
					new DvControlKey(DvControlKey.Direction.LEFT, KeyEvent.VK_LEFT),
					new DvControlKey(DvControlKey.Direction.RIGHT, KeyEvent.VK_RIGHT)
				});
			} catch (IOException ex) {
				MiSystem.logWarning(MiLogMsg.Category.DESIGN, MiExceptionUtil.simpleTrace(ex));
			}
		});
		villainRunner = new VillainRunner(33);
		villainRunner.start();
	}
	/**
	 * Animates the inventory belt for testing purposes.
	 */
	private class ItemDemoTask extends MiBackgroundTask {
		private final VinItem [] items;
		private final long period;
		/**
		 * Creates a new item player instance.
		 * @param period
		 * @throws IOException 
		 */
		public ItemDemoTask(long period) throws IOException {
			super("ItemPlayer");
			ClassLoader cl = GameTrailer.class.getClassLoader();
			items = new VinItem[] {
				new VinItem("Ancient Shield", ImageIO.read(cl.getResource("shield-ancient.png"))),
				new VinItem("Wooden Shield", ImageIO.read(cl.getResource("shield-wooden.png"))),
				new VinItem("Bomb", ImageIO.read(cl.getResource("bomb.png"))),
				new VinItem("Sword", ImageIO.read(cl.getResource("sword.png"))),
				new VinItem("Bow", ImageIO.read(cl.getResource("bow.png")))
			};
			this.period = period;
		}

		@Override
		public void run() {
			try {
				int idx = 0;
				boolean add = true;
				VinItem[] currentItems = new VinItem[3];
				while(getRunFlag()) {
					if(add) {
						if(idx < items.length) {
							status.addItem(items[idx]);
							++idx;
						} else {
							idx = 0;
							add = false;
							currentItems = status.getCurrItems().toArray(currentItems);
							continue;
						}
					} else {
						if(idx < currentItems.length) {
							status.removeItem(currentItems[idx].name);
							++idx;
						}  else {
							idx = 0;
							add = true;
							continue;
						}
					}
					synchronized(this) {
						this.wait(period);
					}
				}
			} catch (InterruptedException ex) {
			} finally {
				
			}
		}
	}
	private final State state = new State();
	private final DvAnimatedPanel animation = new DvAnimatedPanel();
	private final File stateFile = new File(new File(System.getProperty("user.home")), "gametrailer.xml");
	private DvActor actor;
	private MiBackgroundTask itemPlayer;
	private final VillainRunner villainRunner;
	private final VinInventory status;
	private static final double scalingFactor = 1.0;
	private static int villainIdx;
	
	/**
	 * Adds some static items onto the animation panel. Must be invoked from the EDT.
	 */
	private void addStuff() {
		try {
			Point center = new Point(state.getWidth() / 2, state.getHeigth() / 2);
			Image cabinImg = ImageIO.read(getClass().getClassLoader().getResource("cabin.png"));
			animation.addCharacter(new DvCharacter("Cabin", cabinImg, new Image[]{cabinImg}).setPoint(center).setScale(scalingFactor));
			animation.addComponent(status);
			itemPlayer = new ItemDemoTask(1000).start();
		} catch (IOException ex) {
			MiSystem.logWarning(MiLogMsg.Category.DESIGN, MiExceptionUtil.simpleTrace(ex));
		}
	}
	/**
	 * Adds a single actor, a character that can be controlled by the players, to
	 * the game.
	 * @param actor The actor to add.
	 * @param controlKeys The control keys to connect the actor to the game.
	 */
	private void addActor(DvActor actor, DvControlKey[] controlKeys) {
		animation.addCharacter(actor.character);
		actor.connectToComponent(this.getRootPane(), controlKeys);
	}
	/**
	 * Launches a new villain.
	 */
	private void launchVillains() {
		int numVillains = (int)(Math.random() * 10) + 3;
		for(int i = 0; i < numVillains; ++i) {
			Point center = new Point(state.getWidth() / 2, state.getHeigth() / 2);
			Villain villain = new Villain("Man" + villainIdx++, new DvTarget.TargetListener() {
				@Override
				public void hit(String name) {
					villainRunner.removeVillain(name);
				}

				@Override
				public void destroyed(String name) {
					villainRunner.removeVillain(name);
					Bullet.removeTarget(name);
					SwingUtilities.invokeLater(() -> {
						animation.removeCharacter(name);
					});
				}
			});
			villain.setScale(scalingFactor);
			animation.addCharacter(villain.setPoint(center));
			Bullet.addTarget(villain);
			villainRunner.addVillain(villain);
		}
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        fileExitMenu = new javax.swing.JMenuItem();
        optionMenu = new javax.swing.JMenu();
        optionLaunchVillain = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Vinny Village Game Trailer");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        fileMenu.setText("File");

        fileExitMenu.setText("Exit");
        fileExitMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileExitMenuActionPerformed(evt);
            }
        });
        fileMenu.add(fileExitMenu);

        menuBar.add(fileMenu);

        optionMenu.setText("Option");

        optionLaunchVillain.setText("Launch villains");
        optionLaunchVillain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionLaunchVillainActionPerformed(evt);
            }
        });
        optionMenu.add(optionLaunchVillain);

        menuBar.add(optionMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if(itemPlayer != null) {
			itemPlayer.stop();
		}
		Bullet.stop();
		animation.stop();
		if(actor != null) {
			actor.disconnect();
		}
		state.importBounds(this).saveState(stateFile);
		villainRunner.stop();
    }//GEN-LAST:event_formWindowClosing

    private void fileExitMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileExitMenuActionPerformed
        formWindowClosing(null);
		dispose();
		System.exit(0);
    }//GEN-LAST:event_fileExitMenuActionPerformed

    private void optionLaunchVillainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionLaunchVillainActionPerformed
        launchVillains();
    }//GEN-LAST:event_optionLaunchVillainActionPerformed

	/**
	 * The program's entry point.
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		MiSystem.setLogLevel(2);
		
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(() -> {
			try {
				new GameTrailer().setVisible(true);
			} catch (IOException ex) {
				MiSystem.logError(MiLogMsg.Category.DESIGN, MiExceptionUtil.simpleTrace(ex));
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem fileExitMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem optionLaunchVillain;
    private javax.swing.JMenu optionMenu;
    // End of variables declaration//GEN-END:variables
}
