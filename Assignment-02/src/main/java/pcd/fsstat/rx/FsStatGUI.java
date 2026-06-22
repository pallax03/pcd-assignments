package pcd.fsstat.rx;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.vertx.core.json.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FsStatGUI extends JFrame {
    Disposable generatingReports;

    public FsStatGUI(final FsStatLib lib) {
        super("Optional: Rx + Swing");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panelTop = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JLabel DLabel = new JLabel("Dir to scan (starting from project root): ");
        JTextField D = new JTextField(".");
        D.setColumns(15);
        inputPanel.add(wrapInputField(DLabel, D));

        JLabel MaxFSLabel = new JLabel("MaxFS (x * 1KB):");
        JTextField MaxFS = new JTextField("2");
        MaxFS.setColumns(5);
        inputPanel.add(wrapInputField(MaxFSLabel, MaxFS));

        JLabel NBLabel = new JLabel("NB:");
        JTextField NB = new JTextField("4");
        NB.setColumns(3);
        inputPanel.add(wrapInputField(NBLabel, NB));

        panelTop.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnStart = new JButton("Start");
        JButton btnStop = new JButton("Stop");
        btnStop.setEnabled(false);
        buttonPanel.add(btnStart);
        buttonPanel.add(btnStop);
        
        panelTop.add(buttonPanel, BorderLayout.SOUTH);

        BarChartPanel chart = new BarChartPanel();

        getContentPane().add(panelTop, BorderLayout.NORTH);
        getContentPane().add(chart, BorderLayout.CENTER);

        btnStart.addActionListener((ActionEvent ev) -> {
            toggleButtons(btnStart, btnStop);
            
            generatingReports = lib
                    .getReport(D.getText(), (1024L * Integer.parseInt(MaxFS.getText())), Integer.parseInt(NB.getText()))
                    .sample(20, TimeUnit.MILLISECONDS, true)
                    .observeOn(Schedulers.from(SwingUtilities::invokeLater))
                    .subscribe(
                        r -> {
                            List<String> labels = new ArrayList<>();
                            List<Integer> values = new ArrayList<>();
                            r.getReport().stream().map(item -> (JsonObject)item).forEach(item -> {
                                labels.add(item.getString("ranges"));
                                values.add(item.getInteger("counted"));
                            });
                            chart.updateChart(labels, values);
                        },
                        error -> {
                            System.err.println("error:\n" + error.getMessage());
                            toggleButtons(btnStart, btnStop);
                        },
                        () -> {
                            System.out.println(D.getText() + " scanned");
                            toggleButtons(btnStart, btnStop);
                        }
                    );
        });

        btnStop.addActionListener((ActionEvent ev) -> {
            toggleButtons(btnStart, btnStop);

            if (generatingReports != null && !generatingReports.isDisposed()) {
                generatingReports.dispose();
            }
            System.out.println("Interrupted");
        });
        setVisible(true);
    }

    private JPanel wrapInputField(Component c1, Component c2) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(c1, BorderLayout.WEST);
        panel.add(c2, BorderLayout.CENTER);
        return panel;
    }

    private void toggleButtons(JButton... buttons) {
        for (var b: buttons) {
            b.setEnabled(!b.isEnabled());
        }
    }

}
