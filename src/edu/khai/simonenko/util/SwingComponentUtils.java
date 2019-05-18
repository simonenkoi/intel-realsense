package edu.khai.simonenko.util;

import edu.khai.simonenko.Settings;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SwingComponentUtils {

    public static void createTextFieldPanel(String label, String startValue, String settingName, GridBagConstraints gridBagConstraints, int gridx,
                                            int gridy, JFrame frame) {
        JPanel panel = new JPanel();
        JLabel text = new JLabel(label);
        JTextField value = new JTextField(startValue, 5);
        JButton button = SwingComponentUtils.createButton(settingName, value, frame);

        panel.add(text);
        panel.add(value);
        panel.add(button);
        gridBagConstraints.gridx = gridx;
        gridBagConstraints.gridy = gridy;
        frame.add(panel, gridBagConstraints);
    }

    public static void createComboBoxPanel(String label, Object[] values, GridBagConstraints gridBagConstraints, int gridx,
                                           int gridy, JFrame frame) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel text = new JLabel(label);
        JComboBox<Object> uploadSettings = new JComboBox<>(values);
        uploadSettings.setSelectedItem("default");
        uploadSettings.addItemListener(e -> SettingsUtil.uploadSettings(new File("settings/" + e.getItem() + ".xml")));

        panel.add(text);
        panel.add(uploadSettings);
        gridBagConstraints.gridx = gridx;
        gridBagConstraints.gridy = gridy;
        frame.add(panel, gridBagConstraints);
    }

    public static void createCheckBox(String label, Boolean defaultValue, String settingName, GridBagConstraints gridBagConstraints, int gridX,
                                      int gridY, JFrame frame) {
        createCheckBox(label, defaultValue, settingName, gridBagConstraints, gridX, gridY, frame, null);
    }

    public static void createCheckBox(String label, Boolean defaultValue, String settingName, GridBagConstraints gridBagConstraints, int gridX,
                                      int gridY, JFrame frame, ItemListener event) {
        JCheckBox checkBox = new JCheckBox(label, null, defaultValue);

        checkBox.addItemListener(event != null ? event : e -> {
            try {
                SettingsUtil.setValueOfStringToField(Settings.class, Settings.class.getField(settingName),
                                                     String.valueOf(e.getStateChange() == ItemEvent.SELECTED));
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        });
        gridBagConstraints.gridx = gridX;
        gridBagConstraints.gridy = gridY;
        frame.add(checkBox, gridBagConstraints);
    }

    public static JButton createButton(String name, JTextField input, JFrame frame) {
        JButton button = new JButton("✓");
        button.addActionListener(e -> {
            try {
                SettingsUtil.setValueOfStringToField(Settings.class, Settings.class.getField(name), input.getText());
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                JOptionPane.showMessageDialog(frame, "Verify that the entered data is correct.", "Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        });
        button.setFont(new Font("Dialog", Font.PLAIN, 8));
        button.setPreferredSize(new Dimension(18, 18));
        button.setMargin(new Insets(0, 0, 0, 0));
        return button;
    }
}
