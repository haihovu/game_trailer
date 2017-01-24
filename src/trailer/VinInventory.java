/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trailer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.dejavu.game.DvAnimatedPanel;
import org.dejavu.game.DvComponent;
import org.dejavu.util.DjvExceptionUtil;
import org.dejavu.util.DjvLogMsg;
import org.dejavu.util.DjvSystem;

/**
 * Represent the actor's inventory belt.
 * @author hai
 */
public class VinInventory extends DvComponent {
	private final Image belt;
	private final Map<String, VinItem> items = new HashMap<>(32);
	private final DvAnimatedPanel animation;
	private final AffineTransform xform = new AffineTransform();
	private static final int itemYOffset = 6;
	private static final int itemXOffset = 13;
	/**
	 * Creates a new inventory belt.
	 * @param animation The animation panel within which the actor lives.
	 * @throws IOException Failed to load the belt image.
	 */
	public VinInventory(DvAnimatedPanel animation) throws IOException {
		super("Status");
		this.animation = animation;
		belt = ImageIO.read(VinInventory.class.getClassLoader().getResource("belt.png"));
	}
	/**
	 * Repaints the belt. Must be invoked from the EDT.
	 */
	private void repaint() {
		Rectangle bounds = animation.getBounds();
		int width = belt.getWidth(null);
		int x = bounds.width - width;
		animation.repaint(new Rectangle(x, 0, width, belt.getHeight(null)));
	}
	
	/**
	 * Adds a new item into the inventory. May be invoked from any thread.
	 * @param item The item to be added.
	 * @return This object.
	 */
	public VinInventory addItem(VinItem item) {
		SwingUtilities.invokeLater(() -> {
			if(items.size() > 2) {
				// Belt full, remove the first item
				for(VinItem i : items.values()) {
					items.remove(i.name);
					break;
				}
			}
			items.put(item.name, item);
			repaint();
		});
		return this;
	}
	
	/**
	 * Removes an item from the inventory. May be invoked from any thread.
	 * @param name The name of the target item.
	 * @return This object.
	 */
	public VinInventory removeItem(String name) {
		SwingUtilities.invokeLater(() -> {
			items.remove(name);
			repaint();
		});
		return this;
	}
	
	/**
	 * Retrieves the list of all current items in the inventory. May be invoked from
	 * any thread.
	 * @return THe current inventory list.
	 */
	public Collection<VinItem> getCurrItems() {
		Collection<VinItem> ret = new LinkedList<>();
		try {
			if(!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeAndWait(() -> {
					ret.addAll(items.values());
				});
			} else {
				ret.addAll(items.values());
			}
		} catch (InterruptedException | InvocationTargetException ex) {
			DjvSystem.logInfo(DjvLogMsg.Category.DESIGN, DjvExceptionUtil.simpleTrace(ex));
		}
		return ret;
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		Rectangle bounds = animation.getBounds();
		int width = belt.getWidth(null);
		int x = bounds.width - (int)(width * getScale());
		xform.setTransform(getScale(), 0.0, 0.0, getScale(), x, 0);
		g2d.drawImage(belt, xform, null);
		int count = 0;
		for(VinItem item : items.values()) {
			item.setLocation(new Point(x + (int)(getScale() * (itemXOffset + (count * 19))), (int)(getScale() * itemYOffset)));
			item.setScale(getScale());
			item.draw(g2d);
			++count;
		}
	}
}
