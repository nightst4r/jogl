/**
 * Copyright 2013 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
 
package com.jogamp.opengl.test.junit.jogl.acore;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import jogamp.nativewindow.jawt.JAWTUtil;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jogamp.opengl.test.junit.jogl.demos.gl2.Gears;
import com.jogamp.opengl.test.junit.util.UITestCase;

public class TestAddRemove01GLCanvasSwingAWT extends UITestCase {
    static long durationPerTest = 50;
    static int addRemoveCount = 15;
    static boolean shallUseOffscreenFBOLayer = false;
    static boolean shallUseOffscreenPBufferLayer = false;
    static GLProfile glp;
    static int width, height;
    static boolean waitForKey = false;

    @BeforeClass
    public static void initClass() {
        if(GLProfile.isAvailable(GLProfile.GL2)) {
            glp = GLProfile.get(GLProfile.GL2);
            Assert.assertNotNull(glp);
            width  = 640;
            height = 480;
        } else {
            setTestSupported(false);
        }
    }

    @AfterClass
    public static void releaseClass() {
    }
    
    protected JPanel createParkingSlot(final JFrame[] top, final int width, final int height) 
            throws InterruptedException, InvocationTargetException 
    {
        final JPanel[] jPanel = new JPanel[] { null };
        SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    jPanel[0] = new JPanel();
                    jPanel[0].setLayout(new BorderLayout());
            
                    final JFrame jFrame1 = new JFrame("Parking Slot");
                    // jFrame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    jFrame1.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // equivalent to Frame, use windowClosing event!
                    jFrame1.getContentPane().add(jPanel[0]);
                    jFrame1.setSize(width, height);
                    
                    top[0] = jFrame1;
                } } );
        return jPanel[0];        
    }
    
    protected JPanel create(final JFrame[] top, final int width, final int height, final int num) 
            throws InterruptedException, InvocationTargetException 
    {
        final JPanel[] jPanel = new JPanel[] { null };
        SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    jPanel[0] = new JPanel();
                    jPanel[0].setLayout(new BorderLayout());

                    final JFrame jFrame1 = new JFrame("JFrame #"+num);
                    // jFrame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    jFrame1.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // equivalent to Frame, use windowClosing event!
                    jFrame1.getContentPane().add(jPanel[0]);
                    jFrame1.setSize(width, height);
                    
                    top[0] = jFrame1;
                } } );
        return jPanel[0];        
    }

    protected void add(final Container cont, final Component comp) 
            throws InterruptedException, InvocationTargetException 
    {
        SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    cont.add(comp, BorderLayout.CENTER);                    
                } } );
    }
    
    protected void dispose(final GLCanvas glc) 
            throws InterruptedException, InvocationTargetException 
    {
        SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    glc.destroy();                    
                } } );
    }
    
    protected void setVisible(final JFrame jFrame, final boolean visible) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    if( visible ) {
                        jFrame.validate();
                        jFrame.pack();
                    }
                    jFrame.setVisible(visible);
                } } ) ;        
    }
    
    protected void dispose(final JFrame jFrame) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    jFrame.dispose();
                } } ) ;        
    }
    
    protected void runTestGL(boolean onscreen, GLCapabilities caps, int addRemoveOpCount)
            throws AWTException, InterruptedException, InvocationTargetException
    {

        for(int i=0; i<addRemoveOpCount; i++) {
            final GLCanvas glc = new GLCanvas(caps);
            Assert.assertNotNull(glc);
            if( !onscreen ) {
                glc.setShallUseOffscreenLayer(true);
            }
            Dimension glc_sz = new Dimension(width, height);
            glc.setMinimumSize(glc_sz);
            glc.setPreferredSize(glc_sz);
            glc.setSize(glc_sz);
            glc.addGLEventListener(new Gears());
            
            final JFrame[] top = new JFrame[] { null };
            final Container glcCont = create(top, width, height, i);
            add(glcCont, glc);
            
            setVisible(top[0], true);
                        
            final long t0 = System.currentTimeMillis();
            do {
                glc.display();
                Thread.sleep(10);
            } while ( ( System.currentTimeMillis() - t0 ) < durationPerTest ) ;
            
            System.err.println("GLCanvas isOffscreenLayerSurfaceEnabled: "+glc.isOffscreenLayerSurfaceEnabled()+": "+glc.getChosenGLCapabilities());
            
            dispose(top[0]);            
        }
    }

    @Test
    public void test01Onscreen()
            throws AWTException, InterruptedException, InvocationTargetException
    {
        if( shallUseOffscreenFBOLayer || shallUseOffscreenPBufferLayer || JAWTUtil.isOffscreenLayerRequired() ) {
            System.err.println("Offscreen test requested or platform requires it.");
            return;
        }
        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        if(shallUseOffscreenPBufferLayer) {
            caps.setPBuffer(true);
            caps.setOnscreen(true); // simulate normal behavior ..
        }
        runTestGL(true, caps, addRemoveCount);
    }

    @Test
    public void test02Offscreen()
            throws AWTException, InterruptedException, InvocationTargetException
    {
        if( !JAWTUtil.isOffscreenLayerSupported() ) {
            System.err.println("Platform doesn't support offscreen test.");
            return;
        }
        GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());
        if(shallUseOffscreenPBufferLayer) {
            caps.setPBuffer(true);
            caps.setOnscreen(true); // simulate normal behavior ..
        }
        runTestGL(false, caps, addRemoveCount);
    }
    
    public static void main(String args[]) throws IOException {
        for(int i=0; i<args.length; i++) {
            if(args[i].equals("-time")) {
                i++;
                try {
                    durationPerTest = Long.parseLong(args[i]);
                } catch (Exception ex) { ex.printStackTrace(); }
            } else if(args[i].equals("-loops")) {
                i++;
                try {
                    addRemoveCount = Integer.parseInt(args[i]);
                } catch (Exception ex) { ex.printStackTrace(); }
            } else if(args[i].equals("-layeredFBO")) {
                shallUseOffscreenFBOLayer = true;
            } else if(args[i].equals("-layeredPBuffer")) {
                shallUseOffscreenPBufferLayer = true;
            } else if(args[i].equals("-wait")) {
                waitForKey = true;
            }            
        }
        System.err.println("waitForKey                    "+waitForKey);
        
        System.err.println("addRemoveCount                "+addRemoveCount);
        
        System.err.println("shallUseOffscreenFBOLayer     "+shallUseOffscreenFBOLayer);
        System.err.println("shallUseOffscreenPBufferLayer "+shallUseOffscreenPBufferLayer);
        if(waitForKey) {
            UITestCase.waitForKey("Start");
        }
        org.junit.runner.JUnitCore.main(TestAddRemove01GLCanvasSwingAWT.class.getName());
    }
}