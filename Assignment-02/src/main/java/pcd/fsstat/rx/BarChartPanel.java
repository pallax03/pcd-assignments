package pcd.fsstat.rx;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class BarChartPanel extends JPanel {
    private List<Integer> dataBuckets;
    private List<String> rangeLabels;
    private final List<Color> palette = List.of(
            new Color(202, 240, 248),
            new Color(144, 224, 239),
            new Color(0, 180, 216),
            new Color(0, 119, 182),
            new Color(3, 4, 94)
    );

    public void updateChart(final List<String> labels, final List<Integer> values) {
        this.dataBuckets = values;
        this.rangeLabels = labels;
        
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (dataBuckets == null || dataBuckets.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int maxVal = dataBuckets.stream().max(Integer::compareTo).orElse(1);
        if (maxVal == 0) maxVal = 1;

        int barWidth = panelWidth / dataBuckets.size();

        for (int i = 0; i < dataBuckets.size(); i++) {
            int value = dataBuckets.get(i);

            int barHeight = (int) (((double) value / maxVal) * (panelHeight - 40));

            int x = i * barWidth;
            int y = panelHeight - barHeight - 20;

            g2d.setColor(palette.get(i % this.palette.size()));
            g2d.fillRect(x + 5, y, barWidth - 10, barHeight);

            g2d.setColor(Color.BLACK);
            g2d.drawRect(x + 5, y, barWidth - 10, barHeight);

            String label = rangeLabels.get(i);
            g2d.drawString(label, x + (barWidth / 2) - 30, panelHeight - 5);

            if (value > 0) {
                g2d.drawString(String.valueOf(value), x + (barWidth / 2) - 10, y - 5);
            }
        }
    }
}