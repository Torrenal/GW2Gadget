package com.torrenal.craftingGadget.ui.components;

import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.ImageCapabilities;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.MenuComponent;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.im.InputContext;
import java.awt.im.InputMethodRequests;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.VolatileImage;
import java.awt.peer.ComponentPeer;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.EventListener;
import java.util.Locale;
import java.util.Set;

import javax.accessibility.AccessibleContext;
import javax.swing.Icon;
import javax.swing.InputVerifier;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.LabelUI;

@SuppressWarnings("deprecation")
public class CLabel extends JLabel
{
	private static final long serialVersionUID = 1L;

	public CLabel()
	{
	}

	public CLabel(String text)
	{
		super(text);
	}

	public CLabel(Icon image)
	{
		super(image);
	}

	public CLabel(String text, int horizontalAlignment)
	{
		super(text, horizontalAlignment);
	}

	public CLabel(Icon image, int horizontalAlignment)
	{
		super(image, horizontalAlignment);
	}

	public CLabel(String text, Icon icon, int horizontalAlignment)
	{
		super(text, icon, horizontalAlignment);
	}

	@Override
   public void updateUI()
   {
	   warnIfOffEventThread();
	   super.updateUI();
	   Font font = getFont();
	   if(font.getSize() < 12)
	   {
	   	font = font.deriveFont(12F);
		   setFont(font);
	   }
   }

	private void warnIfOffEventThread()
   {
	   if(!SwingUtilities.isEventDispatchThread())
	   {
//	   	new Error("Invalid Invocation: Invoked from Event Thread").printStackTrace();
	   }
   }

	
	@Override
   protected int checkHorizontalKey(int key, String message)
   {
	   warnIfOffEventThread();
	   return super.checkHorizontalKey(key, message);
   }

	@Override
   protected int checkVerticalKey(int key, String message)
   {
	   warnIfOffEventThread();
	   return super.checkVerticalKey(key, message);
   }

	@Override
   public AccessibleContext getAccessibleContext()
   {
	   warnIfOffEventThread();
	   return super.getAccessibleContext();
   }

	@Override
   public Icon getDisabledIcon()
   {
	   warnIfOffEventThread();
	   return super.getDisabledIcon();
   }

	@Override
   public int getDisplayedMnemonic()
   {
	   warnIfOffEventThread();
	   return super.getDisplayedMnemonic();
   }

	@Override
   public int getDisplayedMnemonicIndex()
   {
	   warnIfOffEventThread();
	   return super.getDisplayedMnemonicIndex();
   }

	@Override
   public int getHorizontalAlignment()
   {
	   warnIfOffEventThread();
	   return super.getHorizontalAlignment();
   }

	@Override
   public int getHorizontalTextPosition()
   {
	   warnIfOffEventThread();
	   return super.getHorizontalTextPosition();
   }

	@Override
   public Icon getIcon()
   {
	   warnIfOffEventThread();
	   return super.getIcon();
   }

	@Override
   public int getIconTextGap()
   {
	   warnIfOffEventThread();
	   return super.getIconTextGap();
   }

	@Override
   public Component getLabelFor()
   {
	   warnIfOffEventThread();
	   return super.getLabelFor();
   }

	@Override
   public String getText()
   {
	   warnIfOffEventThread();
	   return super.getText();
   }

	@Override
   public LabelUI getUI()
   {
	   warnIfOffEventThread();
	   return super.getUI();
   }

	@Override
   public String getUIClassID()
   {
	   warnIfOffEventThread();
	   return super.getUIClassID();
   }

	@Override
   public int getVerticalAlignment()
   {
	   warnIfOffEventThread();
	   return super.getVerticalAlignment();
   }

	@Override
   public int getVerticalTextPosition()
   {
	   warnIfOffEventThread();
	   return super.getVerticalTextPosition();
   }

	@Override
   public boolean imageUpdate(Image img, int infoflags, int x, int y, int w,
         int h)
   {
	   warnIfOffEventThread();
	   return super.imageUpdate(img, infoflags, x, y, w, h);
   }

	@Override
   protected String paramString()
   {
	   warnIfOffEventThread();
	   return super.paramString();
   }

	@Override
   public void setDisabledIcon(Icon disabledIcon)
   {
	   warnIfOffEventThread();
	   super.setDisabledIcon(disabledIcon);
   }

	@Override
   public void setDisplayedMnemonic(char aChar)
   {
	   warnIfOffEventThread();
	   super.setDisplayedMnemonic(aChar);
   }

	@Override
   public void setDisplayedMnemonic(int key)
   {
	   warnIfOffEventThread();
	   super.setDisplayedMnemonic(key);
   }

	@Override
   public void setDisplayedMnemonicIndex(int index)
         throws IllegalArgumentException
   {
	   warnIfOffEventThread();
	   super.setDisplayedMnemonicIndex(index);
   }

	@Override
   public void setHorizontalAlignment(int alignment)
   {
	   warnIfOffEventThread();
	   super.setHorizontalAlignment(alignment);
   }

	@Override
   public void setHorizontalTextPosition(int textPosition)
   {
	   warnIfOffEventThread();
	   super.setHorizontalTextPosition(textPosition);
   }

	@Override
   public void setIcon(Icon icon)
   {
	   warnIfOffEventThread();
	   super.setIcon(icon);
   }

	@Override
   public void setIconTextGap(int iconTextGap)
   {
	   warnIfOffEventThread();
	   super.setIconTextGap(iconTextGap);
   }

	@Override
   public void setLabelFor(Component c)
   {
	   warnIfOffEventThread();
	   super.setLabelFor(c);
   }

	@Override
   public void setText(String text)
   {
	   warnIfOffEventThread();
	   super.setText(text);
   }

	@Override
   public void setUI(LabelUI ui)
   {
	   warnIfOffEventThread();
	   super.setUI(ui);
   }

	@Override
   public void setVerticalAlignment(int alignment)
   {
	   warnIfOffEventThread();
	   super.setVerticalAlignment(alignment);
   }

	@Override
   public void setVerticalTextPosition(int textPosition)
   {
	   warnIfOffEventThread();
	   super.setVerticalTextPosition(textPosition);
   }

	@Override
   public void addAncestorListener(AncestorListener arg0)
   {
	   warnIfOffEventThread();
	   super.addAncestorListener(arg0);
   }

	@Override
   public void addNotify()
   {
	   warnIfOffEventThread();
	   super.addNotify();
   }

	@Override
   public synchronized void addVetoableChangeListener(
         VetoableChangeListener arg0)
   {
	   warnIfOffEventThread();
	   super.addVetoableChangeListener(arg0);
   }

	@Override
   public void computeVisibleRect(Rectangle arg0)
   {
	   warnIfOffEventThread();
	   super.computeVisibleRect(arg0);
   }

	@Override
   public boolean contains(int arg0, int arg1)
   {
	   warnIfOffEventThread();
	   return super.contains(arg0, arg1);
   }

	@Override
   public JToolTip createToolTip()
   {
	   warnIfOffEventThread();
	   return super.createToolTip();
   }

	@Override
   public void disable()
   {
	   warnIfOffEventThread();
	   super.disable();
   }

	@Override
   public void enable()
   {
	   warnIfOffEventThread();
	   super.enable();
   }

	@Override
   public void firePropertyChange(String arg0, boolean arg1, boolean arg2)
   {
	   warnIfOffEventThread();
	   super.firePropertyChange(arg0, arg1, arg2);
   }

	@Override
   public void firePropertyChange(String arg0, int arg1, int arg2)
   {
	   warnIfOffEventThread();
	   super.firePropertyChange(arg0, arg1, arg2);
   }

	@Override
   public void firePropertyChange(String arg0, char arg1, char arg2)
   {
	   warnIfOffEventThread();
	   super.firePropertyChange(arg0, arg1, arg2);
   }

	@Override
   protected void fireVetoableChange(String arg0, Object arg1, Object arg2)
         throws PropertyVetoException
   {
	   warnIfOffEventThread();
	   super.fireVetoableChange(arg0, arg1, arg2);
   }

	@Override
   public ActionListener getActionForKeyStroke(KeyStroke arg0)
   {
	   warnIfOffEventThread();
	   return super.getActionForKeyStroke(arg0);
   }

	@Override
   public float getAlignmentX()
   {
	   warnIfOffEventThread();
	   return super.getAlignmentX();
   }

	@Override
   public float getAlignmentY()
   {
	   warnIfOffEventThread();
	   return super.getAlignmentY();
   }

	@Override
   public AncestorListener[] getAncestorListeners()
   {
	   warnIfOffEventThread();
	   return super.getAncestorListeners();
   }

	@Override
   public boolean getAutoscrolls()
   {
	   warnIfOffEventThread();
	   return super.getAutoscrolls();
   }

	@Override
   public int getBaseline(int arg0, int arg1)
   {
	   warnIfOffEventThread();
	   return super.getBaseline(arg0, arg1);
   }

	@Override
   public BaselineResizeBehavior getBaselineResizeBehavior()
   {
	   warnIfOffEventThread();
	   return super.getBaselineResizeBehavior();
   }

	@Override
   public Border getBorder()
   {
	   warnIfOffEventThread();
	   return super.getBorder();
   }

	@Override
   public Rectangle getBounds(Rectangle arg0)
   {
	   warnIfOffEventThread();
	   return super.getBounds(arg0);
   }

	@Override
   protected Graphics getComponentGraphics(Graphics arg0)
   {
	   warnIfOffEventThread();
	   return super.getComponentGraphics(arg0);
   }

	@Override
   public JPopupMenu getComponentPopupMenu()
   {
	   warnIfOffEventThread();
	   return super.getComponentPopupMenu();
   }

	@Override
   public int getConditionForKeyStroke(KeyStroke arg0)
   {
	   warnIfOffEventThread();
	   return super.getConditionForKeyStroke(arg0);
   }

	@Override
   public int getDebugGraphicsOptions()
   {
	   warnIfOffEventThread();
	   return super.getDebugGraphicsOptions();
   }

	@Override
   public FontMetrics getFontMetrics(Font arg0)
   {
	   warnIfOffEventThread();
	   return super.getFontMetrics(arg0);
   }

	@Override
   public Graphics getGraphics()
   {
	   warnIfOffEventThread();
	   return super.getGraphics();
   }

	@Override
   public int getHeight()
   {
	   warnIfOffEventThread();
	   return super.getHeight();
   }

	@Override
   public boolean getInheritsPopupMenu()
   {
	   warnIfOffEventThread();
	   return super.getInheritsPopupMenu();
   }

	@Override
   public InputVerifier getInputVerifier()
   {
	   warnIfOffEventThread();
	   return super.getInputVerifier();
   }

	@Override
   public Insets getInsets()
   {
	   warnIfOffEventThread();
	   return super.getInsets();
   }

	@Override
   public Insets getInsets(Insets arg0)
   {
	   warnIfOffEventThread();
	   return super.getInsets(arg0);
   }

	@Override
   public <T extends EventListener> T[] getListeners(Class<T> arg0)
   {
	   warnIfOffEventThread();
	   return super.getListeners(arg0);
   }

	@Override
   public Point getLocation(Point arg0)
   {
	   warnIfOffEventThread();
	   return super.getLocation(arg0);
   }

	@Override
   public Dimension getMaximumSize()
   {
	   warnIfOffEventThread();
	   return super.getMaximumSize();
   }

	@Override
   public Dimension getMinimumSize()
   {
	   warnIfOffEventThread();
	   return super.getMinimumSize();
   }

	@Override
   public Component getNextFocusableComponent()
   {
	   warnIfOffEventThread();
	   return super.getNextFocusableComponent();
   }

	@Override
   public Point getPopupLocation(MouseEvent arg0)
   {
	   warnIfOffEventThread();
	   return super.getPopupLocation(arg0);
   }

	@Override
   public Dimension getPreferredSize()
   {
	   warnIfOffEventThread();
	   return super.getPreferredSize();
   }

	@Override
   public KeyStroke[] getRegisteredKeyStrokes()
   {
	   warnIfOffEventThread();
	   return super.getRegisteredKeyStrokes();
   }

	@Override
   public JRootPane getRootPane()
   {
	   warnIfOffEventThread();
	   return super.getRootPane();
   }

	@Override
   public Dimension getSize(Dimension arg0)
   {
	   warnIfOffEventThread();
	   return super.getSize(arg0);
   }

	@Override
   public Point getToolTipLocation(MouseEvent arg0)
   {
	   warnIfOffEventThread();
	   return super.getToolTipLocation(arg0);
   }

	@Override
   public String getToolTipText()
   {
	   warnIfOffEventThread();
	   return super.getToolTipText();
   }

	@Override
   public String getToolTipText(MouseEvent arg0)
   {
	   warnIfOffEventThread();
	   return super.getToolTipText(arg0);
   }

	@Override
   public Container getTopLevelAncestor()
   {
	   warnIfOffEventThread();
	   return super.getTopLevelAncestor();
   }

	@Override
   public TransferHandler getTransferHandler()
   {
	   warnIfOffEventThread();
	   return super.getTransferHandler();
   }

	@Override
   public boolean getVerifyInputWhenFocusTarget()
   {
	   warnIfOffEventThread();
	   return super.getVerifyInputWhenFocusTarget();
   }

	@Override
   public synchronized VetoableChangeListener[] getVetoableChangeListeners()
   {
	   warnIfOffEventThread();
	   return super.getVetoableChangeListeners();
   }

	@Override
   public Rectangle getVisibleRect()
   {
	   warnIfOffEventThread();
	   return super.getVisibleRect();
   }

	@Override
   public int getWidth()
   {
	   warnIfOffEventThread();
	   return super.getWidth();
   }

	@Override
   public int getX()
   {
	   warnIfOffEventThread();
	   return super.getX();
   }

	@Override
   public int getY()
   {
	   warnIfOffEventThread();
	   return super.getY();
   }

	@Override
   public void grabFocus()
   {
	   warnIfOffEventThread();
	   super.grabFocus();
   }

	@Override
   public boolean isDoubleBuffered()
   {
	   warnIfOffEventThread();
	   return super.isDoubleBuffered();
   }

	@Override
   public boolean isManagingFocus()
   {
	   warnIfOffEventThread();
	   return super.isManagingFocus();
   }

	@Override
   public boolean isOpaque()
   {
	   warnIfOffEventThread();
	   return super.isOpaque();
   }

	@Override
   public boolean isOptimizedDrawingEnabled()
   {
	   warnIfOffEventThread();
	   return super.isOptimizedDrawingEnabled();
   }

	@Override
   protected boolean isPaintingOrigin()
   {
	   warnIfOffEventThread();
	   return super.isPaintingOrigin();
   }

	@Override
   public boolean isPaintingTile()
   {
	   warnIfOffEventThread();
	   return super.isPaintingTile();
   }

	@Override
   public boolean isRequestFocusEnabled()
   {
	   warnIfOffEventThread();
	   return super.isRequestFocusEnabled();
   }

	@Override
   public boolean isValidateRoot()
   {
	   warnIfOffEventThread();
	   return super.isValidateRoot();
   }

	@Override
   public void paint(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.paint(arg0);
   }

	@Override
   protected void paintBorder(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.paintBorder(arg0);
   }

	@Override
   protected void paintChildren(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.paintChildren(arg0);
   }

	@Override
   protected void paintComponent(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.paintComponent(arg0);
   }

	@Override
   public void paintImmediately(Rectangle arg0)
   {
	   warnIfOffEventThread();
	   super.paintImmediately(arg0);
   }

	@Override
   public void paintImmediately(int arg0, int arg1, int arg2, int arg3)
   {
	   warnIfOffEventThread();
	   super.paintImmediately(arg0, arg1, arg2, arg3);
   }

	@Override
   public void print(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.print(arg0);
   }

	@Override
   public void printAll(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.printAll(arg0);
   }

	@Override
   protected void printBorder(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.printBorder(arg0);
   }

	@Override
   protected void printChildren(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.printChildren(arg0);
   }

	@Override
   protected void printComponent(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.printComponent(arg0);
   }

	@Override
   protected void processComponentKeyEvent(KeyEvent arg0)
   {
	   warnIfOffEventThread();
	   super.processComponentKeyEvent(arg0);
   }

	@Override
   protected boolean processKeyBinding(KeyStroke arg0, KeyEvent arg1, int arg2,
         boolean arg3)
   {
	   warnIfOffEventThread();
	   return super.processKeyBinding(arg0, arg1, arg2, arg3);
   }

	@Override
   protected void processKeyEvent(KeyEvent arg0)
   {
	   warnIfOffEventThread();
	   super.processKeyEvent(arg0);
   }

	@Override
   protected void processMouseEvent(MouseEvent arg0)
   {
	   warnIfOffEventThread();
	   super.processMouseEvent(arg0);
   }

	@Override
   protected void processMouseMotionEvent(MouseEvent arg0)
   {
	   warnIfOffEventThread();
	   super.processMouseMotionEvent(arg0);
   }

	@Override
   public void registerKeyboardAction(ActionListener arg0, KeyStroke arg1,
         int arg2)
   {
	   warnIfOffEventThread();
	   super.registerKeyboardAction(arg0, arg1, arg2);
   }

	@Override
   public void registerKeyboardAction(ActionListener arg0, String arg1,
         KeyStroke arg2, int arg3)
   {
	   warnIfOffEventThread();
	   super.registerKeyboardAction(arg0, arg1, arg2, arg3);
   }

	@Override
   public void removeAncestorListener(AncestorListener arg0)
   {
	   warnIfOffEventThread();
	   super.removeAncestorListener(arg0);
   }

	@Override
   public void removeNotify()
   {
	   warnIfOffEventThread();
	   super.removeNotify();
   }

	@Override
   public synchronized void removeVetoableChangeListener(
         VetoableChangeListener arg0)
   {
	   warnIfOffEventThread();
	   super.removeVetoableChangeListener(arg0);
   }

	@Override
   public void repaint(Rectangle arg0)
   {
	   warnIfOffEventThread();
	   super.repaint(arg0);
   }

	@Override
   public void repaint(long arg0, int arg1, int arg2, int arg3, int arg4)
   {
	   warnIfOffEventThread();
	   super.repaint(arg0, arg1, arg2, arg3, arg4);
   }

	@Override
   public boolean requestDefaultFocus()
   {
	   warnIfOffEventThread();
	   return super.requestDefaultFocus();
   }

	@Override
   public void requestFocus()
   {
	   warnIfOffEventThread();
	   super.requestFocus();
   }

	@Override
   public boolean requestFocus(boolean arg0)
   {
	   warnIfOffEventThread();
	   return super.requestFocus(arg0);
   }

	@Override
   public boolean requestFocusInWindow()
   {
	   warnIfOffEventThread();
	   return super.requestFocusInWindow();
   }

	@Override
   protected boolean requestFocusInWindow(boolean arg0)
   {
	   warnIfOffEventThread();
	   return super.requestFocusInWindow(arg0);
   }

	@Override
   public void resetKeyboardActions()
   {
	   warnIfOffEventThread();
	   super.resetKeyboardActions();
   }

	@Override
   public void reshape(int arg0, int arg1, int arg2, int arg3)
   {
	   warnIfOffEventThread();
	   super.reshape(arg0, arg1, arg2, arg3);
   }

	@Override
   public void revalidate()
   {
	   warnIfOffEventThread();
	   super.revalidate();
   }

	@Override
   public void scrollRectToVisible(Rectangle arg0)
   {
	   warnIfOffEventThread();
	   super.scrollRectToVisible(arg0);
   }

	@Override
   public void setAlignmentX(float arg0)
   {
	   warnIfOffEventThread();
	   super.setAlignmentX(arg0);
   }

	@Override
   public void setAlignmentY(float arg0)
   {
	   warnIfOffEventThread();
	   super.setAlignmentY(arg0);
   }

	@Override
   public void setAutoscrolls(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.setAutoscrolls(arg0);
   }

	@Override
   public void setBackground(Color arg0)
   {
	   warnIfOffEventThread();
	   super.setBackground(arg0);
   }

	@Override
   public void setBorder(Border arg0)
   {
	   warnIfOffEventThread();
	   super.setBorder(arg0);
   }

	@Override
   public void setComponentPopupMenu(JPopupMenu arg0)
   {
	   warnIfOffEventThread();
	   super.setComponentPopupMenu(arg0);
   }

	@Override
   public void setDebugGraphicsOptions(int arg0)
   {
	   warnIfOffEventThread();
	   super.setDebugGraphicsOptions(arg0);
   }

	@Override
   public void setDoubleBuffered(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.setDoubleBuffered(arg0);
   }

	@Override
   public void setEnabled(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.setEnabled(arg0);
   }

	@Override
   public void setFocusTraversalKeys(int arg0, Set<? extends AWTKeyStroke> arg1)
   {
	   warnIfOffEventThread();
	   super.setFocusTraversalKeys(arg0, arg1);
   }

	@Override
   public void setFont(Font arg0)
   {
	   warnIfOffEventThread();
	   super.setFont(arg0);
   }

	@Override
   public void setForeground(Color arg0)
   {
	   warnIfOffEventThread();
	   super.setForeground(arg0);
   }

	@Override
   public void setInheritsPopupMenu(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.setInheritsPopupMenu(arg0);
   }

	@Override
   public void setInputVerifier(InputVerifier arg0)
   {
	   warnIfOffEventThread();
	   super.setInputVerifier(arg0);
   }

	@Override
   public void setMaximumSize(Dimension arg0)
   {
	   warnIfOffEventThread();
	   super.setMaximumSize(arg0);
   }

	@Override
   public void setMinimumSize(Dimension arg0)
   {
	   warnIfOffEventThread();
	   super.setMinimumSize(arg0);
   }

	@Override
   public void setNextFocusableComponent(Component arg0)
   {
	   warnIfOffEventThread();
	   super.setNextFocusableComponent(arg0);
   }

	@Override
   public void setOpaque(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.setOpaque(arg0);
   }

	@Override
   public void setPreferredSize(Dimension arg0)
   {
	   warnIfOffEventThread();
	   super.setPreferredSize(arg0);
   }

	@Override
   public void setRequestFocusEnabled(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.setRequestFocusEnabled(arg0);
   }

	@Override
   public void setToolTipText(String arg0)
   {
	   warnIfOffEventThread();
	   super.setToolTipText(arg0);
   }

	@Override
   public void setTransferHandler(TransferHandler arg0)
   {
	   warnIfOffEventThread();
	   super.setTransferHandler(arg0);
   }

	@Override
   protected void setUI(ComponentUI arg0)
   {
	   warnIfOffEventThread();
	   super.setUI(arg0);
   }

	@Override
   public void setVerifyInputWhenFocusTarget(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.setVerifyInputWhenFocusTarget(arg0);
   }

	@Override
   public void setVisible(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.setVisible(arg0);
   }

	@Override
   public void unregisterKeyboardAction(KeyStroke arg0)
   {
	   warnIfOffEventThread();
	   super.unregisterKeyboardAction(arg0);
   }

	@Override
   public void update(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.update(arg0);
   }

	@Override
   public Component add(Component arg0)
   {
	   warnIfOffEventThread();
	   return super.add(arg0);
   }

	@Override
   public Component add(String arg0, Component arg1)
   {
	   warnIfOffEventThread();
	   return super.add(arg0, arg1);
   }

	@Override
   public Component add(Component arg0, int arg1)
   {
	   warnIfOffEventThread();
	   return super.add(arg0, arg1);
   }

	@Override
   public void add(Component arg0, Object arg1)
   {
	   warnIfOffEventThread();
	   super.add(arg0, arg1);
   }

	@Override
   public void add(Component arg0, Object arg1, int arg2)
   {
	   warnIfOffEventThread();
	   super.add(arg0, arg1, arg2);
   }

	@Override
   public synchronized void addContainerListener(ContainerListener arg0)
   {
	   warnIfOffEventThread();
	   super.addContainerListener(arg0);
   }

	@Override
   protected void addImpl(Component arg0, Object arg1, int arg2)
   {
	   warnIfOffEventThread();
	   super.addImpl(arg0, arg1, arg2);
   }

	@Override
   public void addPropertyChangeListener(PropertyChangeListener arg0)
   {
	   warnIfOffEventThread();
	   super.addPropertyChangeListener(arg0);
   }

	@Override
   public void addPropertyChangeListener(String arg0,
         PropertyChangeListener arg1)
   {
	   warnIfOffEventThread();
	   super.addPropertyChangeListener(arg0, arg1);
   }

	@Override
   public void applyComponentOrientation(ComponentOrientation arg0)
   {
	   warnIfOffEventThread();
	   super.applyComponentOrientation(arg0);
   }

	@Override
   public boolean areFocusTraversalKeysSet(int arg0)
   {
	   warnIfOffEventThread();
	   return super.areFocusTraversalKeysSet(arg0);
   }

	@Override
   public int countComponents()
   {
	   warnIfOffEventThread();
	   return super.countComponents();
   }

	@Override
   public void deliverEvent(Event arg0)
   {
	   warnIfOffEventThread();
	   super.deliverEvent(arg0);
   }

	@Override
   public void doLayout()
   {
	   warnIfOffEventThread();
	   super.doLayout();
   }

	@Override
   public Component findComponentAt(Point arg0)
   {
	   warnIfOffEventThread();
	   return super.findComponentAt(arg0);
   }

	@Override
   public Component findComponentAt(int arg0, int arg1)
   {
	   warnIfOffEventThread();
	   return super.findComponentAt(arg0, arg1);
   }

	@Override
   public Component getComponent(int arg0)
   {
	   warnIfOffEventThread();
	   return super.getComponent(arg0);
   }

	@Override
   public Component getComponentAt(Point arg0)
   {
	   warnIfOffEventThread();
	   return super.getComponentAt(arg0);
   }

	@Override
   public Component getComponentAt(int arg0, int arg1)
   {
	   warnIfOffEventThread();
	   return super.getComponentAt(arg0, arg1);
   }

	@Override
   public int getComponentCount()
   {
	   warnIfOffEventThread();
	   return super.getComponentCount();
   }

	@Override
   public int getComponentZOrder(Component arg0)
   {
	   warnIfOffEventThread();
	   return super.getComponentZOrder(arg0);
   }

	@Override
   public Component[] getComponents()
   {
	   warnIfOffEventThread();
	   return super.getComponents();
   }

	@Override
   public synchronized ContainerListener[] getContainerListeners()
   {
	   warnIfOffEventThread();
	   return super.getContainerListeners();
   }

	@Override
   public Set<AWTKeyStroke> getFocusTraversalKeys(int arg0)
   {
	   warnIfOffEventThread();
	   return super.getFocusTraversalKeys(arg0);
   }

	@Override
   public FocusTraversalPolicy getFocusTraversalPolicy()
   {
	   warnIfOffEventThread();
	   return super.getFocusTraversalPolicy();
   }

	@Override
   public LayoutManager getLayout()
   {
	   warnIfOffEventThread();
	   return super.getLayout();
   }

	@Override
   public Point getMousePosition(boolean arg0) throws HeadlessException
   {
	   warnIfOffEventThread();
	   return super.getMousePosition(arg0);
   }

	@Override
   public Insets insets()
   {
	   warnIfOffEventThread();
	   return super.insets();
   }

	@Override
   public void invalidate()
   {
	   warnIfOffEventThread();
	   super.invalidate();
   }

	@Override
   public boolean isAncestorOf(Component arg0)
   {
	   warnIfOffEventThread();
	   return super.isAncestorOf(arg0);
   }

	@Override
   public boolean isFocusCycleRoot()
   {
	   warnIfOffEventThread();
	   return super.isFocusCycleRoot();
   }

	@Override
   public boolean isFocusCycleRoot(Container arg0)
   {
	   warnIfOffEventThread();
	   return super.isFocusCycleRoot(arg0);
   }

	@Override
   public boolean isFocusTraversalPolicySet()
   {
	   warnIfOffEventThread();
	   return super.isFocusTraversalPolicySet();
   }

	@Override
   public void layout()
   {
	   warnIfOffEventThread();
	   super.layout();
   }

	@Override
   public void list(PrintStream arg0, int arg1)
   {
	   warnIfOffEventThread();
	   super.list(arg0, arg1);
   }

	@Override
   public void list(PrintWriter arg0, int arg1)
   {
	   warnIfOffEventThread();
	   super.list(arg0, arg1);
   }

	@Override
   public Component locate(int arg0, int arg1)
   {
	   warnIfOffEventThread();
	   return super.locate(arg0, arg1);
   }

	@Override
   public Dimension minimumSize()
   {
	   warnIfOffEventThread();
	   return super.minimumSize();
   }

	@Override
   public void paintComponents(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.paintComponents(arg0);
   }

	@Override
   public Dimension preferredSize()
   {
	   warnIfOffEventThread();
	   return super.preferredSize();
   }

	@Override
   public void printComponents(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.printComponents(arg0);
   }

	@Override
   protected void processContainerEvent(ContainerEvent arg0)
   {
	   warnIfOffEventThread();
	   super.processContainerEvent(arg0);
   }

	@Override
   protected void processEvent(AWTEvent arg0)
   {
	   warnIfOffEventThread();
	   super.processEvent(arg0);
   }

	@Override
   public void remove(int arg0)
   {
	   warnIfOffEventThread();
	   super.remove(arg0);
   }

	@Override
   public void remove(Component arg0)
   {
	   warnIfOffEventThread();
	   super.remove(arg0);
   }

	@Override
   public void removeAll()
   {
	   warnIfOffEventThread();
	   super.removeAll();
   }

	@Override
   public synchronized void removeContainerListener(ContainerListener arg0)
   {
	   warnIfOffEventThread();
	   super.removeContainerListener(arg0);
   }

	@Override
   public void setComponentZOrder(Component arg0, int arg1)
   {
	   warnIfOffEventThread();
	   super.setComponentZOrder(arg0, arg1);
   }

	@Override
   public void setFocusCycleRoot(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.setFocusCycleRoot(arg0);
   }

	@Override
   public void setFocusTraversalPolicy(FocusTraversalPolicy arg0)
   {
	   warnIfOffEventThread();
	   super.setFocusTraversalPolicy(arg0);
   }

	@Override
   public void setLayout(LayoutManager arg0)
   {
	   warnIfOffEventThread();
	   super.setLayout(arg0);
   }

	@Override
   public void transferFocusDownCycle()
   {
	   warnIfOffEventThread();
	   super.transferFocusDownCycle();
   }

	@Override
   public void validate()
   {
	   warnIfOffEventThread();
	   super.validate();
   }

	@Override
   protected void validateTree()
   {
	   warnIfOffEventThread();
	   super.validateTree();
   }

   @Override
   public boolean action(Event arg0, Object arg1)
   {
	   warnIfOffEventThread();
	   return super.action(arg0, arg1);
   }

	@Override
   public void add(PopupMenu arg0)
   {
	   warnIfOffEventThread();
	   super.add(arg0);
   }

	@Override
   public synchronized void addComponentListener(ComponentListener arg0)
   {
	   warnIfOffEventThread();
	   super.addComponentListener(arg0);
   }

	@Override
   public synchronized void addFocusListener(FocusListener arg0)
   {
	   warnIfOffEventThread();
	   super.addFocusListener(arg0);
   }

	@Override
   public void addHierarchyBoundsListener(HierarchyBoundsListener arg0)
   {
	   warnIfOffEventThread();
	   super.addHierarchyBoundsListener(arg0);
   }

	@Override
   public void addHierarchyListener(HierarchyListener arg0)
   {
	   warnIfOffEventThread();
	   super.addHierarchyListener(arg0);
   }

	@Override
   public synchronized void addInputMethodListener(InputMethodListener arg0)
   {
	   warnIfOffEventThread();
	   super.addInputMethodListener(arg0);
   }

	@Override
   public synchronized void addKeyListener(KeyListener arg0)
   {
	   warnIfOffEventThread();
	   super.addKeyListener(arg0);
   }

	@Override
   public synchronized void addMouseListener(MouseListener arg0)
   {
	   warnIfOffEventThread();
	   super.addMouseListener(arg0);
   }

	@Override
   public synchronized void addMouseMotionListener(MouseMotionListener arg0)
   {
	   warnIfOffEventThread();
	   super.addMouseMotionListener(arg0);
   }

	@Override
   public synchronized void addMouseWheelListener(MouseWheelListener arg0)
   {
	   warnIfOffEventThread();
	   super.addMouseWheelListener(arg0);
   }

	@Override
   public Rectangle bounds()
   {
	   warnIfOffEventThread();
	   return super.bounds();
   }

	@Override
   public int checkImage(Image arg0, ImageObserver arg1)
   {
	   warnIfOffEventThread();
	   return super.checkImage(arg0, arg1);
   }

	@Override
   public int checkImage(Image arg0, int arg1, int arg2, ImageObserver arg3)
   {
	   warnIfOffEventThread();
	   return super.checkImage(arg0, arg1, arg2, arg3);
   }

	@Override
   protected AWTEvent coalesceEvents(AWTEvent arg0, AWTEvent arg1)
   {
	   warnIfOffEventThread();
	   return super.coalesceEvents(arg0, arg1);
   }

	@Override
   public boolean contains(Point arg0)
   {
	   warnIfOffEventThread();
	   return super.contains(arg0);
   }

	@Override
   public Image createImage(ImageProducer arg0)
   {
	   warnIfOffEventThread();
	   return super.createImage(arg0);
   }

	@Override
   public Image createImage(int arg0, int arg1)
   {
	   warnIfOffEventThread();
	   return super.createImage(arg0, arg1);
   }

	@Override
   public VolatileImage createVolatileImage(int arg0, int arg1)
   {
	   warnIfOffEventThread();
	   return super.createVolatileImage(arg0, arg1);
   }

	@Override
   public VolatileImage createVolatileImage(int arg0, int arg1,
         ImageCapabilities arg2) throws AWTException
   {
	   warnIfOffEventThread();
	   return super.createVolatileImage(arg0, arg1, arg2);
   }

	@Override
   public void enable(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.enable(arg0);
   }

	@Override
   public void enableInputMethods(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.enableInputMethods(arg0);
   }

	@Override
   protected void firePropertyChange(String arg0, Object arg1, Object arg2)
   {
	   warnIfOffEventThread();
	   super.firePropertyChange(arg0, arg1, arg2);
   }

	@Override
   public void firePropertyChange(String arg0, byte arg1, byte arg2)
   {
	   warnIfOffEventThread();
	   super.firePropertyChange(arg0, arg1, arg2);
   }

	@Override
   public void firePropertyChange(String arg0, short arg1, short arg2)
   {
	   warnIfOffEventThread();
	   super.firePropertyChange(arg0, arg1, arg2);
   }

	@Override
   public void firePropertyChange(String arg0, long arg1, long arg2)
   {
	   warnIfOffEventThread();
	   super.firePropertyChange(arg0, arg1, arg2);
   }

	@Override
   public void firePropertyChange(String arg0, float arg1, float arg2)
   {
	   warnIfOffEventThread();
	   super.firePropertyChange(arg0, arg1, arg2);
   }

	@Override
   public void firePropertyChange(String arg0, double arg1, double arg2)
   {
	   warnIfOffEventThread();
	   super.firePropertyChange(arg0, arg1, arg2);
   }

	@Override
   public Color getBackground()
   {
	   warnIfOffEventThread();
	   return super.getBackground();
   }

	@Override
   public Rectangle getBounds()
   {
	   warnIfOffEventThread();
	   return super.getBounds();
   }

	@Override
   public ColorModel getColorModel()
   {
	   warnIfOffEventThread();
	   return super.getColorModel();
   }

	@Override
   public synchronized ComponentListener[] getComponentListeners()
   {
	   warnIfOffEventThread();
	   return super.getComponentListeners();
   }

	@Override
   public ComponentOrientation getComponentOrientation()
   {
	   warnIfOffEventThread();
	   return super.getComponentOrientation();
   }

	@Override
   public Cursor getCursor()
   {
	   warnIfOffEventThread();
	   return super.getCursor();
   }

	@Override
   public synchronized DropTarget getDropTarget()
   {
	   warnIfOffEventThread();
	   return super.getDropTarget();
   }

	@Override
   public Container getFocusCycleRootAncestor()
   {
	   warnIfOffEventThread();
	   return super.getFocusCycleRootAncestor();
   }

	@Override
   public synchronized FocusListener[] getFocusListeners()
   {
	   warnIfOffEventThread();
	   return super.getFocusListeners();
   }

	@Override
   public boolean getFocusTraversalKeysEnabled()
   {
	   warnIfOffEventThread();
	   return super.getFocusTraversalKeysEnabled();
   }

	@Override
   public Font getFont()
   {
	   warnIfOffEventThread();
	   return super.getFont();
   }

	@Override
   public Color getForeground()
   {
	   warnIfOffEventThread();
	   return super.getForeground();
   }

	@Override
   public GraphicsConfiguration getGraphicsConfiguration()
   {
	   warnIfOffEventThread();
	   return super.getGraphicsConfiguration();
   }

	@Override
   public synchronized HierarchyBoundsListener[] getHierarchyBoundsListeners()
   {
	   warnIfOffEventThread();
	   return super.getHierarchyBoundsListeners();
   }

	@Override
   public synchronized HierarchyListener[] getHierarchyListeners()
   {
	   warnIfOffEventThread();
	   return super.getHierarchyListeners();
   }

	@Override
   public boolean getIgnoreRepaint()
   {
	   warnIfOffEventThread();
	   return super.getIgnoreRepaint();
   }

	@Override
   public InputContext getInputContext()
   {
	   warnIfOffEventThread();
	   return super.getInputContext();
   }

	@Override
   public synchronized InputMethodListener[] getInputMethodListeners()
   {
	   warnIfOffEventThread();
	   return super.getInputMethodListeners();
   }

	@Override
   public InputMethodRequests getInputMethodRequests()
   {
	   warnIfOffEventThread();
	   return super.getInputMethodRequests();
   }

	@Override
   public synchronized KeyListener[] getKeyListeners()
   {
	   warnIfOffEventThread();
	   return super.getKeyListeners();
   }

	@Override
   public Locale getLocale()
   {
	   warnIfOffEventThread();
	   return super.getLocale();
   }

	@Override
   public Point getLocation()
   {
	   warnIfOffEventThread();
	   return super.getLocation();
   }

	@Override
   public Point getLocationOnScreen()
   {
	   warnIfOffEventThread();
	   return super.getLocationOnScreen();
   }

	@Override
   public synchronized MouseListener[] getMouseListeners()
   {
	   warnIfOffEventThread();
	   return super.getMouseListeners();
   }

	@Override
   public synchronized MouseMotionListener[] getMouseMotionListeners()
   {
	   warnIfOffEventThread();
	   return super.getMouseMotionListeners();
   }

	@Override
   public Point getMousePosition() throws HeadlessException
   {
	   warnIfOffEventThread();
	   return super.getMousePosition();
   }

	@Override
   public synchronized MouseWheelListener[] getMouseWheelListeners()
   {
	   warnIfOffEventThread();
	   return super.getMouseWheelListeners();
   }

	@Override
   public String getName()
   {
	   warnIfOffEventThread();
	   return super.getName();
   }

	@Override
   public Container getParent()
   {
	   warnIfOffEventThread();
	   return super.getParent();
   }

	@Override
   public ComponentPeer getPeer()
   {
	   warnIfOffEventThread();
	   return super.getPeer();
   }

	@Override
   public PropertyChangeListener[] getPropertyChangeListeners()
   {
	   warnIfOffEventThread();
	   return super.getPropertyChangeListeners();
   }

	@Override
   public PropertyChangeListener[] getPropertyChangeListeners(String arg0)
   {
	   warnIfOffEventThread();
	   return super.getPropertyChangeListeners(arg0);
   }

	@Override
   public Dimension getSize()
   {
	   warnIfOffEventThread();
	   return super.getSize();
   }

	@Override
   public Toolkit getToolkit()
   {
	   warnIfOffEventThread();
	   return super.getToolkit();
   }

	@Override
   public boolean gotFocus(Event arg0, Object arg1)
   {
	   warnIfOffEventThread();
	   return super.gotFocus(arg0, arg1);
   }

	@Override
   public boolean handleEvent(Event arg0)
   {
	   warnIfOffEventThread();
	   return super.handleEvent(arg0);
   }

	@Override
   public boolean hasFocus()
   {
	   warnIfOffEventThread();
	   return super.hasFocus();
   }

	@Override
   public void hide()
   {
	   warnIfOffEventThread();
	   super.hide();
   }

	@Override
   public boolean inside(int arg0, int arg1)
   {
	   warnIfOffEventThread();
	   return super.inside(arg0, arg1);
   }

	@Override
   public boolean isBackgroundSet()
   {
	   warnIfOffEventThread();
	   return super.isBackgroundSet();
   }

	@Override
   public boolean isCursorSet()
   {
	   warnIfOffEventThread();
	   return super.isCursorSet();
   }

	@Override
   public boolean isDisplayable()
   {
	   warnIfOffEventThread();
	   return super.isDisplayable();
   }

	@Override
   public boolean isEnabled()
   {
	   warnIfOffEventThread();
	   return super.isEnabled();
   }

	@Override
   public boolean isFocusOwner()
   {
	   warnIfOffEventThread();
	   return super.isFocusOwner();
   }

	@Override
   public boolean isFocusTraversable()
   {
	   warnIfOffEventThread();
	   return super.isFocusTraversable();
   }

	@Override
   public boolean isFocusable()
   {
	   warnIfOffEventThread();
	   return super.isFocusable();
   }

	@Override
   public boolean isFontSet()
   {
	   warnIfOffEventThread();
	   return super.isFontSet();
   }

	@Override
   public boolean isForegroundSet()
   {
	   warnIfOffEventThread();
	   return super.isForegroundSet();
   }

	@Override
   public boolean isLightweight()
   {
	   warnIfOffEventThread();
	   return super.isLightweight();
   }

	@Override
   public boolean isMaximumSizeSet()
   {
	   warnIfOffEventThread();
	   return super.isMaximumSizeSet();
   }

	@Override
   public boolean isMinimumSizeSet()
   {
	   warnIfOffEventThread();
	   return super.isMinimumSizeSet();
   }

	@Override
   public boolean isPreferredSizeSet()
   {
	   warnIfOffEventThread();
	   return super.isPreferredSizeSet();
   }

	@Override
   public boolean isShowing()
   {
	   warnIfOffEventThread();
	   return super.isShowing();
   }

	@Override
   public boolean isValid()
   {
	   warnIfOffEventThread();
	   return super.isValid();
   }

	@Override
   public boolean isVisible()
   {
	   warnIfOffEventThread();
	   return super.isVisible();
   }

	@Override
   public boolean keyDown(Event arg0, int arg1)
   {
	   warnIfOffEventThread();
	   return super.keyDown(arg0, arg1);
   }

	@Override
   public boolean keyUp(Event arg0, int arg1)
   {
	   warnIfOffEventThread();
	   return super.keyUp(arg0, arg1);
   }

	@Override
   public void list()
   {
	   warnIfOffEventThread();
	   super.list();
   }

	@Override
   public void list(PrintStream arg0)
   {
	   warnIfOffEventThread();
	   super.list(arg0);
   }

	@Override
   public void list(PrintWriter arg0)
   {
	   warnIfOffEventThread();
	   super.list(arg0);
   }

	@Override
   public Point location()
   {
	   warnIfOffEventThread();
	   return super.location();
   }

	@Override
   public boolean lostFocus(Event arg0, Object arg1)
   {
	   warnIfOffEventThread();
	   return super.lostFocus(arg0, arg1);
   }

	@Override
   public boolean mouseDown(Event arg0, int arg1, int arg2)
   {
	   warnIfOffEventThread();
	   return super.mouseDown(arg0, arg1, arg2);
   }

	@Override
   public boolean mouseDrag(Event arg0, int arg1, int arg2)
   {
	   warnIfOffEventThread();
	   return super.mouseDrag(arg0, arg1, arg2);
   }

	@Override
   public boolean mouseEnter(Event arg0, int arg1, int arg2)
   {
	   warnIfOffEventThread();
	   return super.mouseEnter(arg0, arg1, arg2);
   }

	@Override
   public boolean mouseExit(Event arg0, int arg1, int arg2)
   {
	   warnIfOffEventThread();
	   return super.mouseExit(arg0, arg1, arg2);
   }

	@Override
   public boolean mouseMove(Event arg0, int arg1, int arg2)
   {
	   warnIfOffEventThread();
	   return super.mouseMove(arg0, arg1, arg2);
   }

	@Override
   public boolean mouseUp(Event arg0, int arg1, int arg2)
   {
	   warnIfOffEventThread();
	   return super.mouseUp(arg0, arg1, arg2);
   }

	@Override
   public void move(int arg0, int arg1)
   {
	   warnIfOffEventThread();
	   super.move(arg0, arg1);
   }

	@Override
   public void nextFocus()
   {
	   warnIfOffEventThread();
	   super.nextFocus();
   }

	@Override
   public void paintAll(Graphics arg0)
   {
	   warnIfOffEventThread();
	   super.paintAll(arg0);
   }

	@Override
   public boolean postEvent(Event arg0)
   {
	   warnIfOffEventThread();
	   return super.postEvent(arg0);
   }

	@Override
   public boolean prepareImage(Image arg0, ImageObserver arg1)
   {
	   warnIfOffEventThread();
	   return super.prepareImage(arg0, arg1);
   }

	@Override
   public boolean prepareImage(Image arg0, int arg1, int arg2,
         ImageObserver arg3)
   {
	   warnIfOffEventThread();
	   return super.prepareImage(arg0, arg1, arg2, arg3);
   }

	@Override
   protected void processComponentEvent(ComponentEvent arg0)
   {
	   warnIfOffEventThread();
	   super.processComponentEvent(arg0);
   }

	@Override
   protected void processFocusEvent(FocusEvent arg0)
   {
	   warnIfOffEventThread();
	   super.processFocusEvent(arg0);
   }

	@Override
   protected void processHierarchyBoundsEvent(HierarchyEvent arg0)
   {
	   warnIfOffEventThread();
	   super.processHierarchyBoundsEvent(arg0);
   }

	@Override
   protected void processHierarchyEvent(HierarchyEvent arg0)
   {
	   warnIfOffEventThread();
	   super.processHierarchyEvent(arg0);
   }

	@Override
   protected void processInputMethodEvent(InputMethodEvent arg0)
   {
	   warnIfOffEventThread();
	   super.processInputMethodEvent(arg0);
   }

	@Override
   protected void processMouseWheelEvent(MouseWheelEvent arg0)
   {
	   warnIfOffEventThread();
	   super.processMouseWheelEvent(arg0);
   }

	@Override
   public void remove(MenuComponent arg0)
   {
	   warnIfOffEventThread();
	   super.remove(arg0);
   }

	@Override
   public synchronized void removeComponentListener(ComponentListener arg0)
   {
	   warnIfOffEventThread();
	   super.removeComponentListener(arg0);
   }

	@Override
   public synchronized void removeFocusListener(FocusListener arg0)
   {
	   warnIfOffEventThread();
	   super.removeFocusListener(arg0);
   }

	@Override
   public void removeHierarchyBoundsListener(HierarchyBoundsListener arg0)
   {
	   warnIfOffEventThread();
	   super.removeHierarchyBoundsListener(arg0);
   }

	@Override
   public void removeHierarchyListener(HierarchyListener arg0)
   {
	   warnIfOffEventThread();
	   super.removeHierarchyListener(arg0);
   }

	@Override
   public synchronized void removeInputMethodListener(InputMethodListener arg0)
   {
	   warnIfOffEventThread();
	   super.removeInputMethodListener(arg0);
   }

	@Override
   public synchronized void removeKeyListener(KeyListener arg0)
   {
	   warnIfOffEventThread();
	   super.removeKeyListener(arg0);
   }

	@Override
   public synchronized void removeMouseListener(MouseListener arg0)
   {
	   warnIfOffEventThread();
	   super.removeMouseListener(arg0);
   }

	@Override
   public synchronized void removeMouseMotionListener(MouseMotionListener arg0)
   {
	   warnIfOffEventThread();
	   super.removeMouseMotionListener(arg0);
   }

	@Override
   public synchronized void removeMouseWheelListener(MouseWheelListener arg0)
   {
	   warnIfOffEventThread();
	   super.removeMouseWheelListener(arg0);
   }

	@Override
   public void removePropertyChangeListener(PropertyChangeListener arg0)
   {
	   warnIfOffEventThread();
	   super.removePropertyChangeListener(arg0);
   }

	@Override
   public void removePropertyChangeListener(String arg0,
         PropertyChangeListener arg1)
   {
	   warnIfOffEventThread();
	   super.removePropertyChangeListener(arg0, arg1);
   }

	@Override
   public void repaint()
   {
	   warnIfOffEventThread();
	   super.repaint();
   }

	@Override
   public void repaint(long arg0)
   {
	   warnIfOffEventThread();
	   super.repaint(arg0);
   }

	@Override
   public void repaint(int arg0, int arg1, int arg2, int arg3)
   {
	   warnIfOffEventThread();
	   super.repaint(arg0, arg1, arg2, arg3);
   }

	@Override
   public void resize(Dimension arg0)
   {
	   warnIfOffEventThread();
	   super.resize(arg0);
   }

	@Override
   public void resize(int arg0, int arg1)
   {
	   warnIfOffEventThread();
	   super.resize(arg0, arg1);
   }

	@Override
   public void setBounds(Rectangle arg0)
   {
	   warnIfOffEventThread();
	   super.setBounds(arg0);
   }

	@Override
   public void setBounds(int arg0, int arg1, int arg2, int arg3)
   {
	   warnIfOffEventThread();
	   super.setBounds(arg0, arg1, arg2, arg3);
   }

	@Override
   public void setComponentOrientation(ComponentOrientation arg0)
   {
	   warnIfOffEventThread();
	   super.setComponentOrientation(arg0);
   }

	@Override
   public void setCursor(Cursor arg0)
   {
	   warnIfOffEventThread();
	   super.setCursor(arg0);
   }

	@Override
   public synchronized void setDropTarget(DropTarget arg0)
   {
	   warnIfOffEventThread();
	   super.setDropTarget(arg0);
   }

	@Override
   public void setFocusTraversalKeysEnabled(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.setFocusTraversalKeysEnabled(arg0);
   }

	@Override
   public void setFocusable(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.setFocusable(arg0);
   }

	@Override
   public void setIgnoreRepaint(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.setIgnoreRepaint(arg0);
   }

	@Override
   public void setLocale(Locale arg0)
   {
	   warnIfOffEventThread();
	   super.setLocale(arg0);
   }

	@Override
   public void setLocation(Point arg0)
   {
	   warnIfOffEventThread();
	   super.setLocation(arg0);
   }

	@Override
   public void setLocation(int arg0, int arg1)
   {
	   warnIfOffEventThread();
	   super.setLocation(arg0, arg1);
   }

	@Override
   public void setName(String arg0)
   {
	   warnIfOffEventThread();
	   super.setName(arg0);
   }

	@Override
   public void setSize(Dimension arg0)
   {
	   warnIfOffEventThread();
	   super.setSize(arg0);
   }

	@Override
   public void setSize(int arg0, int arg1)
   {
	   warnIfOffEventThread();
	   super.setSize(arg0, arg1);
   }

	@Override
   public void show()
   {
	   warnIfOffEventThread();
	   super.show();
   }

	@Override
   public void show(boolean arg0)
   {
	   warnIfOffEventThread();
	   super.show(arg0);
   }

	@Override
   public Dimension size()
   {
	   warnIfOffEventThread();
	   return super.size();
   }

	@Override
   public String toString()
   {
	   warnIfOffEventThread();
	   return super.toString();
   }

	@Override
   public void transferFocus()
   {
	   warnIfOffEventThread();
	   super.transferFocus();
   }

	@Override
   public void transferFocusBackward()
   {
	   warnIfOffEventThread();
	   super.transferFocusBackward();
   }

	@Override
   public void transferFocusUpCycle()
   {
	   warnIfOffEventThread();
	   super.transferFocusUpCycle();
   }
}
