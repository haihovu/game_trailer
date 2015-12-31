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
import javax.imageio.ImageIO;
import org.dejavu.game.DvCharacter;
import org.dejavu.game.DvTarget;

/**
 * Representing a villain to be be used as targets
 * @author hai
 */
public class Villain extends DvTarget {
	private final TargetListener listener;
	private static final Image[] FLASH_IMAGES;
	private static final Image DAMAGED_MAN_IMAGE;
	private static final Image MAN_IMAGE;
	private int flashIdx;
	private int flashTimer;
	private long damaged;
	boolean junk;
	private float xMult = 1.0F;
	private float yMult = 1.0F;
	private float xOffset = 0.0F;
	private float yOffset = 0.0F;
	/**
	 * Creates a new villain.
	 * @param name The name of the villain. Should be unique.
	 * @param listener Target listener for hit/destroy events.
	 */
	public Villain(String name, TargetListener listener) {
		super(name, MAN_IMAGE, new Image[]{MAN_IMAGE});
		this.listener = listener;
	}
	
	@Override
	public void destroyed() {
		if(listener != null) {
			listener.destroyed(name);
		}
	}

	@Override
	public void hit() {
		synchronized(this) {
			if(damaged == 0) {
				damaged = System.currentTimeMillis();
				if(listener != null) {
					listener.hit(name);
				}
			} else if(junk) {
				destroyed();
			}
		}
	}

	@Override
	public DvCharacter setPoint(Point pnt) {
		xOffset = pnt.x;
		yOffset = pnt.y;
		return super.setPoint(pnt);
	}

	/**
	 * Moves the villain by the prescribed increments. May be invoked from any thread.
	 * @param bounds The bounds within which the villain must move.
	 */
	void moveVillain(Rectangle bounds) {
		Image curImg = getNextImage();

		float xInc = (float)(Math.random() * 3.0 + 1.0);
		float yInc = (float)(Math.random() * 2.0 + 0.5);
		xOffset += (xMult * xInc);
		yOffset += (yMult * yInc);
		double scale = getScale();
		if((xOffset + (curImg.getWidth(null) * scale / 2.0)) > bounds.width) {
			xMult = -1.0F;
		} else if(xOffset < 0.0) {
			xMult = 1.0F;
		} else if(Math.random() > 0.99) {
			xMult = -xMult;
		}
		if((yOffset + (curImg.getHeight(null) * scale / 2.0)) > bounds.height) {
			yMult = -1.0F;
		} else if(yOffset < 0.0) {
			yMult = 1.0F;
		} else if(Math.random() > 0.99) {
			yMult = -yMult;
		}

		super.setPoint(new Point((int)xOffset, (int)yOffset));
	}

	@Override
	public Image getNextImage() {
		synchronized(this) {
			if(damaged > 0) {
				long delta = System.currentTimeMillis() - damaged;
				if(delta < 1500) {
					if(flashTimer > 0) {
						--flashTimer;
					} else {
						flashTimer = 3;
						flashIdx++;
						if(flashIdx >= FLASH_IMAGES.length) {
							flashIdx = 0;
						}
					}
					return FLASH_IMAGES[flashIdx];
				}
				junk = true;
				return DAMAGED_MAN_IMAGE;
			}
			return super.getNextImage();
		}
	}
	
	static  {
		Image[] tmpFlashes = null;
		Image tmpDamagedMan = null;
		Image tmpMan = null;
		try {
			tmpFlashes = new Image[] {
				ImageIO.read(Villain.class.getClassLoader().getResource("asterisk_orange.png")),
				ImageIO.read(Villain.class.getClassLoader().getResource("asterisk_yellow.png"))
			};
			tmpDamagedMan = ImageIO.read(Villain.class.getClassLoader().getResource("rune.png"));
			tmpMan = ImageIO.read(Villain.class.getClassLoader().getResource("nil.png"));
		} catch (IOException ex) {
			MiSystem.logError(MiLogMsg.Category.DESIGN, MiExceptionUtil.simpleTrace(ex));
		}
		FLASH_IMAGES = tmpFlashes != null ? tmpFlashes : new Image[0];
		DAMAGED_MAN_IMAGE = tmpDamagedMan;
		MAN_IMAGE = tmpMan;
	}
}
