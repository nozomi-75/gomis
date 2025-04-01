package test;

import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JButton;

import lyfjshs.gomis.test.simple.SimpleInputForms;
import lyfjshs.gomis.test.simple.SimpleInputForms2;
import net.miginfocom.swing.MigLayout;
import raven.extras.SlidePane;
import raven.extras.SlidePaneTransition;
import test.base.BaseFrame;

public class TestSlidePane extends BaseFrame {

    private int state = 1;

    public TestSlidePane() {
        super("Test Slide");
        setLayout(new MigLayout("wrap,al center", "[center]"));
        JButton button = new JButton("slide");

        SlidePane slidePane = new SlidePane((container, component) -> component.getPreferredSize());
        slidePane.addSlide(new SimpleInputForms());
        slidePane.setOpaque(true);

        button.addActionListener(e -> {
            Component com = state % 2 == 0 ? new SimpleInputForms() : new SimpleInputForms2();
            state++;
            slidePane.addSlide(com, SlidePaneTransition.Type.FORWARD);
        });

        add(slidePane);
        add(button);
    }

    public static void main(String[] args) {
        installLaf();
        EventQueue.invokeLater(() -> new TestSlidePane().setVisible(true));
    }
}
