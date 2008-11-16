package jist.listener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Hashtable;

/**
 * SchemeTurtleGraphics is a Turtle Graphics program based on Daniel Azuma's
 * Turtle Tracks.
 * 
 * @author Jeff Wang
 */
public class SchemeTurtleGraphics {

	/**
	 * Names of primitives (keywords)
	 */
	static final int TURTLE_HEIGHT = 16;

	static final int TURTLE_HALF_WIDTH = 6;

	static final int DEFAULT_WINDOW_WIDTH = 400;

	static final int DEFAULT_WINDOW_HEIGHT = 400;

	private Color _backColor;

	private Color _penColor;

	private byte _penState;

	private double _xpos;

	private double _ypos;

	int _xhalfsize;

	int _yhalfsize;

	private double _heading;

	private boolean _turtleVisible;

	private byte _wrapState;

	private TurtleWind _graphWind;

	Graphics _graphContext;

	private Hashtable _colorNames;

	RedisplayThread _thread;

	int _redisplayInterval;

	private static final byte PS_DOWN = 1;

	private static final byte PS_UP = 2;

	private static final byte PS_ERASE = 3;

	private static final byte WS_WRAP = 1;

	private static final byte WS_FENCE = 2;

	private static final byte WS_WINDOW = 3;

	public SchemeTurtleGraphics() {
		setup();
		System.err.println("new STG: " + this);
	}

	/**
	 * Set up primitive group
	 */
	protected void setup() {

		_graphWind = null;
		_thread = null;
		_redisplayInterval = 0;

		resetPalette();

	}

	/**
	 * VM is closing down
	 */
	protected void exiting() {
		synchronized (this) {
			if (_graphWind != null) {
				_graphWind.dispose();
				_graphWind = null;
				_thread.kill();
				_thread = null;
			}
		}
	}

	/**
	 * Primitive implementation for BACK
	 */

	public final void pBack(double d) {
		double dist = -d;
		synchronized (this) {
			double headingRad = (90.0 - _heading) / 180.0 * Math.PI;
			moveTo(_xpos + dist * Math.cos(headingRad), _ypos + dist
					* Math.sin(headingRad));

		}
	}

	/**
	 * Primitive implementation for CLEAN
	 */
	public final void pClean() {

		synchronized (this) {
			if (_graphWind != null) {
				_graphWind.reset();
			}
		}

	}

	/**
	 * Primitive implementation for CLEARSCREEN
	 */
	public final void pClearscreen() {

		synchronized (this) {
			if (_graphWind != null) {
				_graphWind.reset();
			}
			_xpos = 0;
			_ypos = 0;
			_heading = 0;
			updateTurtle();
		}

	}

	/**
	 * Primitive implementation for SHOWNP
	 */
	public final boolean pShown() {
		return _turtleVisible;
	}

	/**
	 * Primitive implementation for DISTANCETOXY
	 */
	public final double pDistancetoxy(double x, double y) {
		double xval = x;
		double yval = y;
		double dist;
		synchronized (this) {
			dist = Math.sqrt((xval - _xpos) * (xval - _xpos) + (yval - _ypos)
					* (yval - _ypos));
		}
		return dist;
	}

	/**
	 * Primitive implementation for DRAW
	 */
	public final void pDraw(int x, int y) {
		int xs, ys;
		if ((x < 0) || (y < 0)) {
			xs = DEFAULT_WINDOW_WIDTH / 2;
			ys = DEFAULT_WINDOW_HEIGHT / 2;
		} else {
			xs = x;
			ys = y;
		}

		synchronized (this) {
			_xhalfsize = xs;
			_yhalfsize = ys;
			if (_graphWind != null) {
				_graphWind.reset();
			} else {
				try {
					_graphWind = new TurtleWind();
					_thread = new RedisplayThread();
					_thread.start();
				} catch (InternalError e) {
				}
			}
			if (_graphWind == null) {
				System.err.println("_graphWind is null");
			}
			_backColor = Color.black;
			_penColor = Color.white;
			_penState = PS_DOWN;
			_xpos = 0;
			_ypos = 0;
			_heading = 0;
			_turtleVisible = true;
			_wrapState = WS_WRAP;
			updateTurtle();
		}
	}

	public final void pDraw() {
		pDraw(-1, -1);
	}

	/**
	 * Primitive implementation for FENCE
	 */
	public final void pFence() {
		synchronized (this) {
			if (_xpos < -_xhalfsize || _xpos > _xhalfsize - 1
					|| _ypos < -_yhalfsize || _ypos > _yhalfsize - 1) {
				System.err.println("Fence out of range");
			}
			_wrapState = WS_FENCE;
			updateTurtle();
		}

	}

	/**
	 * Primitive implementation for FORWARD
	 */
	public final void pForward(double d) {
		double dist = d;
		synchronized (this) {
			double headingRad = (90.0 - _heading) / 180.0 * Math.PI;
			moveTo(_xpos + dist * Math.cos(headingRad), _ypos + dist
					* Math.sin(headingRad));
		}

	}

	/**
	 * Primitive implementation for GETBACKGROUND
	 */
	public final int[] pGetbackground() {
		int[] a = new int[3];
		a[0] = _backColor.getRed();
		a[1] = _backColor.getGreen();
		a[2] = _backColor.getBlue();
		return a;
	}

	/**
	 * Primitive implementation for GETPENCOLOR
	 */
	public final int[] pGetpencolor() {
		int[] a = new int[3];
		a[0] = _penColor.getRed();
		a[1] = _penColor.getGreen();
		a[2] = _penColor.getBlue();
		return a;
	}

	/**
	 * Primitive implementation for HEADING
	 */
	public final double pHeading() {
		double temp;
		synchronized (this) {
			temp = _heading;
		}
		return temp;
	}

	/**
	 * Primitive implementation for HIDETURTLE
	 */
	public final void pHideturtle() {
		synchronized (this) {
			_turtleVisible = false;
			updateTurtle();
		}
	}

	/**
	 * Primitive implementation for HOME
	 */
	public final void pHome() {
		synchronized (this) {
			drawLineTo(0, 0);
			_xpos = 0;
			_ypos = 0;
			_heading = 0;
			updateTurtle();
		}

	}

	/**
	 * Primitive implementation for LABEL
	 */
	public final void pLabel(String l) {

		String str = l;

		synchronized (this) {
			if (_graphWind != null && _penState != PS_UP) {
				if (_penState == PS_DOWN) {
					_graphContext.setColor(_penColor);
				} else {
					_graphContext.setColor(_backColor);
				}
				_graphContext.drawString(str, (int) (Math.round(_xhalfsize
						+ _xpos)), (int) (Math.round(_yhalfsize - _ypos)));
				_graphWind.redisplay();
			}
		}

	}

	/**
	 * Primitive implementation for LEFT
	 */
	public final void pLeft(double d) {

		double val = d;
		synchronized (this) {
			_heading = fixAngle(_heading - val);
			updateTurtle();
		}
	}

	/**
	 * Primitive implementation for NODRAW
	 */
	public final void pNodraw() {
		synchronized (this) {
			if (_graphWind != null) {
				_graphWind.dispose();
				_graphWind = null;
				_graphContext = null;
				_thread.kill();
				_thread = null;
				_redisplayInterval = 0;
			}
		}

	}

	/**
	 * Primitive implementation for PENDOWN
	 */
	public final void pPendown() {
		synchronized (this) {
			_penState = PS_DOWN;
		}
	}

	/**
	 * Primitive implementation for PENERASE
	 */
	public final void pPenerase() {
		synchronized (this) {
			_penState = PS_ERASE;
		}
	}

	/**
	 * Primitive implementation for PENUP
	 */
	public final void pPenup() {
		synchronized (this) {
			_penState = PS_UP;
		}
	}

	/**
	 * Primitive implementation for POS
	 */
	public final double[] pPos() {

		double val1;
		double val2;
		double[] a = new double[2];
		synchronized (this) {
			val1 = _xpos;
			val2 = _ypos;
		}
		a[0] = val1;
		a[1] = val2;
		return a;
	}

	/**
	 * Primitive implementation for REFRESH
	 */
	public final void pRefresh() {

		synchronized (this) {
			if (_graphWind != null) {
				_graphWind.update();
			}
		}
	}

	/**
	 * Primitive implementation for REFRESHINTERVAL
	 */
	public final void pRefreshinterval(int i) {

		synchronized (this) {
			if (_graphWind != null) {
				_redisplayInterval = i;
				_thread.setInterval(_redisplayInterval);
			}
		}
	}

	/**
	 * Primitive implementation for RIGHT
	 */
	public final void pRight(double d) {

		double val = d;
		synchronized (this) {
			_heading = fixAngle(_heading + val);
			updateTurtle();
		}
	}

	/**
	 * Primitive implementation for SETBACKGROUND
	 */
	public final void pSetbackground(String color) {

		Color temp = (Color) _colorNames.get(color);

		if (temp == null)
			return;

		synchronized (this) {
			_backColor = temp;
		}
	}

	/**
	 * Primitive implementation for SETHEADING
	 */
	public final void pSetheading(double d) {
		double val = d;
		synchronized (this) {
			_heading = fixAngle(val);
			updateTurtle();
		}

	}

	/**
	 * Primitive implementation for SETPENCOLOR
	 */
	public final void pSetpencolor(String color) {
		Color c = (Color) _colorNames.get(color);
		if (c == null)
			return;

		synchronized (this) {
			_penColor = c;
		}
	}

	/**
	 * Primitive implementation for SETX
	 */
	public final void pSetx(double d) {

		double val = d;
		synchronized (this) {
			if (_wrapState == WS_FENCE
					&& (val < -_xhalfsize || val > _xhalfsize - 1)) {
				System.err.println("Turtle out of bounds");
			}
			if (_wrapState == WS_WRAP) {
				while (true) {
					if (val < -_xhalfsize - 0.5) {
						val = wrapXMinus(val, _ypos);
					} else if (val > _xhalfsize - 0.5) {
						val = wrapXPlus(val, _ypos);
					} else {
						break;
					}
				}
			}
			drawLineTo(val, _ypos);
			_xpos = val;
			updateTurtle();
		}

	}

	/**
	 * Primitive implementation for SETXY
	 */
	public final void pSetxy(double x, double y) {

		double xval = x;
		double yval = y;
		synchronized (this) {
			moveTo(xval, yval);
		}
	}

	/**
	 * Primitive implementation for SETY
	 */
	public final void pSety(double y) {

		double val = y;
		synchronized (this) {
			if (_wrapState == WS_FENCE
					&& (val < -_yhalfsize || val > _yhalfsize - 1)) {
				System.err.println("Turtle out of bounds");
			}
			if (_wrapState == WS_WRAP) {
				while (true) {
					if (val < -_yhalfsize - 0.5) {
						val = wrapYMinus(_xpos, val);
					} else if (val > _yhalfsize - 0.5) {
						val = wrapYPlus(_xpos, val);
					} else {
						break;
					}
				}
			}
			drawLineTo(_xpos, val);
			_ypos = val;
			updateTurtle();
		}

	}

	/**
	 * Primitive implementation for SHOWTURTLE
	 */
	public final void pShowturtle() {

		synchronized (this) {
			_turtleVisible = true;
			updateTurtle();
		}
	}

	/**
	 * Primitive implementation for TOWARDSXY
	 */
	public final double pTowardsxy(double x, double y) {

		double xval = x;
		double yval = y;
		double ang;
		synchronized (this) {
			ang = fixAngle(90.0 - Math.atan2(yval - _ypos, xval - _xpos)
					/ Math.PI * 180);
		}
		return ang;
	}

	/**
	 * Primitive implementation for WINDOW
	 */
	public final void pWindow() {
		synchronized (this) {
			_wrapState = WS_WINDOW;
			updateTurtle();
		}
	}

	/**
	 * Primitive implementation for WRAP
	 */
	public final void pWrap() {

		synchronized (this) {
			_xpos = wrapCoordinate(_xpos, _xhalfsize);
			_ypos = wrapCoordinate(_ypos, _yhalfsize);
			_wrapState = WS_WRAP;
			updateTurtle();
		}
	}

	/**
	 * Primitive implementation for XCOR
	 */
	public final double pXcor() {

		double val;
		synchronized (this) {
			val = _xpos;
		}
		return val;
	}

	/**
	 * Primitive implementation for XSIZE
	 */
	public final double pXsize() {

		double val;
		synchronized (this) {
			val = _xhalfsize;
		}
		return val;
	}

	/**
	 * Primitive implementation for YCOR
	 */
	public final double pYcor() {
		double val;
		synchronized (this) {
			val = _ypos;
		}
		return val;
	}

	/**
	 * Primitive implementation for YSIZE
	 */
	public final double pYsize() {

		double val;
		synchronized (this) {
			val = _yhalfsize;
		}
		return val;
	}

	/**
	 * Primitive implementation for SETPALETTE
	 */
	public final void pSetpalette(String color, int r, int g, int b) {

		Color c = new Color(r, g, b);
		_colorNames.put(color, c);
	}

	/**
	 * Primitive implementation for UNSETPALETTE
	 */
	public final void pUnsetpalette(String color) {
		_colorNames.remove(color);
	}

	/**
	 * Primitive implementation for PALETTE
	 */
	public final int[] pPalette(String c) {

		int[] result = new int[3];
		Color color = (Color) (_colorNames.get(c));

		if (color == null) {
			return null;
		}

		result[0] = color.getRed();
		result[1] = color.getGreen();
		result[2] = color.getBlue();
		return result;
	}

	/**
	 * Primitive implementation for PALETTEP
	 */
	public final boolean pPalettep(String c) {
		return (_colorNames.get(c) != null);
	}

	/**
	 * Move to x and y coordinate
	 * 
	 * @param xval
	 *            x coordinate
	 * @param yval
	 *            y coordinate
	 * 
	 * @exception virtuoso.logo.TurtleBoundsException
	 *                turtle out of bounds
	 */
	private final void moveTo(double xval, double yval) {
		if (_wrapState == WS_FENCE
				&& (xval < -_xhalfsize || xval > _xhalfsize - 1
						|| yval < -_yhalfsize || yval > _yhalfsize - 1)) {
			System.err.println("Turtle out of bounds");
		}
		if (_wrapState == WS_WRAP) {
			while (true) {
				if (xval < -_xhalfsize - 0.5) {
					if (yval < -_yhalfsize - 0.5) {
						if ((-_xhalfsize - 0.5 - _xpos) * (yval - _ypos) > (-_yhalfsize - 0.5 - _ypos)
								* (xval - _xpos)) {
							yval = wrapYMinus(xval, yval);
						} else {
							xval = wrapXMinus(xval, yval);
						}
					} else if (yval > _yhalfsize - 0.5) {
						if ((-_xhalfsize - 0.5 - _xpos) * (yval - _ypos) < (_yhalfsize - 0.5 - _ypos)
								* (xval - _xpos)) {
							yval = wrapYPlus(xval, yval);
						} else {
							xval = wrapXMinus(xval, yval);
						}
					} else {
						xval = wrapXMinus(xval, yval);
					}
				} else if (xval > _xhalfsize - 0.5) {
					if (yval < -_yhalfsize - 0.5) {
						if ((_xhalfsize - 0.5 - _xpos) * (yval - _ypos) < (-_yhalfsize - 0.5 - _ypos)
								* (xval - _xpos)) {
							yval = wrapYMinus(xval, yval);
						} else {
							xval = wrapXPlus(xval, yval);
						}
					} else if (yval > _yhalfsize - 0.5) {
						if ((_xhalfsize - 0.5 - _xpos) * (yval - _ypos) > (_yhalfsize - 0.5 - _ypos)
								* (xval - _xpos)) {
							yval = wrapYPlus(xval, yval);
						} else {
							xval = wrapXPlus(xval, yval);
						}
					} else {
						xval = wrapXPlus(xval, yval);
					}
				} else {
					if (yval < -_yhalfsize - 0.5) {
						yval = wrapYMinus(xval, yval);
					} else if (yval > _yhalfsize - 0.5) {
						yval = wrapYPlus(xval, yval);
					} else {
						break;
					}
				}
			}
		}
		drawLineTo(xval, yval);
		_xpos = xval;
		_ypos = yval;
		updateTurtle();
		return;
	}

	/**
	 * Draw a line in the graphics context
	 */
	private final void drawLineTo(double x2, double y2) {
		if (_graphWind != null && _penState != PS_UP) {
			if (_penState == PS_DOWN) {
				_graphContext.setColor(_penColor);
			} else {
				_graphContext.setColor(_backColor);
			}
			_graphContext.drawLine((int) (Math.round(_xhalfsize + _xpos)),
					(int) (Math.round(_yhalfsize - _ypos)), (int) (Math
							.round(_xhalfsize + x2)), (int) (Math
							.round(_yhalfsize - y2)));
		}
	}

	/**
	 * Wrap coordinate into window bounds
	 * 
	 * @param val
	 *            value
	 * @param limit
	 *            value limit
	 * 
	 * @return wrapped value
	 */
	private final double wrapCoordinate(double val, double limit) {
		double limit2 = limit * 2.0;
		double ret;

		if (val < 0) {
			ret = val + (limit2 * Math.floor((-val) / limit2)) + limit2;
		} else {
			ret = val - (limit2 * Math.floor(val / limit2));
		}
		if (ret > limit - 0.5) {
			return ret - limit2;
		} else {
			return ret;
		}
	}

	/**
	 * Fix angle (make it between 0 and 360)
	 * 
	 * @param ang
	 *            angle
	 * 
	 * @return fixed angle
	 */
	private final double fixAngle(double ang) {
		if (ang < 0) {
			return ang + (360.0 * Math.floor((-ang) / 360.0)) + 360.0;
		} else {
			return ang - (360.0 * Math.floor(ang / 360.0));
		}
	}

	/**
	 * Setxy wrap helper
	 * 
	 * @param xval
	 *            x coordinate
	 * @param yval
	 *            y coordinate
	 * 
	 * @return new xval
	 */
	private final double wrapXMinus(double xval, double yval) {
		double newy = _ypos + (-_xhalfsize - 0.5 - _xpos) / (xval - _xpos)
				* (yval - _ypos);
		drawLineTo(-_xhalfsize - 0.5, newy);
		_xpos = _xhalfsize - 0.5;
		_ypos = newy;
		updateTurtle();
		return xval + _xhalfsize * 2.0;
	}

	/*
	 * Setxy wrap helper
	 * 
	 * @param xval x coordinate @param yval y coordinate
	 * 
	 * @return new yval
	 */
	private final double wrapYMinus(double xval, double yval) {
		double newx = _xpos + (-_yhalfsize - 0.5 - _ypos) / (yval - _ypos)
				* (xval - _xpos);
		drawLineTo(newx, -_yhalfsize - 0.5);
		_ypos = _yhalfsize - 0.5;
		_xpos = newx;
		updateTurtle();
		return yval + _yhalfsize * 2.0;
	}

	/**
	 * Setxy wrap helper
	 * 
	 * @param xval
	 *            x coordinate
	 * @param yval
	 *            y coordinate
	 * 
	 * @return new xval
	 */
	private final double wrapXPlus(double xval, double yval) {
		double newy = _ypos + (_xhalfsize - 0.5 - _xpos) / (xval - _xpos)
				* (yval - _ypos);
		drawLineTo(_xhalfsize - 0.5, newy);
		_xpos = -_xhalfsize - 0.5;
		_ypos = newy;
		updateTurtle();
		return xval - _xhalfsize * 2.0;
	}

	/**
	 * Setxy wrap helper
	 * 
	 * @param xval
	 *            x coordinate
	 * @param yval
	 *            y coordinate
	 * 
	 * @return new yval
	 */
	private final double wrapYPlus(double xval, double yval) {
		double newx = _xpos + (_yhalfsize - 0.5 - _ypos) / (yval - _ypos)
				* (xval - _xpos);
		drawLineTo(newx, _yhalfsize - 0.5);
		_ypos = -_yhalfsize - 0.5;
		_xpos = newx;
		updateTurtle();
		return yval - _yhalfsize * 2.0;
	}

	/**
	 * Update graphics window's turtle info
	 */
	private final void updateTurtle() {
		if (_graphWind == null)
			throw new NullPointerException(
					"updateTurtle() when _graphWind null!");
		if (_graphWind != null) {
			_graphWind.setTurtleState(_xpos, _ypos, (90.0 - _heading) / 180.0
					* Math.PI, _turtleVisible, _wrapState == WS_WRAP);
		}
	}

	/**
	 * Reset color palette
	 */
	private final void resetPalette() {
		_colorNames = new Hashtable();

		_colorNames.put(new String("0"), Color.black);
		_colorNames.put(new String("1"), Color.blue);
		_colorNames.put(new String("2"), Color.green);
		_colorNames.put(new String("3"), Color.cyan);
		_colorNames.put(new String("4"), Color.red);
		_colorNames.put(new String("5"), Color.magenta);
		_colorNames.put(new String("6"), Color.yellow);
		_colorNames.put(new String("7"), Color.white);

		_colorNames.put(new String("black"), Color.black);
		_colorNames.put(new String("blue"), Color.blue);
		_colorNames.put(new String("cyan"), Color.cyan);
		_colorNames.put(new String("darkgray"), Color.darkGray);
		_colorNames.put(new String("gray"), Color.gray);
		_colorNames.put(new String("green"), Color.green);
		_colorNames.put(new String("lightgray"), Color.lightGray);
		_colorNames.put(new String("magenta"), Color.magenta);
		_colorNames.put(new String("orange"), Color.orange);
		_colorNames.put(new String("pink"), Color.pink);
		_colorNames.put(new String("red"), Color.red);
		_colorNames.put(new String("white"), Color.white);
		_colorNames.put(new String("yellow"), Color.yellow);
	}

	/**
	 * Update graph window
	 */
	synchronized final void updateWind() {
		if (_graphWind != null) {
			_graphWind.update();
		}
	}

	/**
	 * Redisplay thread inner class
	 */
	final class RedisplayThread extends Thread {

		private boolean _toggle;

		private boolean _alive;

		private int _interval;

		/**
		 * Constructor
		 */
		RedisplayThread() {
			_toggle = false;
			_alive = true;
			_interval = 0;
		}

		/**
		 * Toggle the thread
		 */
		synchronized final void toggle() {
			_toggle = true;
		}

		/**
		 * Kill the thread
		 */
		synchronized final void kill() {
			_alive = false;
			notifyAll();
		}

		/**
		 * Set the thread's tick interval
		 */
		synchronized final void setInterval(int val) {
			_interval = val;
			notifyAll();
		}

		/**
		 * Run the thread
		 */
		public void run() {
			boolean update = false;

			while (_alive) {
				synchronized (this) {
					try {
						if (_interval > 0) {
							wait(_interval);
						} else {
							wait();
						}
					} catch (InterruptedException e) {
					}
					update = (_alive && _toggle);
					_toggle = false;
				}
				if (update) {
					SchemeTurtleGraphics.this.updateWind();
				}
			}
		}

	}

	/**
	 * Graphics window inner class
	 */
	final class TurtleWind extends Frame {

		private Image _offImage;

		private int _xsize;

		private int _ysize;

		private double _turtleX;

		private double _turtleY;

		private double _turtleHead;

		private boolean _turtleVis;

		private boolean _turtleWrap;

		/**
		 * Create a TurtleWind
		 */
		TurtleWind() {
			setLayout(new BorderLayout());
			setResizable(false);
			setTitle("Logo Graphics");
			_xsize = _xhalfsize * 2;
			_ysize = _yhalfsize * 2;
			setSize(_xsize, _ysize);
			show();
			_offImage = createImage(_xsize, _ysize);
			if (_offImage == null) {
				System.err.println("Unable to open graphics window");
			}
			_graphContext = _offImage.getGraphics();
			_graphContext.setColor(Color.black);
			_graphContext.fillRect(0, 0, _xsize, _ysize);
			_graphContext.setFont(Font.decode("Monospaced"));
		}

		/**
		 * Reset a TurtleWind
		 */
		final void reset() {
			if (_xsize != _xhalfsize * 2 || _ysize != _yhalfsize * 2) {
				_offImage = createImage(_xhalfsize * 2, _yhalfsize * 2);
				if (_offImage == null) {
					System.err.println("Unable to resize graphics window");
				}
				_xsize = _xhalfsize * 2;
				_ysize = _yhalfsize * 2;
				setResizable(true);
				setSize(_xsize, _ysize);
				setResizable(false);
				_graphContext = _offImage.getGraphics();
			}
			_graphContext.setColor(Color.black);
			_graphContext.fillRect(0, 0, _xsize, _ysize);
			update();
		}

		/**
		 * Redraw the window
		 */
		public void paint(Graphics g) {
			update(g);
		}

		/**
		 * Update the window
		 */
		public synchronized void update() {
			update(getGraphics());
		}

		/**
		 * Update the window
		 */
		public synchronized void update(Graphics g) {
			if (_offImage == null) {
				System.err.println("update() _offImage == null");
				return;
			}

			g.drawImage(_offImage, 0, 0, this);
			if (_turtleVis) {
				g.setColor(Color.white);
				drawTurtle(g, _turtleX, _turtleY);
				if (_turtleWrap) {
					if (_turtleX > _xhalfsize - 20) {
						drawTurtle(g, _turtleX - _xsize, _turtleY);
						if (_turtleY > _yhalfsize - 20) {
							drawTurtle(g, _turtleX, _turtleY - _ysize);
							drawTurtle(g, _turtleX - _xsize, _turtleY - _ysize);
						} else if (_turtleY < -_yhalfsize + 20) {
							drawTurtle(g, _turtleX, _turtleY + _ysize);
							drawTurtle(g, _turtleX - _xsize, _turtleY + _ysize);
						}
					} else if (_turtleX < -_xhalfsize + 20) {
						drawTurtle(g, _turtleX + _xsize, _turtleY);
						if (_turtleY > _yhalfsize - 20) {
							drawTurtle(g, _turtleX, _turtleY - _ysize);
							drawTurtle(g, _turtleX + _xsize, _turtleY - _ysize);
						} else if (_turtleY < -_yhalfsize + 20) {
							drawTurtle(g, _turtleX, _turtleY + _ysize);
							drawTurtle(g, _turtleX + _xsize, _turtleY + _ysize);
						}
					} else {
						if (_turtleY > _yhalfsize - 20) {
							drawTurtle(g, _turtleX, _turtleY - _ysize);
						} else if (_turtleY < -_yhalfsize + 20) {
							drawTurtle(g, _turtleX, _turtleY + _ysize);
						}
					}
				}
			}
		}

		/**
		 * Draw the turtle
		 */
		final void drawTurtle(Graphics g, double x, double y) {
			int x1 = _xhalfsize
					+ (int) (Math.round(x + TURTLE_HEIGHT
							* Math.cos(_turtleHead)));
			int y1 = _yhalfsize
					- (int) (Math.round(y + TURTLE_HEIGHT
							* Math.sin(_turtleHead)));
			int x2 = _xhalfsize
					+ (int) (Math.round(x + TURTLE_HALF_WIDTH
							* Math.sin(_turtleHead)));
			int y2 = _yhalfsize
					- (int) (Math.round(y - TURTLE_HALF_WIDTH
							* Math.cos(_turtleHead)));
			int x3 = _xhalfsize
					+ (int) (Math.round(x - TURTLE_HALF_WIDTH
							* Math.sin(_turtleHead)));
			int y3 = _yhalfsize
					- (int) (Math.round(y + TURTLE_HALF_WIDTH
							* Math.cos(_turtleHead)));
			g.drawLine(x1, y1, x2, y2);
			g.drawLine(x2, y2, x3, y3);
			g.drawLine(x3, y3, x1, y1);
		}

		/**
		 * Set turtle state
		 */
		final void setTurtleState(double x, // screen coordinates
				double y, // screen coordinates
				double h, // standard polar coordinates
				boolean v, boolean w) {
			_turtleX = x;
			_turtleY = y;
			_turtleHead = h;
			_turtleVis = v;
			_turtleWrap = w;
			redisplay();
		}

		/**
		 * Redisplay screen
		 */
		final void redisplay() {
			if (_redisplayInterval == 0) {
				update();
			} else if (_redisplayInterval > 0) {
				_thread.toggle();
			}
		}

	}

}

/*
 * Copyright (c) 2004 Regents of the University of California (Regents). Created
 * by Graduate School of Education, University of California at Berkeley.
 * 
 * This software is distributed under the GNU General Public License, v2.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * REGENTS SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE SOFTWAREAND ACCOMPANYING DOCUMENTATION, IF ANY, PROVIDED
 * HEREUNDER IS PROVIDED "AS IS". REGENTS HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * IN NO EVENT SHALL REGENTS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * REGENTS HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
