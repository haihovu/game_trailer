/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trailer;

import com.mitel.miutil.MiExceptionUtil;
import com.mitel.miutil.MiLogMsg;
import com.mitel.miutil.MiSystem;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.dejavu.game.DvAnimatedPanel;
import org.dejavu.game.DvCharacter;
import org.dejavu.game.DvTarget;

/**
 * Represents bullets in the animation panel.
 * @author hai
 */
public class Bullet {
	/**
	 * The manager controlling the trajectory of every bullet, from the sources
	 * until they fall off the edge of the animation panel.
	 */
	private static class Manager extends TimerTask implements Listener {
		private final Map<String, Bullet> bullets = new HashMap<>(256);
		private final Timer timer = new Timer("BulletManager");
		private final DvAnimatedPanel animation;
		/**
		 * Creates a new manager.
		 * @param animation The animation panel.
		 */
		private Manager(DvAnimatedPanel animation) {
			this.animation = animation;
		}

		/**
		 * Adds a new bullet to the animation panel. Must be called from EDT.
		 * @param src The source location of the new bullet.
		 * @param dst A point in the trajectory of the new bullet.
		 */
		private void addBullet(Point src, Point dst) {
			SwingUtilities.invokeLater(()->{
				synchronized(bullets) {
					Bullet bullet = new Bullet(animation, src, dst, this);
					bullets.put(bullet.name, bullet);
					animation.addCharacter(bullet.bullet);
				}
			});
		}
		
		/**
		 * Removes a bullet from the animation panel. May be called from any thread.
		 * @param name The name of the target bullet.
		 */
		private void removeBullet(String name) {
			synchronized(bullets) {
				Bullet obsolete = bullets.remove(name);
				SwingUtilities.invokeLater(() -> {
					if(obsolete != null) {
						animation.removeCharacter(obsolete.name);
					}
				});
			}
		}
		
		/**
		 * Starts the manager.
		 * @return This object.
		 */
		private Manager start() {
			timer.schedule(this, 0, 33);
			return this;
		}
		
		/**
		 * Stops the manager.
		 */
		private void stop() {
			timer.cancel();
			cancel();
		}
		
		/**
		 * Retrieves the list of all existing bullets.
		 * @return The list of existing bullets. This is a copy of the internal list
		 * so it will not change, and the caller may modify the content as desired.
		 */
		private Collection<Bullet> getBullets() {
			synchronized(bullets) {
				return new ArrayList<>(bullets.values());
			}
		}

		@Override
		public void run() {
			Collection<Bullet> spent = new LinkedList<>();
			getBullets().stream().forEach((bullet) -> {
				bullet.move();
				getTargets().stream().forEach((target) -> {
					if(bullet.bullet.getBounds().intersects(target.getBounds())) {
						target.hit();
						spent.add(bullet);
					}
				});
			});
			spent.stream().forEach((bullet) -> {
				removeBullet(bullet.name);
			});
		}

		@Override
		public void completed(String name) {
			removeBullet(name);
		}
	}
	
	private static Manager mgr;
	private static final Map<String, DvTarget> targets = new HashMap<>(32);
	
	/**
	 * Starts the bullet animation manager.
	 * @param animation The animation panel.
	 */
	public static void start(DvAnimatedPanel animation) {
		synchronized(Bullet.class) {
			if(mgr == null) {
				mgr = new Manager(animation).start();
			}
		}
	}
	
	/**
	 * Stops the bullet animation manager.
	 */
	public static void stop() {
		Manager m;
		synchronized(Bullet.class) {
			m = getManager();
			mgr = null;
		}
		if(m != null) {
			m.stop();
		}
	}
	/**
	 * Adds a new bullet to the animation. May be called from any thread.
	 * @param src The source location of the new bullet.
	 * @param dst A point in the trajectory of the bullet.
	 */
	public static void addBullet(Point src, Point dst) {
		Manager m = getManager();
		if(m != null) {
			m.addBullet(src, dst);
		}
	}
	
	/**
	 * Retrieves the current bullet manager. May be called from any thread.
	 * @return The manager, or null if none was started.
	 */
	private static Manager getManager() {
		synchronized(Bullet.class) {
			return mgr;
		}
	}
	/**
	 * Removes a bullet from the animation.
	 * @param bullet The bullet to be removed.
	 */
	public static void removeBullet(Bullet bullet) {
		Manager m = getManager();
		if(m != null) {
			m.removeBullet(bullet.name);
		}
	}
	/**
	 * Adds a target to the bullet manager.
	 * @param target The new target to be added.
	 */
	public static void addTarget(DvTarget target) {
		synchronized(targets) {
			targets.put(target.name, target);
		}
	}
	private static Collection<DvTarget> getTargets() {
		synchronized(targets) {
			return new ArrayList<>(targets.values());
		}
	}
	/**
	 * Removes a target from the bullet manager.
	 * @param name The name of the target to be removed.
	 */
	public static void removeTarget(String name) {
		synchronized(targets) {
			targets.remove(name);
		}
	}
	/**
	 * Interface for handling asynchronous events.
	 */
	private static interface Listener {
		/**
		 * The bullet has completed its trajectory.
		 * @param name The name of the aforementioned bullet.
		 */
		void completed(String name);
	}
	
	private static Image BULLET_IMAGE;
	private static final double bulletSpeed = 10.0;
	private static int bulletIdx;
	/**
	 * The name of this bullet.
	 */
	public final String name;
	private final double incX;
	private final double incY;
	private double curX;
	private double curY;
	
	/**
	 * The visual representation of this bullet.
	 */
	public final DvCharacter bullet;
	private final Runnable work;
	
	/**
	 * Creates a new bullet marshal.
	 * @param animation The animation panel within which the bullet is to be fired.
	 * @param src The initial location of the bullet.
	 * @param dst A point in the trajectory of the bullet.
	 * @param listener Optional listener for termination event.
	 */
	private Bullet(DvAnimatedPanel animation, Point src, Point dst, Listener listener) {
		name = "Bullet" + getNewIndex();
		bullet = new DvCharacter(name, BULLET_IMAGE, new Image[]{BULLET_IMAGE});
		int deltaX = (dst.x - src.x);
		int deltaY = (dst.y - src.y);
		double dist = Math.sqrt((double)((deltaX * deltaX) + (deltaY * deltaY)));
		incX = (bulletSpeed * deltaX)/ dist;
		incY = (bulletSpeed * deltaY)/ dist;
		curX = src.x;
		curY = src.y;
		bullet.setPoint(src);
		work = ()->{
			curX += incX;
			curY += incY;
			Rectangle bounds = animation.getBounds();
			if((((int)curX) > bounds.width)||(((int)curY) > bounds.height)||(curX < 0.0)||(curY < 0.0)) {
				// Hit the edge of the panel, stop here.
				if(listener != null) {
					listener.completed(name);
				}
			} else {
				bullet.setPoint(new Point((int)curX, (int)curY));
			}
		};
	}
 
	/**
	 * Get a new bullet index.
	 * @return A new bullet index.
	 */
	private static int getNewIndex() {
		synchronized(Bullet.class) {
			return bulletIdx++;
		}
	}
	
	/**
	 * Moves the bullet along the trajectory. May be called from any thread.
	 */
	private void move() {
		SwingUtilities.invokeLater(work);
	}
	
	static {
		try {
			BULLET_IMAGE = ImageIO.read(Bullet.class.getClassLoader().getResource("magiccnt.png"));
		} catch (IOException ex) {
			MiSystem.logError(MiLogMsg.Category.DESIGN, MiExceptionUtil.simpleTrace(ex));
		}
	}
}
