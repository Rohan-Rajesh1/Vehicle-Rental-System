package com.vehiclerental.ui.admin;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.Map;

/**
 * Custom Swing Component rendering Visual Analytics Charts using Graphics2D.
 * - Bar Chart: Revenue breakdown by Rental Agency.
 * - Donut Chart: Fleet Distribution (Standard Cars, Bikes, and Electric Vehicles).
 */
public class AnalyticsChartPanel extends JPanel {
    private Map<String, Double> agencyRevenueData;
    private int carCount = 0;
    private int bikeCount = 0;
    private int evCount = 0;

    public AnalyticsChartPanel() {
        setPreferredSize(new Dimension(800, 280));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
    }

    public void updateData(Map<String, Double> agencyRevenue, int cars, int bikes, int evs) {
        this.agencyRevenueData = agencyRevenue;
        this.carCount = cars;
        this.bikeCount = bikes;
        this.evCount = evs;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int halfWidth = width / 2;

        // 1. Left Side: Bar Chart for Agency Revenue
        drawBarChart(g2, 20, 20, halfWidth - 40, height - 40);

        // Separator line
        g2.setColor(new Color(226, 232, 240));
        g2.drawLine(halfWidth, 15, halfWidth, height - 15);

        // 2. Right Side: Donut Chart for Fleet Composition
        drawDonutChart(g2, halfWidth + 20, 20, halfWidth - 40, height - 40);

        g2.dispose();
    }

    private void drawBarChart(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(26, 54, 93));
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.drawString("Agency Revenue Breakdown ($)", x, y + 14);

        if (agencyRevenueData == null || agencyRevenueData.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Arial", Font.ITALIC, 13));
            g2.drawString("No booking revenue recorded yet.", x + 20, y + h / 2);
            return;
        }

        double maxVal = 100.0;
        for (double val : agencyRevenueData.values()) {
            if (val > maxVal) maxVal = val;
        }

        int barStartY = y + 40;
        int barAvailableHeight = h - 65;
        int barHeight = Math.min(32, barAvailableHeight / Math.max(1, agencyRevenueData.size()));
        int currentY = barStartY;

        Color[] barColors = {
            new Color(37, 99, 235),
            new Color(5, 150, 105),
            new Color(217, 119, 6),
            new Color(124, 58, 237)
        };

        int colorIdx = 0;
        for (Map.Entry<String, Double> entry : agencyRevenueData.entrySet()) {
            String name = entry.getKey();
            double val = entry.getValue();

            // Label
            g2.setColor(new Color(51, 65, 85));
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            String shortName = name.length() > 16 ? name.substring(0, 14) + "..." : name;
            g2.drawString(shortName, x, currentY + barHeight / 2 + 5);

            // Bar
            int barStartX = x + 120;
            int barMaxW = w - 190;
            int barW = (int) ((val / maxVal) * barMaxW);
            barW = Math.max(12, barW);

            g2.setColor(barColors[colorIdx % barColors.length]);
            g2.fillRoundRect(barStartX, currentY + 2, barW, barHeight - 6, 6, 6);

            // Value text
            g2.setColor(new Color(15, 23, 42));
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("$" + String.format("%.2f", val), barStartX + barW + 8, currentY + barHeight / 2 + 5);

            currentY += barHeight + 6;
            colorIdx++;
        }
    }

    private void drawDonutChart(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(26, 54, 93));
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.drawString("Fleet Composition (Cars vs Bikes vs EVs)", x, y + 14);

        int total = carCount + bikeCount + evCount;
        if (total == 0) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Arial", Font.ITALIC, 13));
            g2.drawString("No vehicles currently in catalog.", x + 20, y + h / 2);
            return;
        }

        int diameter = Math.min(w / 2, h - 60);
        int cx = x + 20;
        int cy = y + 35;

        double carAngle = ((double) carCount / total) * 360.0;
        double bikeAngle = ((double) bikeCount / total) * 360.0;
        double evAngle = 360.0 - carAngle - bikeAngle;

        Color carColor = new Color(59, 130, 246);
        Color bikeColor = new Color(245, 158, 11);
        Color evColor = new Color(16, 185, 129);

        // Draw Slices
        double curAngle = 90.0;
        g2.setColor(carColor);
        g2.fill(new Arc2D.Double(cx, cy, diameter, diameter, curAngle, carAngle, Arc2D.PIE));
        curAngle += carAngle;

        g2.setColor(bikeColor);
        g2.fill(new Arc2D.Double(cx, cy, diameter, diameter, curAngle, bikeAngle, Arc2D.PIE));
        curAngle += bikeAngle;

        g2.setColor(evColor);
        g2.fill(new Arc2D.Double(cx, cy, diameter, diameter, curAngle, evAngle, Arc2D.PIE));

        // Donut inner hole
        int innerDiam = (int) (diameter * 0.55);
        int holeX = cx + (diameter - innerDiam) / 2;
        int holeY = cy + (diameter - innerDiam) / 2;
        g2.setColor(Color.WHITE);
        g2.fillOval(holeX, holeY, innerDiam, innerDiam);

        g2.setColor(new Color(15, 23, 42));
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        String totalStr = total + " Total";
        int strW = g2.getFontMetrics().stringWidth(totalStr);
        g2.drawString(totalStr, holeX + (innerDiam - strW) / 2, holeY + innerDiam / 2 + 5);

        // Legend
        int legX = cx + diameter + 20;
        int legY = cy + 15;

        drawLegendItem(g2, legX, legY, carColor, "Standard Cars: " + carCount + " (" + Math.round((carCount * 100.0) / total) + "%)");
        drawLegendItem(g2, legX, legY + 28, bikeColor, "Standard Bikes: " + bikeCount + " (" + Math.round((bikeCount * 100.0) / total) + "%)");
        drawLegendItem(g2, legX, legY + 56, evColor, "⚡ Electric EVs: " + evCount + " (" + Math.round((evCount * 100.0) / total) + "%)");
    }

    private void drawLegendItem(Graphics2D g2, int x, int y, Color c, String text) {
        g2.setColor(c);
        g2.fillRoundRect(x, y - 10, 14, 14, 4, 4);
        g2.setColor(new Color(51, 65, 85));
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.drawString(text, x + 20, y + 2);
    }
}
