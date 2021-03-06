/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.crashreporter;

import javax.swing.JDialog;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.terasology.crashreporter.GlobalProperties.KEY;

import java.awt.Dialog;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

/**
 * Displays a detailed error message and provides some options to communicate with devs.
 * Errors are reported to {@link System#err}
 */
public final class CrashReporter {

    private CrashReporter() {
        // don't create any instances
    }

    /**
     * Can be called from any thread.
     * @param throwable the exception to report
     * @param logFileFolder the log file folder or <code>null</code>
     */
    public static void report(final Throwable throwable, final Path logFileFolder) {
        // Swing element methods must be called in the swing thread
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    LookAndFeel oldLaF = UIManager.getLookAndFeel();
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    GlobalProperties properties = new GlobalProperties();
                    showModalDialog(throwable, properties, logFileFolder);
                    try {
                        UIManager.setLookAndFeel(oldLaF);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected static void showModalDialog(Throwable throwable, GlobalProperties properties, Path logFolder) {
        String dialogTitle = I18N.getMessage("dialogTitle");
        String version = Resources.getVersion();

        if (version != null) {
            dialogTitle += " " + version;
        }

        RootPanel panel = new RootPanel(throwable, properties, logFolder);
        JDialog dialog = new JDialog((Dialog) null, dialogTitle, true);
        dialog.setIconImage(Resources.loadImage(properties.get(KEY.RES_SERVER_ICON)));
        dialog.setContentPane(panel);
        dialog.setMinimumSize(new Dimension(600, 400));
        dialog.setLocationRelativeTo(null);
        dialog.setResizable(true);      // disabled by default
        dialog.setVisible(true);
        dialog.dispose();
    }
}
