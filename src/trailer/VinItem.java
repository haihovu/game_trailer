/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trailer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import org.dejavu.game.DvComponent;

/**
 * Represents items that the actor can hold in his belt.
 * @author hai
 */
public class VinItem extends DvComponent {
	public final String name;
	private final Image image;
	private Point location = new Point();
	private final AffineTransform xform = new AffineTransform();
	/**
	 * Creates a new item.
	 * @param name The name of the item.
	 * @param image The image representing the item.
	 */
	public VinItem(String name, Image image) {
		super(name);
		this.name = name;
		this.image = image;
	}
	/**
	 * Specifies the location of the item in the animation panel.
	 * @param point The item's new location.
	 * @return This item.
	 */
	public VinItem setLocation(Point point) {
		this.location = point;
		return this;
	}
	
	@Override
	public void draw(Graphics2D g2d) {
		xform.setTransform(getScale(), 0, 0, getScale(), location.x, location.y);
		g2d.drawImage(image, xform, null);
	}
	
}
